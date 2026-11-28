"""
Servicio de Catálogo Base e Ingesta Dinámica de Recetas para Chefsito en AWS.

Resuelve el requerimiento de no quedarse únicamente con las 24 recetas precargadas:
1. Semilla Inicial (24 recetas mexicanas base idénticas a RecipeSeed.kt).
2. Catálogo Expandido en la Nube (+12 nuevas recetas mexicanas dinámicas listas en el servidor).
3. Ingestador Externo Automático (TheMealDB Mexican/Latin + DummyJSON) que traduce,
   asigna tipo de comida (Desayuno/Comida/Cena), día sugerido y calcula el costo estimado
   en MXN por ingrediente para que el motor de costos de la app Android funcione sin cambios.
4. Carga administrativa en vivo vía JSON/API (POST /api/v1/recipes y POST /api/v1/recipes/bulk).
"""

from datetime import datetime, timezone
from typing import Any, Dict, List, Tuple
import httpx
from sqlalchemy.orm import Session

from .models import Recipe

# Diccionario de costos estimados por porción en MXN (compatible con RecipeSeed.kt + ampliado)
INGREDIENT_COSTS_MXN: Dict[str, float] = {
    "Caldo de pollo": 5.0,
    "Tortilla de harina": 8.0,
    "Tortilla de maíz": 6.0,
    "Flor de calabaza": 8.0,
    "Queso manchego": 12.0,
    "Queso fresco": 10.0,
    "Queso panela": 12.0,
    "Chile jalapeño": 3.0,
    "Chile serrano": 3.0,
    "Chile poblano": 7.0,
    "Chile guajillo": 5.0,
    "Chile chipotle": 6.0,
    "Tomate verde": 5.0,
    "Pan de caja": 8.0,
    "Champiñones": 10.0,
    "Calabacita": 7.0,
    "Zanahoria": 4.0,
    "Chícharos": 5.0,
    "Aguacate": 12.0,
    "Quesillo": 15.0,
    "Mayonesa": 5.0,
    "Cilantro": 3.0,
    "Jitomate": 5.0,
    "Lechuga": 5.0,
    "Chorizo": 12.0,
    "Nopales": 7.0,
    "Bolillo": 6.0,
    "Cebolla": 4.0,
    "Epazote": 2.0,
    "Plátano": 5.0,
    "Totopos": 8.0,
    "Bistec": 30.0,
    "Frijol": 7.0,
    "Canela": 2.0,
    "Aceite": 3.0,
    "Pollo": 25.0,
    "Huevo": 8.0,
    "Jamón": 10.0,
    "Arroz": 6.0,
    "Crema": 6.0,
    "Salsa": 5.0,
    "Elote": 8.0,
    "Leche": 8.0,
    "Avena": 6.0,
    "Limón": 2.0,
    "Atún": 14.0,
    "Papa": 6.0,
    "Miel": 5.0,
    "Ajo": 3.0,
    "Carne molida": 28.0,
    "Cerdo": 26.0,
    "Pescado": 32.0,
    "Camarón": 38.0,
    "Lentejas": 7.0,
    "Garbanzo": 8.0,
    "Espagueti": 7.0,
    "Pasta": 7.0,
}

# Claves ordenadas por longitud descendente para evitar que "Pollo" pise a "Caldo de pollo"
SORTED_COST_KEYS = sorted(INGREDIENT_COSTS_MXN.keys(), key=len, reverse=True)

# Traducción rápida de ingredientes comunes de TheMealDB / DummyJSON al español mexicano
EN_TO_ES_INGREDIENTS: Dict[str, str] = {
    "chicken": "pechuga de pollo",
    "chicken breast": "pechuga de pollo",
    "beef": "bistec de res",
    "minced beef": "carne molida de res",
    "ground beef": "carne molida de res",
    "pork": "carne de cerdo",
    "chorizo": "chorizo",
    "egg": "huevo",
    "eggs": "huevo",
    "corn tortillas": "tortilla de maíz",
    "tortillas": "tortilla de maíz",
    "flour tortillas": "tortilla de harina",
    "beans": "frijoles cocidos",
    "black beans": "frijoles negros",
    "refried beans": "frijoles refritos",
    "kidney beans": "frijoles cocidos",
    "cheese": "queso fresco",
    "cheddar cheese": "queso manchego",
    "mozzarella": "quesillo",
    "Monterey Jack Cheese": "quesillo",
    "sour cream": "crema",
    "cream": "crema",
    "tomato": "jitomate",
    "tomatoes": "jitomate",
    "plum tomatoes": "jitomate",
    "onion": "cebolla",
    "red onion": "cebolla morada",
    "garlic": "diente de ajo",
    "avocado": "aguacate",
    "lime": "limón",
    "cilantro": "cilantro",
    "coriander": "cilantro",
    "jalapeno": "chile jalapeño",
    "chili": "chile serrano",
    "green chilli": "chile serrano",
    "rice": "arroz",
    "potatoes": "papa",
    "potato": "papa",
    "lettuce": "lechuga",
    "olive oil": "aceite",
    "vegetable oil": "aceite",
    "oil": "aceite",
    "milk": "leche",
    "bread": "bolillo",
    "mushrooms": "champiñones",
    "corn": "elote",
    "sweetcorn": "elote",
    "zucchini": "calabacita",
    "carrots": "zanahoria",
    "peas": "chícharos",
    "tuna": "atún",
    "ham": "jamón",
}

WEEK_DAYS = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"]
MEAL_TYPES = ["Desayuno", "Comida", "Cena"]


def estimate_cost_for_ingredient(ingredient_text: str) -> float:
    lower_text = ingredient_text.lower()
    for key in SORTED_COST_KEYS:
        if key.lower() in lower_text:
            return INGREDIENT_COSTS_MXN[key]
    return 6.0


def build_recipe_dict(
    external_id: str,
    title: str,
    description: str,
    minutes: int,
    difficulty: str,
    meal_type: str,
    day_of_week: str,
    ingredients: List[str],
    steps: List[str],
    source: str = "SEED_MEXICANO",
    image_url: str = "",
) -> Dict[str, Any]:
    lines = []
    total = 0.0
    for item in ingredients:
        if "|" in item:
            name_part, cost_part = item.split("|", 1)
            try:
                cost = float(cost_part)
            except ValueError:
                cost = estimate_cost_for_ingredient(name_part)
            lines.append(f"{name_part.strip()}|{cost}")
            total += cost
        else:
            cost = estimate_cost_for_ingredient(item)
            lines.append(f"{item.strip()}|{cost}")
            total += cost

    formatted_steps = []
    for idx, step in enumerate(steps, start=1):
        clean = step.strip()
        if clean and not clean[0].isdigit():
            formatted_steps.append(f"{idx}. {clean}")
        else:
            formatted_steps.append(clean)

    return {
        "external_id": external_id,
        "title": title,
        "description": description,
        "prep_time_minutes": minutes,
        "difficulty": difficulty,
        "meal_type": meal_type,
        "day_of_week": day_of_week,
        "ingredients": "\n".join(lines),
        "steps": "\n".join(formatted_steps),
        "estimated_price": round(total, 2),
        "source": source,
        "image_url": image_url,
    }


def get_base_and_expanded_recipes() -> List[Dict[str, Any]]:
    """
    Devuelve las 24 recetas originales del proyecto Android + 12 recetas mexicanas
    nuevas exclusivas del catálogo en la nube de AWS (total: 36 recetas listas al iniciar).
    """
    return [
        # ==================== 8 DESAYUNOS BASE ====================
        build_recipe_dict(
            external_id="seed-mx-01",
            title="Chilaquiles con pollo y queso",
            description="Crujientes totopos bañados en salsa roja de jitomate con pollo deshebrado, queso fresco y un toque de crema. El desayuno mexicano por excelencia.",
            minutes=30,
            difficulty="Media",
            meal_type="Desayuno",
            day_of_week="Lunes",
            ingredients=[
                "120 g de totopos de maíz",
                "150 g de pechuga de pollo deshebrada",
                "3 piezas de jitomate",
                "1 pieza de chile serrano",
                "1/2 pieza de cebolla",
                "1 diente de ajo",
                "80 g de queso fresco desmenuzado",
                "2 cucharadas de crema",
                "1 ramita de cilantro",
            ],
            steps=[
                "Cuece 150 g de pechuga de pollo en agua con media cebolla y sal durante 18 minutos; deshébrala y reserva el caldo.",
                "Asa 3 jitomates, 1 chile serrano, 1/4 de cebolla y 1 diente de ajo en el comal durante 6 minutos.",
                "Licúa lo asado con 1/2 taza del caldo de pollo y cocina la salsa en una sartén con 1 cucharada de aceite por 5 minutos.",
                "Agrega el pollo deshebrado a la salsa y mezcla a fuego medio durante 3 minutos.",
                "Incorpora 120 g de totopos y mezcla rápido durante 1 minuto para que no se ablanden.",
                "Sirve con 80 g de queso fresco, 2 cucharadas de crema y cilantro picado.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-02",
            title="Huevos divorciados con frijol",
            description="Dos huevos estrellados sobre tortillas: uno bañado en salsa roja y otro en salsa verde, acompañados de frijoles refritos. Dos sabores en un solo plato.",
            minutes=25,
            difficulty="Media",
            meal_type="Desayuno",
            day_of_week="Martes",
            ingredients=[
                "2 piezas de huevo",
                "3 piezas de tortilla de maíz",
                "2 piezas de jitomate",
                "1 pieza de chile serrano",
                "1/4 pieza de cebolla",
                "1 taza de frijoles refritos",
                "50 g de queso fresco",
                "2 cucharadas de crema",
                "2 piezas de tomate verde",
            ],
            steps=[
                "Calienta 1 taza de frijoles refritos en una olla a fuego medio durante 5 minutos y reserva.",
                "Asa 2 jitomates, 1 chile serrano y 1/4 de cebolla; licúa y cocina la salsa roja 6 minutos.",
                "Hierve 2 tomates verdes con 1/2 chile serrano durante 8 minutos; licúa y cocina la salsa verde 5 minutos.",
                "Calienta 3 tortillas de maíz en el comal durante 30 segundos por lado.",
                "Fríe 2 huevos en 1 cucharada de aceite a fuego medio durante 3 minutos.",
                "Coloca un huevo sobre cada tortilla, baña uno con salsa roja y otro con salsa verde; corona con queso y crema.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-03",
            title="Enfrijoladas rellenas de jamón y quesillo",
            description="Tortillas de maíz rellenas de jamón y quesillo, bañadas en una salsa cremosa de frijol y terminadas con queso fresco. Suaves, calientes y muy reconfortantes.",
            minutes=25,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Miércoles",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles cocidos",
                "2 rebanadas de jamón",
                "100 g de quesillo deshebrado",
                "1/4 pieza de cebolla",
                "2 cucharadas de crema",
                "50 g de queso fresco",
            ],
            steps=[
                "Licúa 1 taza de frijoles cocidos con 1/2 taza de agua hasta obtener una salsa tersa.",
                "Calienta la salsa en una olla con 1 cucharada de aceite a fuego medio durante 5 minutos y sazona.",
                "Calienta 4 tortillas de maíz en el comal durante 30 segundos por lado.",
                "Rellena cada tortilla con 2 rebanadas de jamón y 100 g de quesillo; enróllalas.",
                "Pasa los rollos por la salsa de frijol caliente durante 1 minuto hasta cubrirlos.",
                "Sirve con cebolla picada, 2 cucharadas de crema y 50 g de queso fresco.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-04",
            title="Molletes con frijol, queso y pico de gallo",
            description="Mitades de bolillo tostadas con frijoles refritos y queso gratinado, servidas con pico de gallo fresco. Sencillos, económicos y deliciosos.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Jueves",
            ingredients=[
                "2 piezas de bolillo",
                "1 taza de frijoles refritos",
                "100 g de queso manchego rallado",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
                "1 pieza de chile serrano",
            ],
            steps=[
                "Parte 2 bolillos por la mitad y tuéstalos en el comal durante 3 minutos por el lado cortado.",
                "Calienta 1 taza de frijoles refritos con 1 cucharada de aceite durante 5 minutos.",
                "Unta los frijoles sobre cada mitad de bolillo.",
                "Cubre con 100 g de queso manchego rallado.",
                "Gratina en el horno a 180 °C durante 6 minutos o tapa el comal 4 minutos hasta que el queso se derrita.",
                "Prepara pico de gallo con 2 jitomates, 1/4 de cebolla, cilantro y 1 chile serrano; sirve encima.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-05",
            title="Omelette de jamón y quesillo con nopales",
            description="Omelette esponjoso relleno de jamón y quesillo fundido, acompañado de nopales asados con cebolla y jitomate. Un desayuno completo y balanceado.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Viernes",
            ingredients=[
                "3 piezas de huevo",
                "2 rebanadas de jamón",
                "80 g de quesillo",
                "2 piezas de nopales",
                "1/2 pieza de cebolla",
                "1 pieza de jitomate",
                "1 cucharada de aceite",
            ],
            steps=[
                "Lava 2 nopales, córtalos en tiras y ásalos con 1/4 de cebolla y 1 jitomate durante 8 minutos.",
                "Bate 3 huevos con una pizca de sal durante 30 segundos.",
                "Calienta 1 cucharada de aceite en una sartén antiadherente a fuego medio.",
                "Vierte el huevo y cocina 2 minutos hasta que cuaje por abajo.",
                "Coloca 2 rebanadas de jamón y 80 g de quesillo en una mitad y dobla el omelette.",
                "Cocina 1 minuto más y sirve acompañado de los nopales asados.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-06",
            title="Huevos a la mexicana con frijoles y tortillas",
            description="Huevos revueltos con jitomate, cebolla y chile serrano, acompañados de frijoles y tortillas calientitas. El desayuno de todos los días.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Sábado",
            ingredients=[
                "3 piezas de huevo",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 pieza de chile serrano",
                "1 taza de frijoles refritos",
                "3 piezas de tortilla de maíz",
                "1 cucharada de aceite",
            ],
            steps=[
                "Pica finamente 2 jitomates, 1/4 de cebolla y 1 chile serrano.",
                "Sofríe la verdura en 1 cucharada de aceite a fuego medio durante 5 minutos.",
                "Bate 3 huevos y viértelos en la sartén.",
                "Revuelve a fuego medio durante 3 minutos hasta que el huevo cuaje pero quede jugoso.",
                "Calienta 1 taza de frijoles refritos y 3 tortillas de maíz durante 4 minutos.",
                "Sirve los huevos con los frijoles y las tortillas al lado.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-07",
            title="Pan francés con plátano y canela",
            description="Rebanadas de pan remojadas en leche y huevo, doradas en sartén y servidas con plátano, miel y canela. Un desayuno dulce que gusta a todos.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Domingo",
            ingredients=[
                "4 rebanadas de pan de caja",
                "2 piezas de huevo",
                "1/2 taza de leche",
                "1 cucharadita de canela",
                "1 pieza de plátano",
                "2 cucharadas de miel",
                "1 cucharada de aceite",
            ],
            steps=[
                "Bate 2 huevos con 1/2 taza de leche y 1 cucharadita de canela durante 30 segundos.",
                "Remoja 4 rebanadas de pan de caja en la mezcla por ambos lados.",
                "Calienta 1 cucharada de aceite en una sartén a fuego medio.",
                "Dora las rebanadas 2 minutos por lado hasta que estén doradas.",
                "Rebana 1 plátano y colócalo encima.",
                "Baña con 2 cucharadas de miel y espolvorea canela al gusto.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-08",
            title="Avena con plátano, canela y miel",
            description="Avena cremosa cocida en leche con canela, endulzada con miel y coronada con plátano. Ligera, nutritiva y lista en minutos.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Lunes",
            ingredients=[
                "1 taza de avena",
                "2 tazas de leche",
                "1 pieza de plátano",
                "1 cucharadita de canela",
                "1 cucharada de miel",
            ],
            steps=[
                "Calienta 2 tazas de leche en una olla a fuego medio durante 3 minutos.",
                "Agrega 1 taza de avena y cocina moviendo constantemente 7 minutos.",
                "Añade 1 cucharadita de canela y cocina 2 minutos más hasta que espese.",
                "Endulza con 1 cucharada de miel y mezcla.",
                "Sirve en un tazón y corona con 1 plátano en rodajas.",
            ],
        ),
        # ==================== 8 COMIDAS BASE ====================
        build_recipe_dict(
            external_id="seed-mx-09",
            title="Enchiladas verdes con pollo",
            description="Tortillas rellenas de pollo bañadas en salsa verde de tomate y chile serrano, con crema y queso fresco. Un clásico de la comida mexicana.",
            minutes=35,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Lunes",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "200 g de pollo deshebrado",
                "6 piezas de tomate verde",
                "2 piezas de chile serrano",
                "1/4 pieza de cebolla",
                "1 diente de ajo",
                "3 cucharadas de crema",
                "80 g de queso fresco",
            ],
            steps=[
                "Cuece 200 g de pollo con 1/4 de cebolla y sal durante 18 minutos; deshébralo.",
                "Hierve 6 tomates verdes, 2 chiles serranos, 1/4 de cebolla y 1 diente de ajo durante 10 minutos.",
                "Licúa lo hervido con 1/2 taza de agua y cocina la salsa con 1 cucharada de aceite durante 6 minutos.",
                "Pasa 4 tortillas de maíz por aceite caliente 15 segundos por lado para suavizarlas.",
                "Rellena cada tortilla con pollo, enróllalas y colócalas en el plato.",
                "Baña con salsa verde y corona con 3 cucharadas de crema y 80 g de queso fresco.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-10",
            title="Tacos dorados de papa con crema y queso",
            description="Tacos de tortilla de maíz rellenos de papa sazonada, fritos hasta quedar dorados y servidos con lechuga, crema y queso fresco.",
            minutes=30,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Martes",
            ingredients=[
                "6 piezas de tortilla de maíz",
                "3 piezas de papa",
                "80 g de queso fresco",
                "3 cucharadas de crema",
                "1/2 pieza de lechuga",
                "2 piezas de jitomate",
                "2 tazas de aceite",
            ],
            steps=[
                "Cuece 3 papas en agua con sal durante 15 minutos; haz un puré y sazona.",
                "Calienta 6 tortillas de maíz en el comal 30 segundos por lado para que no se rompan.",
                "Rellena cada tortilla con puré de papa y enróllala.",
                "Calienta 2 tazas de aceite a 180 °C y fríe los tacos 3 minutos por lado hasta dorar.",
                "Escurre sobre papel absorbente durante 2 minutos.",
                "Sirve con lechuga, jitomate, 3 cucharadas de crema y 80 g de queso fresco.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-11",
            title="Alambre de res con quesillo",
            description="Tiras de bistec salteadas con chorizo, cebolla y chile jalapeño, cubiertas de quesillo fundido y servidas con tortillas de harina.",
            minutes=30,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Miércoles",
            ingredients=[
                "250 g de bistec de res",
                "100 g de chorizo",
                "150 g de quesillo",
                "1/2 pieza de cebolla",
                "2 piezas de chile jalapeño",
                "4 piezas de tortilla de harina",
            ],
            steps=[
                "Corta 250 g de bistec en tiras y sazona con sal y pimienta.",
                "Sofríe 100 g de chorizo en una sartén a fuego medio durante 5 minutos hasta que suelte su grasa.",
                "Agrega el bistec y cocina 6 minutos hasta que se dore.",
                "Incorpora 1/2 cebolla y 2 chiles jalapeños en tiras; cocina 4 minutos.",
                "Cubre con 150 g de quesillo y tapa la sartén 3 minutos hasta que se derrita.",
                "Sirve con 4 tortillas de harina calientes.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-12",
            title="Bistec a la mexicana con arroz y frijoles",
            description="Bistec en tiras guisado con jitomate, cebolla y chile serrano, acompañado de arroz rojo y frijoles de la olla. Comida completa y casera.",
            minutes=35,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Jueves",
            ingredients=[
                "250 g de bistec de res",
                "3 piezas de jitomate",
                "1/2 pieza de cebolla",
                "1 pieza de chile serrano",
                "1 taza de arroz",
                "1 taza de frijoles de la olla",
                "1 cucharada de aceite",
            ],
            steps=[
                "Corta 250 g de bistec en tiras y sazona con sal y pimienta.",
                "Sofríe la carne en 1 cucharada de aceite a fuego alto durante 5 minutos; reserva.",
                "En la misma sartén acitrona 1/2 cebolla, 3 jitomates y 1 chile serrano durante 5 minutos.",
                "Regresa la carne y mezcla a fuego medio durante 4 minutos.",
                "Calienta 1 taza de arroz y 1 taza de frijoles durante 5 minutos.",
                "Sirve la carne con el arroz y los frijoles.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-13",
            title="Torta completa de jamón, quesillo y chorizo",
            description="Bolillo relleno de jamón, quesillo, chorizo, aguacate, jitomate y lechuga. La torta mexicana completa para una comida rápida y contundente.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Viernes",
            ingredients=[
                "1 pieza de bolillo",
                "2 rebanadas de jamón",
                "80 g de quesillo",
                "50 g de chorizo",
                "1/2 pieza de aguacate",
                "1 pieza de jitomate",
                "2 hojas de lechuga",
                "1 cucharada de mayonesa",
            ],
            steps=[
                "Parte 1 bolillo por la mitad y úntale 1 cucharada de mayonesa.",
                "Calienta 50 g de chorizo en un comal durante 4 minutos.",
                "Agrega 2 rebanadas de jamón y 80 g de quesillo; tapa 2 minutos hasta derretir.",
                "Coloca el relleno dentro del bolillo.",
                "Agrega 1/2 aguacate, 1 jitomate y 2 hojas de lechuga.",
                "Cierra la torta, presiónala ligeramente y sirve de inmediato.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-14",
            title="Calabacitas con elote, crema y queso",
            description="Calabacitas tiernas guisadas con elote, jitomate y cebolla, terminadas con crema y queso fresco. Un guiso vegetariano lleno de sabor.",
            minutes=25,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Sábado",
            ingredients=[
                "3 piezas de calabacita",
                "1 pieza de elote",
                "3 cucharadas de crema",
                "80 g de queso fresco",
                "1/4 pieza de cebolla",
                "2 piezas de jitomate",
                "1 cucharada de aceite",
            ],
            steps=[
                "Lava y corta 3 calabacitas en cubos; desgrana 1 elote.",
                "Sofríe 1/4 de cebolla y 2 jitomates picados en 1 cucharada de aceite durante 5 minutos.",
                "Agrega las calabacitas y el elote; cocina a fuego medio 8 minutos.",
                "Sazona y cocina 3 minutos más hasta que las verduras estén suaves.",
                "Incorpora 3 cucharadas de crema y mezcla 2 minutos.",
                "Sirve con 80 g de queso fresco espolvoreado.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-15",
            title="Arroz con pollo a la mexicana",
            description="Arroz rojo cocido con pollo, zanahoria y chícharos. Un plato único, colorido y rendidor que recuerda a la comida de casa.",
            minutes=45,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Domingo",
            ingredients=[
                "300 g de pollo",
                "1 taza de arroz",
                "1 pieza de zanahoria",
                "1/2 taza de chícharos",
                "1/4 pieza de cebolla",
                "2 piezas de jitomate",
                "2 tazas de caldo de pollo",
                "1 cucharada de aceite",
            ],
            steps=[
                "Sazona 300 g de pollo y dóralo en una olla con 1 cucharada de aceite durante 6 minutos; reserva.",
                "Acitrona 1/4 de cebolla y 2 jitomates picados en la misma olla durante 4 minutos.",
                "Agrega 1 taza de arroz lavado y sofríe 2 minutos.",
                "Incorpora el pollo, 1 zanahoria en cubos y 2 tazas de caldo de pollo.",
                "Tapa y cocina a fuego bajo 20 minutos.",
                "Agrega 1/2 taza de chícharos y deja reposar 5 minutos antes de servir.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-16",
            title="Tostadas de atún con lechuga y aguacate",
            description="Tostadas crujientes con atún mezclado con mayonesa y limón, lechuga, jitomate y aguacate. Una comida fresca y rápida.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Martes",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "1 lata de atún (140 g)",
                "1/2 pieza de lechuga",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1/2 pieza de aguacate",
                "2 cucharadas de mayonesa",
                "1 pieza de limón",
            ],
            steps=[
                "Tuesta 4 tortillas de maíz en el comal 2 minutos por lado hasta que estén crujientes.",
                "Escurre 1 lata de atún y mézclala con 2 cucharadas de mayonesa y el jugo de 1 limón.",
                "Lava y corta 1/2 lechuga, 2 jitomates y 1/4 de cebolla.",
                "Unta el atún sobre cada tostada.",
                "Agrega lechuga, jitomate, cebolla y 1/2 aguacate en rebanadas.",
                "Sirve de inmediato para que la tostada no se ablande.",
            ],
        ),
        # ==================== 8 CENAS BASE ====================
        build_recipe_dict(
            external_id="seed-mx-17",
            title="Sincronizadas de jamón, quesillo y aguacate",
            description="Tortillas de harina rellenas de jamón y quesillo fundido, acompañadas de aguacate y jitomate. Una cena rápida y muy mexicana.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Lunes",
            ingredients=[
                "4 piezas de tortilla de harina",
                "2 rebanadas de jamón",
                "100 g de quesillo",
                "1/2 pieza de aguacate",
                "1 pieza de jitomate",
                "2 cucharadas de crema",
            ],
            steps=[
                "Calienta 1 tortilla de harina en el comal durante 30 segundos.",
                "Coloca 2 rebanadas de jamón y 100 g de quesillo en una mitad.",
                "Dobla la tortilla y dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas.",
                "Sirve con 1/2 aguacate, 1 jitomate y 2 cucharadas de crema.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-18",
            title="Quesadillas de quesillo con champiñones",
            description="Quesadillas de tortilla de maíz con quesillo fundido y champiñones salteados con cebolla. Ligeras, sabrosas y perfectas para la cena.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Martes",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "150 g de quesillo",
                "150 g de champiñones",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Lava y rebana 150 g de champiñones.",
                "Saltea los champiñones con 1/4 de cebolla en 1 cucharada de aceite durante 6 minutos.",
                "Calienta 1 tortilla de maíz y coloca 150 g de quesillo en una mitad.",
                "Agrega los champiñones y dobla la tortilla.",
                "Dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas y sirve con cilantro y 2 cucharadas de salsa.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-19",
            title="Tostadas de frijol con pollo, lechuga, crema y queso",
            description="Tostadas untadas con frijoles refritos, pollo deshebrado, lechuga fresca, crema y queso. Una cena completa con textura crujiente.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Miércoles",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles refritos",
                "150 g de pollo deshebrado",
                "1/2 pieza de lechuga",
                "3 cucharadas de crema",
                "80 g de queso fresco",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Tuesta 4 tortillas de maíz en el comal 2 minutos por lado.",
                "Calienta 1 taza de frijoles refritos y úntalos sobre las tostadas.",
                "Coloca 150 g de pollo deshebrado encima.",
                "Agrega 1/2 lechuga picada.",
                "Corona con 3 cucharadas de crema, 80 g de queso fresco y 2 cucharadas de salsa.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-20",
            title="Burritos de frijol con queso y chorizo",
            description="Tortillas de harina rellenas de frijoles, queso manchego, chorizo y huevo. Una cena contundente que se prepara en minutos.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Jueves",
            ingredients=[
                "4 piezas de tortilla de harina",
                "1 taza de frijoles refritos",
                "100 g de queso manchego",
                "80 g de chorizo",
                "2 piezas de huevo",
                "2 cucharadas de crema",
            ],
            steps=[
                "Calienta 1 taza de frijoles refritos y machácalos durante 5 minutos.",
                "Fríe 80 g de chorizo durante 4 minutos y revuélvelo con 2 huevos batidos.",
                "Calienta 4 tortillas de harina en el comal 30 segundos por lado.",
                "Rellena con frijol, 100 g de queso manchego, chorizo y huevo.",
                "Enrolla los burritos y dóralos 2 minutos por lado en el comal.",
                "Sirve con 2 cucharadas de crema al gusto.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-21",
            title="Sándwich ligero de panela asada y jamón",
            description="Pan de caja tostado con panela asada, jamón, lechuga, jitomate y un toque de mayonesa. Cena ligera y rápida.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Viernes",
            ingredients=[
                "2 rebanadas de pan de caja",
                "100 g de queso panela",
                "2 rebanadas de jamón",
                "2 hojas de lechuga",
                "1 pieza de jitomate",
                "1 cucharada de mayonesa",
            ],
            steps=[
                "Asa 100 g de queso panela en un comal durante 3 minutos por lado.",
                "Tuesta 2 rebanadas de pan de caja 2 minutos.",
                "Úntales 1 cucharada de mayonesa.",
                "Arma el sándwich con 2 rebanadas de jamón, la panela, 2 hojas de lechuga y 1 jitomate.",
                "Cierra y corta por la mitad para servir.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-22",
            title="Molletes dulces con miel y canela",
            description="Bolillo tostado con miel, canela y plátano, acompañado de un vaso de leche. Una cena dulce y sencilla para cerrar el día.",
            minutes=12,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Sábado",
            ingredients=[
                "1 pieza de bolillo",
                "2 cucharadas de miel",
                "1 cucharadita de canela",
                "1 pieza de plátano",
                "1 taza de leche",
            ],
            steps=[
                "Parte 1 bolillo por la mitad y tuéstalo 3 minutos.",
                "Úntale 2 cucharadas de miel mientras está caliente.",
                "Espolvorea 1 cucharadita de canela.",
                "Coloca 1 plátano en rodajas encima.",
                "Sirve con 1 taza de leche fría o caliente.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-23",
            title="Quesadillas de flor de calabaza",
            description="Quesadillas de tortilla de maíz con flor de calabaza y quesillo, sazonadas con epazote. Un clásico vegetariano de temporada.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Domingo",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "150 g de flor de calabaza",
                "120 g de quesillo",
                "1/4 pieza de cebolla",
                "2 ramitas de epazote",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Lava y limpia 150 g de flor de calabaza.",
                "Sofríe la flor con 1/4 de cebolla y 2 ramitas de epazote durante 5 minutos.",
                "Calienta 1 tortilla y coloca 120 g de quesillo en una mitad.",
                "Agrega la flor de calabaza y dobla la tortilla.",
                "Dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas y sirve con 2 cucharadas de salsa.",
            ],
        ),
        build_recipe_dict(
            external_id="seed-mx-24",
            title="Enfrijoladas sencillas con queso",
            description="Tortillas de maíz bañadas en salsa de frijol, con cebolla, crema y queso fresco. La cena más sencilla y reconfortante de la semana.",
            minutes=15,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Lunes",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles cocidos",
                "80 g de queso fresco",
                "1/4 pieza de cebolla",
                "3 cucharadas de crema",
                "1 pieza de chile serrano",
            ],
            steps=[
                "Licúa 1 taza de frijoles cocidos con 1/2 taza de agua y 1 chile serrano.",
                "Calienta la salsa en una olla con 1 cucharada de aceite durante 5 minutos.",
                "Calienta 4 tortillas de maíz en el comal 30 segundos por lado.",
                "Pasa las tortillas por la salsa hasta cubrirlas.",
                "Colócalas en el plato y baña con más salsa.",
                "Corona con cebolla picada, 3 cucharadas de crema y 80 g de queso fresco.",
            ],
        ),
        # ==================== +12 RECETAS DINÁMICAS DE LA NUBE AWS ====================
        build_recipe_dict(
            external_id="aws-cloud-25",
            title="Huevos con chorizo y tortillas recién hechas",
            description="Huevos revueltos con chorizo bien doradito, acompañados de frijoles refritos y tortillas de maíz calientes. Receta actualizada desde el servidor AWS.",
            minutes=18,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Martes",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "3 piezas de huevo",
                "80 g de chorizo",
                "1 taza de frijoles refritos",
                "3 piezas de tortilla de maíz",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Desmenuza 80 g de chorizo en una sartén caliente y fríelo a fuego medio durante 5 minutos en su propia grasa.",
                "Bate 3 huevos con una pizca de sal y viértelos sobre el chorizo dorado.",
                "Cocina moviendo suavemente por 3 minutos hasta que el huevo quede en su punto.",
                "Calienta 1 taza de frijoles refritos y 3 tortillas de maíz en el comal.",
                "Sirve el huevo con chorizo acompañado de los frijoles y salsa al gusto.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-26",
            title="Chilaquiles verdes con huevo estrellado",
            description="Totopos bañados en salsa verde asada de tomate y cilantro, coronados con un huevo estrellado de yema suave, crema y queso fresco.",
            minutes=25,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Miércoles",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "120 g de totopos",
                "5 piezas de tomate verde",
                "1 pieza de chile serrano",
                "2 piezas de huevo",
                "60 g de queso fresco",
                "2 cucharadas de crema",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
            ],
            steps=[
                "Hierve 5 tomates verdes y 1 chile serrano durante 8 minutos.",
                "Licúa con 1/4 de cebolla y cilantro; sofríe la salsa 5 minutos en 1 cucharada de aceite.",
                "Estrella 2 huevos en una sartén con aceite caliente durante 3 minutos.",
                "Baña los 120 g de totopos con la salsa verde caliente.",
                "Coloca encima los huevos estrellados, la crema y el queso fresco.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-27",
            title="Nopales con huevo en salsa roja casera",
            description="Guisado tradicional mexicano de nopales tiernos con huevo revuelto bañados en salsa de jitomate y chile serrano. Económico y muy nutritivo.",
            minutes=22,
            difficulty="Fácil",
            meal_type="Desayuno",
            day_of_week="Jueves",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "3 piezas de nopales",
                "3 piezas de huevo",
                "3 piezas de jitomate",
                "1 pieza de chile serrano",
                "1/4 pieza de cebolla",
                "1 diente de ajo",
                "3 piezas de tortilla de maíz",
            ],
            steps=[
                "Corta 3 nopales en cuadros y cuécelos con sal y un trozo de cebolla por 10 minutos; escurre.",
                "Asa y licúa 3 jitomates, 1 chile serrano, cebolla y ajo.",
                "Bate 3 huevos, agrégalos a una sartén con los nopales y cocina 4 minutos.",
                "Vierte la salsa roja sobre el huevo con nopales y deja hervir 6 minutos a fuego bajo.",
                "Sirve bien caliente acompañado de tortillas de maíz.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-28",
            title="Tinga de pollo con tostadas y crema",
            description="Pollo deshebrado guisado con mucha cebolla fileteada y salsa de jitomate con un toque ahumado, servido sobre tostadas con crema y queso.",
            minutes=35,
            difficulty="Media",
            meal_type="Comida",
            day_of_week="Lunes",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "250 g de pollo deshebrado",
                "4 piezas de jitomate",
                "1 pieza de cebolla",
                "1 diente de ajo",
                "4 piezas de tortilla de maíz",
                "3 cucharadas de crema",
                "80 g de queso fresco",
            ],
            steps=[
                "Cuece 250 g de pollo y deshébralo finamente.",
                "Filetea 1 cebolla completa y acitrónala en una sartén con 1 cucharada de aceite por 5 minutos.",
                "Licúa 4 jitomates cocidos con 1 diente de ajo y un toque de chile al gusto.",
                "Vierte la salsa sobre la cebolla, agrega el pollo deshebrado y cocina 10 minutos a fuego medio.",
                "Sirve sobre tortillas tostadas con crema y queso fresco.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-29",
            title="Sopa de tortilla azteca con aguacate y queso",
            description="Caldillo de jitomate asado servido con tiras de tortilla dorada, cubos de queso panela, aguacate y crema.",
            minutes=30,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Miércoles",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "5 piezas de tortilla de maíz",
                "4 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 diente de ajo",
                "2 tazas de caldo de pollo",
                "100 g de queso panela",
                "1/2 pieza de aguacate",
                "2 cucharadas de crema",
            ],
            steps=[
                "Corta 5 tortillas de maíz en tiras delgadas y dóralas en el comal o con poco aceite hasta que crujan.",
                "Asa 4 jitomates, cebolla y ajo; licúalos con 2 tazas de caldo de pollo.",
                "Hierve el caldillo en una olla durante 10 minutos con una ramita de epazote o cilantro.",
                "Sirve las tiras de tortilla en un plato hondo y baña con el caldillo hirviendo.",
                "Corona con cubos de queso panela, aguacate y crema.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-30",
            title="Papas con chorizo y arroz rojo",
            description="Cubos de papa suave salteados con chorizo bien dorado, servidos con arroz y tortillas de maíz. Rendidor y muy económico.",
            minutes=30,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Viernes",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "3 piezas de papa",
                "120 g de chorizo",
                "1 taza de arroz",
                "1/4 pieza de cebolla",
                "4 piezas de tortilla de maíz",
            ],
            steps=[
                "Corta 3 papas en cubos medianos y cuécelas en agua con sal durante 12 minutos; escurre.",
                "Fríe 120 g de chorizo con 1/4 de cebolla picada durante 5 minutos.",
                "Incorpora las papas al chorizo y dora todo junto a fuego medio durante 7 minutos.",
                "Calienta 1 taza de arroz y 4 tortillas de maíz.",
                "Sirve las papas con chorizo acompañadas del arroz.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-31",
            title="Fajitas de pollo con cebolla, chile y arroz",
            description="Tiras de pechuga de pollo marinadas con limón y salteadas con cebolla y chile jalapeño, acompañadas de arroz.",
            minutes=28,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Sábado",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "250 g de pollo",
                "1/2 pieza de cebolla",
                "2 piezas de chile jalapeño",
                "1 pieza de limón",
                "1 taza de arroz",
                "3 piezas de tortilla de maíz",
                "1 cucharada de aceite",
            ],
            steps=[
                "Corta 250 g de pollo en fajitas y marínalas con el jugo de 1 limón, sal y pimienta.",
                "Corta 1/2 cebolla y 2 chiles jalapeños en julianas.",
                "Saltea el pollo en 1 cucharada de aceite a fuego alto durante 6 minutos.",
                "Agrega la cebolla y el chile; cocina 5 minutos más hasta que doren.",
                "Sirve con 1 taza de arroz caliente y tortillas de maíz.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-32",
            title="Croquetas de atún con papa y ensalada fresca",
            description="Tortitas doradas de atún con puré de papa y huevo, acompañadas de lechuga fresca, jitomate y aguacate.",
            minutes=25,
            difficulty="Fácil",
            meal_type="Comida",
            day_of_week="Domingo",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "1 lata de atún",
                "2 piezas de papa",
                "1 pieza de huevo",
                "1/2 pieza de lechuga",
                "1 pieza de jitomate",
                "1/2 pieza de aguacate",
                "2 cucharadas de aceite",
            ],
            steps=[
                "Cuece 2 papas durante 15 minutos, pélalas y machácalas hasta formar un puré firme.",
                "Mezcla el puré con 1 lata de atún bien escurrida, 1 huevo y sal.",
                "Forma 4 tortitas compactas con las manos.",
                "Dóralas en una sartén con 2 cucharadas de aceite durante 3 minutos por lado.",
                "Sirve acompañadas de lechuga, jitomate y aguacate.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-33",
            title="Gringas caseras de bistec y quesillo",
            description="Tortillas de harina rellenas de bistec finamente picado y abundante quesillo fundido con cebolla y cilantro.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Martes",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "4 piezas de tortilla de harina",
                "180 g de bistec",
                "120 g de quesillo",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Pica 180 g de bistec en trozos pequeños y dóralo en sartén durante 6 minutos.",
                "Calienta las tortillas de harina y distribuye 120 g de quesillo y el bistec.",
                "Dobla a la mitad y calienta 2 minutos por lado hasta fundir el quesillo.",
                "Sirve con cebolla, cilantro picado y salsa.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-34",
            title="Huarache casero de nopal con frijol y queso panela",
            description="Pencas de nopal asadas al comal cubiertas con frijoles refritos, queso panela asado, pico de gallo y aguacate. Cena ligera y saludable.",
            minutes=18,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Miércoles",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "2 piezas de nopales",
                "1/2 taza de frijoles refritos",
                "100 g de queso panela",
                "1 pieza de jitomate",
                "1/4 pieza de cebolla",
                "1/2 pieza de aguacate",
                "2 cucharadas de salsa",
            ],
            steps=[
                "Asa 2 nopales enteros limpios en el comal por 4 minutos de cada lado.",
                "Unta frijoles refritos calientes encima de cada nopal.",
                "Asa rebanadas de queso panela y colócalas sobre el frijol.",
                "Termina con jitomate picado, cebolla, aguacate y salsa.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-35",
            title="Enchiladas rojas sencillas de queso fresco",
            description="Tortillas pasadas por salsa roja de jitomate y rellenas de queso fresco con cebolla, listas en menos de 20 minutos para la cena.",
            minutes=20,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Viernes",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "4 piezas de tortilla de maíz",
                "3 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 diente de ajo",
                "120 g de queso fresco",
                "2 cucharadas de crema",
            ],
            steps=[
                "Cuece 3 jitomates con ajo y licúalos para hacer una salsa roja espesa.",
                "Sofríe la salsa en una sartén por 5 minutos.",
                "Suaviza 4 tortillas en el comal, báñalas en la salsa y rellénalas con queso fresco y cebolla.",
                "Sirve con crema por encima.",
            ],
        ),
        build_recipe_dict(
            external_id="aws-cloud-36",
            title="Avena nocturna tibia con manzana o plátano y leche",
            description="Tazón reconfortante de avena con leche tibia, canela y miel para una cena rápida, económica y digestiva.",
            minutes=12,
            difficulty="Fácil",
            meal_type="Cena",
            day_of_week="Domingo",
            source="AWS_CLOUD_CATALOG",
            ingredients=[
                "1 taza de avena",
                "1 taza de leche",
                "1 pieza de plátano",
                "1 cucharadita de canela",
                "1 cucharada de miel",
            ],
            steps=[
                "Hierve 1 taza de leche con media taza de agua y canela durante 4 minutos.",
                "Añade 1 taza de avena y cocina a fuego bajo por 5 minutos.",
                "Sirve con plátano rebanado y 1 cucharada de miel.",
            ],
        ),
    ]


def seed_initial_catalog(db: Session) -> Tuple[int, int]:
    """
    Siembra o actualiza las 36 recetas del catálogo base + nube en RDS PostgreSQL
    sin duplicar registros existentes (usando external_id o title).
    """
    catalog = get_base_and_expanded_recipes()
    inserted = 0
    updated = 0

    for item in catalog:
        existing = (
            db.query(Recipe)
            .filter(
                (Recipe.external_id == item["external_id"])
                | (Recipe.title == item["title"])
            )
            .first()
        )
        if existing is None:
            recipe = Recipe(**item)
            db.add(recipe)
            inserted += 1
        else:
            if not existing.external_id:
                existing.external_id = item["external_id"]
                updated += 1

    db.commit()
    return inserted, updated


def _translate_ingredient(measure: str, raw_name: str) -> str:
    clean_name = raw_name.strip()
    lower_name = clean_name.lower()
    translated = EN_TO_ES_INGREDIENTS.get(lower_name)
    if not translated:
        for en_key, es_val in EN_TO_ES_INGREDIENTS.items():
            if en_key in lower_name:
                translated = es_val
                break
    final_name = translated or clean_name
    clean_measure = (measure or "").strip()
    if clean_measure:
        return f"{clean_measure} de {final_name}"
    return final_name


async def sync_external_recipes_into_db(db: Session, max_to_import: int = 10) -> Dict[str, Any]:
    """
    Consulta proveedores externos reales (TheMealDB área Mexican + DummyJSON Recipes),
    normaliza sus ingredientes al formato de costos de Chefsito ("Ingrediente|CostoMXN"),
    les asigna tiempo de comida y día de la semana, y los guarda en PostgreSQL (AWS RDS).
    """
    imported = 0
    updated = 0
    sources_queried: List[str] = []
    details: List[str] = []

    async with httpx.AsyncClient(timeout=12.0) as client:
        # 1. Consultar TheMealDB (Recetas mexicanas reales)
        try:
            sources_queried.append("TheMealDB (Mexican Cuisine API)")
            resp = await client.get(
                "https://www.themealdb.com/api/json/v1/1/filter.php?a=Mexican"
            )
            if resp.status_code == 200:
                meals = (resp.json().get("meals") or [])[:max_to_import]
                for idx, summary in enumerate(meals):
                    meal_id = summary.get("idMeal")
                    if not meal_id:
                        continue
                    ext_id = f"themealdb-{meal_id}"
                    detail_resp = await client.get(
                        f"https://www.themealdb.com/api/json/v1/1/lookup.php?i={meal_id}"
                    )
                    if detail_resp.status_code != 200:
                        continue
                    meal_list = detail_resp.json().get("meals") or []
                    if not meal_list:
                        continue
                    meal = meal_list[0]

                    raw_ingredients: List[str] = []
                    for i in range(1, 21):
                        ing = (meal.get(f"strIngredient{i}") or "").strip()
                        meas = (meal.get(f"strMeasure{i}") or "").strip()
                        if ing:
                            raw_ingredients.append(_translate_ingredient(meas, ing))

                    if not raw_ingredients:
                        continue

                    instructions_raw = (meal.get("strInstructions") or "").strip()
                    steps = [
                        line.strip()
                        for line in instructions_raw.replace("\r\n", "\n").split("\n")
                        if len(line.strip()) > 10
                    ][:7]
                    if not steps:
                        steps = ["Prepara y cocina los ingredientes siguiendo el método tradicional."]

                    meal_type = MEAL_TYPES[idx % len(MEAL_TYPES)]
                    day_of_week = WEEK_DAYS[idx % len(WEEK_DAYS)]
                    title = f"{meal.get('strMeal', 'Receta Mexicana')} (Edición Cloud)"

                    recipe_data = build_recipe_dict(
                        external_id=ext_id,
                        title=title,
                        description=f"Receta sincronizada dinámicamente desde TheMealDB ({meal.get('strCategory', 'Mexicana')}). Evaluada automáticamente con los costos de tu despensa.",
                        minutes=30,
                        difficulty="Media",
                        meal_type=meal_type,
                        day_of_week=day_of_week,
                        ingredients=raw_ingredients,
                        steps=steps,
                        source="THEMEALDB_LIVE",
                        image_url=meal.get("strMealThumb") or "",
                    )

                    existing = (
                        db.query(Recipe)
                        .filter(Recipe.external_id == ext_id)
                        .first()
                    )
                    if existing is None:
                        db.add(Recipe(**recipe_data))
                        imported += 1
                        details.append(f"Importada: {title}")
                    else:
                        existing.ingredients = recipe_data["ingredients"]
                        existing.estimated_price = recipe_data["estimated_price"]
                        existing.updated_at = datetime.now(timezone.utc)
                        updated += 1
        except Exception as exc:
            details.append(f"Aviso TheMealDB: {exc}")

        # 2. Consultar DummyJSON Recipes para ampliar variedad
        try:
            sources_queried.append("DummyJSON Recipes API")
            resp = await client.get("https://dummyjson.com/recipes?limit=6")
            if resp.status_code == 200:
                recipes_json = resp.json().get("recipes") or []
                for idx, item in enumerate(recipes_json):
                    ext_id = f"dummyjson-{item.get('id', idx)}"
                    raw_ings = [
                        _translate_ingredient("", ing)
                        for ing in (item.get("ingredients") or [])
                    ]
                    steps = item.get("instructions") or ["Cocinar todos los ingredientes."]
                    if not raw_ings:
                        continue

                    meal_type = MEAL_TYPES[(idx + 1) % len(MEAL_TYPES)]
                    day_of_week = WEEK_DAYS[(idx + 2) % len(WEEK_DAYS)]
                    title = f"{item.get('name', 'Receta Internacional')} ({item.get('cuisine', 'Comunidad')})"

                    recipe_data = build_recipe_dict(
                        external_id=ext_id,
                        title=title,
                        description=f"Receta de la comunidad ({item.get('cuisine', 'Internacional')}) adaptada automáticamente con precios estimados en MXN para tu despensa.",
                        minutes=int(item.get("prepTimeMinutes") or 25),
                        difficulty="Fácil" if item.get("difficulty") == "Easy" else "Media",
                        meal_type=meal_type,
                        day_of_week=day_of_week,
                        ingredients=raw_ings,
                        steps=steps,
                        source="DUMMYJSON_LIVE",
                        image_url=item.get("image") or "",
                    )

                    existing = (
                        db.query(Recipe)
                        .filter(Recipe.external_id == ext_id)
                        .first()
                    )
                    if existing is None:
                        db.add(Recipe(**recipe_data))
                        imported += 1
                        details.append(f"Importada: {title}")
                    else:
                        existing.updated_at = datetime.now(timezone.utc)
                        updated += 1
        except Exception as exc:
            details.append(f"Aviso DummyJSON: {exc}")

    db.commit()
    total_count = db.query(Recipe).count()
    return {
        "imported_count": imported,
        "updated_count": updated,
        "total_catalog_count": total_count,
        "sources_queried": sources_queried,
        "details": details,
    }
