package com.example.chefsito.data.local

import com.example.chefsito.data.local.entity.RecipeEntity

/**
 * Catálogo base de recetas mexicanas que se carga la primera vez que se crea la
 * base de datos.
 *
 * Cada ingrediente se guarda con el formato `"Cantidad + Nombre|CostoEstimado"`.
 * El costo estimado SOLO se usa cuando el usuario no tiene ese ingrediente en su
 * despensa; si lo tiene, el motor usa el costo real que él registró.
 */
object RecipeSeed {

    /** Costo estimado por porción de cada ingrediente (MXN). */
    private val ingredientCosts = mapOf(
        "Huevo" to 8.0,
        "Tortilla de maíz" to 6.0,
        "Tortilla de harina" to 8.0,
        "Frijol" to 7.0,
        "Jamón" to 10.0,
        "Queso fresco" to 10.0,
        "Queso panela" to 12.0,
        "Queso manchego" to 12.0,
        "Quesillo" to 15.0,
        "Arroz" to 6.0,
        "Bolillo" to 6.0,
        "Jitomate" to 5.0,
        "Cebolla" to 4.0,
        "Ajo" to 3.0,
        "Chile serrano" to 3.0,
        "Chile jalapeño" to 3.0,
        "Aguacate" to 12.0,
        "Crema" to 6.0,
        "Pollo" to 25.0,
        "Bistec" to 30.0,
        "Atún" to 14.0,
        "Nopales" to 7.0,
        "Papa" to 6.0,
        "Chorizo" to 12.0,
        "Lechuga" to 5.0,
        "Cilantro" to 3.0,
        "Salsa" to 5.0,
        "Aceite" to 3.0,
        "Caldo de pollo" to 5.0,
        "Tomate verde" to 5.0,
        "Elote" to 8.0,
        "Calabacita" to 7.0,
        "Champiñones" to 10.0,
        "Flor de calabaza" to 8.0,
        "Epazote" to 2.0,
        "Pan de caja" to 8.0,
        "Leche" to 8.0,
        "Canela" to 2.0,
        "Miel" to 5.0,
        "Plátano" to 5.0,
        "Avena" to 6.0,
        "Mayonesa" to 5.0,
        "Limón" to 2.0,
        "Totopos" to 8.0,
        "Zanahoria" to 4.0,
        "Chícharos" to 5.0
    )

    /** Claves ordenadas de mayor a menor para no confundir "Pollo" con "Caldo de pollo". */
    private val costKeys = ingredientCosts.keys.sortedByDescending { it.length }

    private fun costFor(ingredientWithQuantity: String): Double {
        val key = costKeys.firstOrNull { ingredientWithQuantity.contains(it, ignoreCase = true) }
        return key?.let { ingredientCosts[it] } ?: 5.0
    }

    private fun recipe(
        title: String,
        description: String,
        minutes: Int,
        difficulty: String,
        mealType: String,
        dayOfWeek: String,
        ingredients: List<String>,
        steps: List<String>
    ): RecipeEntity {
        val ingredientLines = ingredients.map { ingredient ->
            "$ingredient|${costFor(ingredient)}"
        }
        val estimatedTotal = ingredientLines.sumOf { line ->
            line.substringAfter("|").toDoubleOrNull() ?: 0.0
        }

        return RecipeEntity(
            title = title,
            estimatedPrice = estimatedTotal,
            prepTimeMinutes = minutes,
            difficulty = difficulty,
            description = description,
            mealType = mealType,
            dayOfWeek = dayOfWeek,
            ingredients = ingredientLines.joinToString("\n"),
            steps = steps.mapIndexed { index, step -> "${index + 1}. $step" }
                .joinToString("\n")
        )
    }

    val recipes: List<RecipeEntity> = listOf(

        // ============================ DESAYUNOS ============================
        recipe(
            title = "Chilaquiles con pollo y queso",
            description = "Crujientes totopos bañados en salsa roja de jitomate con pollo deshebrado, queso fresco y un toque de crema. El desayuno mexicano por excelencia.",
            minutes = 30,
            difficulty = "Media",
            mealType = "Desayuno",
            dayOfWeek = "Lunes",
            ingredients = listOf(
                "120 g de totopos de maíz",
                "150 g de pechuga de pollo deshebrada",
                "3 piezas de jitomate",
                "1 pieza de chile serrano",
                "1/2 pieza de cebolla",
                "1 diente de ajo",
                "80 g de queso fresco desmenuzado",
                "2 cucharadas de crema",
                "1 ramita de cilantro"
            ),
            steps = listOf(
                "Cuece 150 g de pechuga de pollo en agua con media cebolla y sal durante 18 minutos; deshébrala y reserva el caldo.",
                "Asa 3 jitomates, 1 chile serrano, 1/4 de cebolla y 1 diente de ajo en el comal durante 6 minutos.",
                "Licúa lo asado con 1/2 taza del caldo de pollo y cocina la salsa en una sartén con 1 cucharada de aceite por 5 minutos.",
                "Agrega el pollo deshebrado a la salsa y mezcla a fuego medio durante 3 minutos.",
                "Incorpora 120 g de totopos y mezcla rápido durante 1 minuto para que no se ablanden.",
                "Sirve con 80 g de queso fresco, 2 cucharadas de crema y cilantro picado."
            )
        ),
        recipe(
            title = "Huevos divorciados con frijol",
            description = "Dos huevos estrellados sobre tortillas: uno bañado en salsa roja y otro en salsa verde, acompañados de frijoles refritos. Dos sabores en un solo plato.",
            minutes = 25,
            difficulty = "Media",
            mealType = "Desayuno",
            dayOfWeek = "Martes",
            ingredients = listOf(
                "2 piezas de huevo",
                "3 piezas de tortilla de maíz",
                "2 piezas de jitomate",
                "1 pieza de chile serrano",
                "1/4 pieza de cebolla",
                "1 taza de frijoles refritos",
                "50 g de queso fresco",
                "2 cucharadas de crema",
                "2 piezas de tomate verde"
            ),
            steps = listOf(
                "Calienta 1 taza de frijoles refritos en una olla a fuego medio durante 5 minutos y reserva.",
                "Asa 2 jitomates, 1 chile serrano y 1/4 de cebolla; licúa y cocina la salsa roja 6 minutos.",
                "Hierve 2 tomates verdes con 1/2 chile serrano durante 8 minutos; licúa y cocina la salsa verde 5 minutos.",
                "Calienta 3 tortillas de maíz en el comal durante 30 segundos por lado.",
                "Fríe 2 huevos en 1 cucharada de aceite a fuego medio durante 3 minutos.",
                "Coloca un huevo sobre cada tortilla, baña uno con salsa roja y otro con salsa verde; corona con queso y crema."
            )
        ),
        recipe(
            title = "Enfrijoladas rellenas de jamón y quesillo",
            description = "Tortillas de maíz rellenas de jamón y quesillo, bañadas en una salsa cremosa de frijol y terminadas con queso fresco. Suaves, calientes y muy reconfortantes.",
            minutes = 25,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Miércoles",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles cocidos",
                "2 rebanadas de jamón",
                "100 g de quesillo deshebrado",
                "1/4 pieza de cebolla",
                "2 cucharadas de crema",
                "50 g de queso fresco"
            ),
            steps = listOf(
                "Licúa 1 taza de frijoles cocidos con 1/2 taza de agua hasta obtener una salsa tersa.",
                "Calienta la salsa en una olla con 1 cucharada de aceite a fuego medio durante 5 minutos y sazona.",
                "Calienta 4 tortillas de maíz en el comal durante 30 segundos por lado.",
                "Rellena cada tortilla con 2 rebanadas de jamón y 100 g de quesillo; enróllalas.",
                "Pasa los rollos por la salsa de frijol caliente durante 1 minuto hasta cubrirlos.",
                "Sirve con cebolla picada, 2 cucharadas de crema y 50 g de queso fresco."
            )
        ),
        recipe(
            title = "Molletes con frijol, queso y pico de gallo",
            description = "Mitades de bolillo tostadas con frijoles refritos y queso gratinado, servidas con pico de gallo fresco. Sencillos, económicos y deliciosos.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Jueves",
            ingredients = listOf(
                "2 piezas de bolillo",
                "1 taza de frijoles refritos",
                "100 g de queso manchego rallado",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
                "1 pieza de chile serrano"
            ),
            steps = listOf(
                "Parte 2 bolillos por la mitad y tuéstalos en el comal durante 3 minutos por el lado cortado.",
                "Calienta 1 taza de frijoles refritos con 1 cucharada de aceite durante 5 minutos.",
                "Unta los frijoles sobre cada mitad de bolillo.",
                "Cubre con 100 g de queso manchego rallado.",
                "Gratina en el horno a 180 °C durante 6 minutos o tapa el comal 4 minutos hasta que el queso se derrita.",
                "Prepara pico de gallo con 2 jitomates, 1/4 de cebolla, cilantro y 1 chile serrano; sirve encima."
            )
        ),
        recipe(
            title = "Omelette de jamón y quesillo con nopales",
            description = "Omelette esponjoso relleno de jamón y quesillo fundido, acompañado de nopales asados con cebolla y jitomate. Un desayuno completo y balanceado.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Viernes",
            ingredients = listOf(
                "3 piezas de huevo",
                "2 rebanadas de jamón",
                "80 g de quesillo",
                "2 piezas de nopales",
                "1/2 pieza de cebolla",
                "1 pieza de jitomate",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Lava 2 nopales, córtalos en tiras y ásalos con 1/4 de cebolla y 1 jitomate durante 8 minutos.",
                "Bate 3 huevos con una pizca de sal durante 30 segundos.",
                "Calienta 1 cucharada de aceite en una sartén antiadherente a fuego medio.",
                "Vierte el huevo y cocina 2 minutos hasta que cuaje por abajo.",
                "Coloca 2 rebanadas de jamón y 80 g de quesillo en una mitad y dobla el omelette.",
                "Cocina 1 minuto más y sirve acompañado de los nopales asados."
            )
        ),
        recipe(
            title = "Huevos a la mexicana con frijoles y tortillas",
            description = "Huevos revueltos con jitomate, cebolla y chile serrano, acompañados de frijoles y tortillas calientitas. El desayuno de todos los días.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Sábado",
            ingredients = listOf(
                "3 piezas de huevo",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1 pieza de chile serrano",
                "1 taza de frijoles refritos",
                "3 piezas de tortilla de maíz",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Pica finamente 2 jitomates, 1/4 de cebolla y 1 chile serrano.",
                "Sofríe la verdura en 1 cucharada de aceite a fuego medio durante 5 minutos.",
                "Bate 3 huevos y viértelos en la sartén.",
                "Revuelve a fuego medio durante 3 minutos hasta que el huevo cuaje pero quede jugoso.",
                "Calienta 1 taza de frijoles refritos y 3 tortillas de maíz durante 4 minutos.",
                "Sirve los huevos con los frijoles y las tortillas al lado."
            )
        ),
        recipe(
            title = "Pan francés con plátano y canela",
            description = "Rebanadas de pan remojadas en leche y huevo, doradas en sartén y servidas con plátano, miel y canela. Un desayuno dulce que gusta a todos.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Domingo",
            ingredients = listOf(
                "4 rebanadas de pan de caja",
                "2 piezas de huevo",
                "1/2 taza de leche",
                "1 cucharadita de canela",
                "1 pieza de plátano",
                "2 cucharadas de miel",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Bate 2 huevos con 1/2 taza de leche y 1 cucharadita de canela durante 30 segundos.",
                "Remoja 4 rebanadas de pan de caja en la mezcla por ambos lados.",
                "Calienta 1 cucharada de aceite en una sartén a fuego medio.",
                "Dora las rebanadas 2 minutos por lado hasta que estén doradas.",
                "Rebana 1 plátano y colócalo encima.",
                "Baña con 2 cucharadas de miel y espolvorea canela al gusto."
            )
        ),
        recipe(
            title = "Avena con plátano, canela y miel",
            description = "Avena cremosa cocida en leche con canela, endulzada con miel y coronada con plátano. Ligera, nutritiva y lista en minutos.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Desayuno",
            dayOfWeek = "Lunes",
            ingredients = listOf(
                "1 taza de avena",
                "2 tazas de leche",
                "1 pieza de plátano",
                "1 cucharadita de canela",
                "1 cucharada de miel"
            ),
            steps = listOf(
                "Calienta 2 tazas de leche en una olla a fuego medio durante 3 minutos.",
                "Agrega 1 taza de avena y cocina moviendo constantemente 7 minutos.",
                "Añade 1 cucharadita de canela y cocina 2 minutos más hasta que espese.",
                "Endulza con 1 cucharada de miel y mezcla.",
                "Sirve en un tazón y corona con 1 plátano en rodajas."
            )
        ),

        // ============================= COMIDAS =============================
        recipe(
            title = "Enchiladas verdes con pollo",
            description = "Tortillas rellenas de pollo bañadas en salsa verde de tomate y chile serrano, con crema y queso fresco. Un clásico de la comida mexicana.",
            minutes = 35,
            difficulty = "Media",
            mealType = "Comida",
            dayOfWeek = "Lunes",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "200 g de pollo deshebrado",
                "6 piezas de tomate verde",
                "2 piezas de chile serrano",
                "1/4 pieza de cebolla",
                "1 diente de ajo",
                "3 cucharadas de crema",
                "80 g de queso fresco"
            ),
            steps = listOf(
                "Cuece 200 g de pollo con 1/4 de cebolla y sal durante 18 minutos; deshébralo.",
                "Hierve 6 tomates verdes, 2 chiles serranos, 1/4 de cebolla y 1 diente de ajo durante 10 minutos.",
                "Licúa lo hervido con 1/2 taza de agua y cocina la salsa con 1 cucharada de aceite durante 6 minutos.",
                "Pasa 4 tortillas de maíz por aceite caliente 15 segundos por lado para suavizarlas.",
                "Rellena cada tortilla con pollo, enróllalas y colócalas en el plato.",
                "Baña con salsa verde y corona con 3 cucharadas de crema y 80 g de queso fresco."
            )
        ),
        recipe(
            title = "Tacos dorados de papa con crema y queso",
            description = "Tacos de tortilla de maíz rellenos de papa sazonada, fritos hasta quedar dorados y servidos con lechuga, crema y queso fresco.",
            minutes = 30,
            difficulty = "Media",
            mealType = "Comida",
            dayOfWeek = "Martes",
            ingredients = listOf(
                "6 piezas de tortilla de maíz",
                "3 piezas de papa",
                "80 g de queso fresco",
                "3 cucharadas de crema",
                "1/2 pieza de lechuga",
                "2 piezas de jitomate",
                "2 tazas de aceite"
            ),
            steps = listOf(
                "Cuece 3 papas en agua con sal durante 15 minutos; haz un puré y sazona.",
                "Calienta 6 tortillas de maíz en el comal 30 segundos por lado para que no se rompan.",
                "Rellena cada tortilla con puré de papa y enróllala.",
                "Calienta 2 tazas de aceite a 180 °C y fríe los tacos 3 minutos por lado hasta dorar.",
                "Escurre sobre papel absorbente durante 2 minutos.",
                "Sirve con lechuga, jitomate, 3 cucharadas de crema y 80 g de queso fresco."
            )
        ),
        recipe(
            title = "Alambre de res con quesillo",
            description = "Tiras de bistec salteadas con chorizo, cebolla y chile jalapeño, cubiertas de quesillo fundido y servidas con tortillas de harina.",
            minutes = 30,
            difficulty = "Media",
            mealType = "Comida",
            dayOfWeek = "Miércoles",
            ingredients = listOf(
                "250 g de bistec de res",
                "100 g de chorizo",
                "150 g de quesillo",
                "1/2 pieza de cebolla",
                "2 piezas de chile jalapeño",
                "4 piezas de tortilla de harina"
            ),
            steps = listOf(
                "Corta 250 g de bistec en tiras y sazona con sal y pimienta.",
                "Sofríe 100 g de chorizo en una sartén a fuego medio durante 5 minutos hasta que suelte su grasa.",
                "Agrega el bistec y cocina 6 minutos hasta que se dore.",
                "Incorpora 1/2 cebolla y 2 chiles jalapeños en tiras; cocina 4 minutos.",
                "Cubre con 150 g de quesillo y tapa la sartén 3 minutos hasta que se derrita.",
                "Sirve con 4 tortillas de harina calientes."
            )
        ),
        recipe(
            title = "Bistec a la mexicana con arroz y frijoles",
            description = "Bistec en tiras guisado con jitomate, cebolla y chile serrano, acompañado de arroz rojo y frijoles de la olla. Comida completa y casera.",
            minutes = 35,
            difficulty = "Media",
            mealType = "Comida",
            dayOfWeek = "Jueves",
            ingredients = listOf(
                "250 g de bistec de res",
                "3 piezas de jitomate",
                "1/2 pieza de cebolla",
                "1 pieza de chile serrano",
                "1 taza de arroz",
                "1 taza de frijoles de la olla",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Corta 250 g de bistec en tiras y sazona con sal y pimienta.",
                "Sofríe la carne en 1 cucharada de aceite a fuego alto durante 5 minutos; reserva.",
                "En la misma sartén acitrona 1/2 cebolla, 3 jitomates y 1 chile serrano durante 5 minutos.",
                "Regresa la carne y mezcla a fuego medio durante 4 minutos.",
                "Calienta 1 taza de arroz y 1 taza de frijoles durante 5 minutos.",
                "Sirve la carne con el arroz y los frijoles."
            )
        ),
        recipe(
            title = "Torta completa de jamón, quesillo y chorizo",
            description = "Bolillo relleno de jamón, quesillo, chorizo, aguacate, jitomate y lechuga. La torta mexicana completa para una comida rápida y contundente.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Comida",
            dayOfWeek = "Viernes",
            ingredients = listOf(
                "1 pieza de bolillo",
                "2 rebanadas de jamón",
                "80 g de quesillo",
                "50 g de chorizo",
                "1/2 pieza de aguacate",
                "1 pieza de jitomate",
                "2 hojas de lechuga",
                "1 cucharada de mayonesa"
            ),
            steps = listOf(
                "Parte 1 bolillo por la mitad y úntale 1 cucharada de mayonesa.",
                "Calienta 50 g de chorizo en un comal durante 4 minutos.",
                "Agrega 2 rebanadas de jamón y 80 g de quesillo; tapa 2 minutos hasta derretir.",
                "Coloca el relleno dentro del bolillo.",
                "Agrega 1/2 aguacate, 1 jitomate y 2 hojas de lechuga.",
                "Cierra la torta, presiónala ligeramente y sirve de inmediato."
            )
        ),
        recipe(
            title = "Calabacitas con elote, crema y queso",
            description = "Calabacitas tiernas guisadas con elote, jitomate y cebolla, terminadas con crema y queso fresco. Un guiso vegetariano lleno de sabor.",
            minutes = 25,
            difficulty = "Fácil",
            mealType = "Comida",
            dayOfWeek = "Sábado",
            ingredients = listOf(
                "3 piezas de calabacita",
                "1 pieza de elote",
                "3 cucharadas de crema",
                "80 g de queso fresco",
                "1/4 pieza de cebolla",
                "2 piezas de jitomate",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Lava y corta 3 calabacitas en cubos; desgrana 1 elote.",
                "Sofríe 1/4 de cebolla y 2 jitomates picados en 1 cucharada de aceite durante 5 minutos.",
                "Agrega las calabacitas y el elote; cocina a fuego medio 8 minutos.",
                "Sazona y cocina 3 minutos más hasta que las verduras estén suaves.",
                "Incorpora 3 cucharadas de crema y mezcla 2 minutos.",
                "Sirve con 80 g de queso fresco espolvoreado."
            )
        ),
        recipe(
            title = "Arroz con pollo a la mexicana",
            description = "Arroz rojo cocido con pollo, zanahoria y chícharos. Un plato único, colorido y rendidor que recuerda a la comida de casa.",
            minutes = 45,
            difficulty = "Media",
            mealType = "Comida",
            dayOfWeek = "Domingo",
            ingredients = listOf(
                "300 g de pollo",
                "1 taza de arroz",
                "1 pieza de zanahoria",
                "1/2 taza de chícharos",
                "1/4 pieza de cebolla",
                "2 piezas de jitomate",
                "2 tazas de caldo de pollo",
                "1 cucharada de aceite"
            ),
            steps = listOf(
                "Sazona 300 g de pollo y dóralo en una olla con 1 cucharada de aceite durante 6 minutos; reserva.",
                "Acitrona 1/4 de cebolla y 2 jitomates picados en la misma olla durante 4 minutos.",
                "Agrega 1 taza de arroz lavado y sofríe 2 minutos.",
                "Incorpora el pollo, 1 zanahoria en cubos y 2 tazas de caldo de pollo.",
                "Tapa y cocina a fuego bajo 20 minutos.",
                "Agrega 1/2 taza de chícharos y deja reposar 5 minutos antes de servir."
            )
        ),
        recipe(
            title = "Tostadas de atún con lechuga y aguacate",
            description = "Tostadas crujientes con atún mezclado con mayonesa y limón, lechuga, jitomate y aguacate. Una comida fresca y rápida.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Comida",
            dayOfWeek = "Martes",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "1 lata de atún (140 g)",
                "1/2 pieza de lechuga",
                "2 piezas de jitomate",
                "1/4 pieza de cebolla",
                "1/2 pieza de aguacate",
                "2 cucharadas de mayonesa",
                "1 pieza de limón"
            ),
            steps = listOf(
                "Tuesta 4 tortillas de maíz en el comal 2 minutos por lado hasta que estén crujientes.",
                "Escurre 1 lata de atún y mézclala con 2 cucharadas de mayonesa y el jugo de 1 limón.",
                "Lava y corta 1/2 lechuga, 2 jitomates y 1/4 de cebolla.",
                "Unta el atún sobre cada tostada.",
                "Agrega lechuga, jitomate, cebolla y 1/2 aguacate en rebanadas.",
                "Sirve de inmediato para que la tostada no se ablande."
            )
        ),

        // ============================== CENAS ==============================
        recipe(
            title = "Sincronizadas de jamón, quesillo y aguacate",
            description = "Tortillas de harina rellenas de jamón y quesillo fundido, acompañadas de aguacate y jitomate. Una cena rápida y muy mexicana.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Lunes",
            ingredients = listOf(
                "4 piezas de tortilla de harina",
                "2 rebanadas de jamón",
                "100 g de quesillo",
                "1/2 pieza de aguacate",
                "1 pieza de jitomate",
                "2 cucharadas de crema"
            ),
            steps = listOf(
                "Calienta 1 tortilla de harina en el comal durante 30 segundos.",
                "Coloca 2 rebanadas de jamón y 100 g de quesillo en una mitad.",
                "Dobla la tortilla y dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas.",
                "Sirve con 1/2 aguacate, 1 jitomate y 2 cucharadas de crema."
            )
        ),
        recipe(
            title = "Quesadillas de quesillo con champiñones",
            description = "Quesadillas de tortilla de maíz con quesillo fundido y champiñones salteados con cebolla. Ligeras, sabrosas y perfectas para la cena.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Martes",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "150 g de quesillo",
                "150 g de champiñones",
                "1/4 pieza de cebolla",
                "1 ramita de cilantro",
                "2 cucharadas de salsa"
            ),
            steps = listOf(
                "Lava y rebana 150 g de champiñones.",
                "Saltea los champiñones con 1/4 de cebolla en 1 cucharada de aceite durante 6 minutos.",
                "Calienta 1 tortilla de maíz y coloca 150 g de quesillo en una mitad.",
                "Agrega los champiñones y dobla la tortilla.",
                "Dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas y sirve con cilantro y 2 cucharadas de salsa."
            )
        ),
        recipe(
            title = "Tostadas de frijol con pollo, lechuga, crema y queso",
            description = "Tostadas untadas con frijoles refritos, pollo deshebrado, lechuga fresca, crema y queso. Una cena completa con textura crujiente.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Miércoles",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles refritos",
                "150 g de pollo deshebrado",
                "1/2 pieza de lechuga",
                "3 cucharadas de crema",
                "80 g de queso fresco",
                "2 cucharadas de salsa"
            ),
            steps = listOf(
                "Tuesta 4 tortillas de maíz en el comal 2 minutos por lado.",
                "Calienta 1 taza de frijoles refritos y úntalos sobre las tostadas.",
                "Coloca 150 g de pollo deshebrado encima.",
                "Agrega 1/2 lechuga picada.",
                "Corona con 3 cucharadas de crema, 80 g de queso fresco y 2 cucharadas de salsa."
            )
        ),
        recipe(
            title = "Burritos de frijol con queso y chorizo",
            description = "Tortillas de harina rellenas de frijoles, queso manchego, chorizo y huevo. Una cena contundente que se prepara en minutos.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Jueves",
            ingredients = listOf(
                "4 piezas de tortilla de harina",
                "1 taza de frijoles refritos",
                "100 g de queso manchego",
                "80 g de chorizo",
                "2 piezas de huevo",
                "2 cucharadas de crema"
            ),
            steps = listOf(
                "Calienta 1 taza de frijoles refritos y machácalos durante 5 minutos.",
                "Fríe 80 g de chorizo durante 4 minutos y revuélvelo con 2 huevos batidos.",
                "Calienta 4 tortillas de harina en el comal 30 segundos por lado.",
                "Rellena con frijol, 100 g de queso manchego, chorizo y huevo.",
                "Enrolla los burritos y dóralos 2 minutos por lado en el comal.",
                "Sirve con 2 cucharadas de crema al gusto."
            )
        ),
        recipe(
            title = "Sándwich ligero de panela asada y jamón",
            description = "Pan de caja tostado con panela asada, jamón, lechuga, jitomate y un toque de mayonesa. Cena ligera y rápida.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Viernes",
            ingredients = listOf(
                "2 rebanadas de pan de caja",
                "100 g de queso panela",
                "2 rebanadas de jamón",
                "2 hojas de lechuga",
                "1 pieza de jitomate",
                "1 cucharada de mayonesa"
            ),
            steps = listOf(
                "Asa 100 g de queso panela en un comal durante 3 minutos por lado.",
                "Tuesta 2 rebanadas de pan de caja 2 minutos.",
                "Úntales 1 cucharada de mayonesa.",
                "Arma el sándwich con 2 rebanadas de jamón, la panela, 2 hojas de lechuga y 1 jitomate.",
                "Cierra y corta por la mitad para servir."
            )
        ),
        recipe(
            title = "Molletes dulces con miel y canela",
            description = "Bolillo tostado con miel, canela y plátano, acompañado de un vaso de leche. Una cena dulce y sencilla para cerrar el día.",
            minutes = 12,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Sábado",
            ingredients = listOf(
                "1 pieza de bolillo",
                "2 cucharadas de miel",
                "1 cucharadita de canela",
                "1 pieza de plátano",
                "1 taza de leche"
            ),
            steps = listOf(
                "Parte 1 bolillo por la mitad y tuéstalo 3 minutos.",
                "Úntale 2 cucharadas de miel mientras está caliente.",
                "Espolvorea 1 cucharadita de canela.",
                "Coloca 1 plátano en rodajas encima.",
                "Sirve con 1 taza de leche fría o caliente."
            )
        ),
        recipe(
            title = "Quesadillas de flor de calabaza",
            description = "Quesadillas de tortilla de maíz con flor de calabaza y quesillo, sazonadas con epazote. Un clásico vegetariano de temporada.",
            minutes = 20,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Domingo",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "150 g de flor de calabaza",
                "120 g de quesillo",
                "1/4 pieza de cebolla",
                "2 ramitas de epazote",
                "2 cucharadas de salsa"
            ),
            steps = listOf(
                "Lava y limpia 150 g de flor de calabaza.",
                "Sofríe la flor con 1/4 de cebolla y 2 ramitas de epazote durante 5 minutos.",
                "Calienta 1 tortilla y coloca 120 g de quesillo en una mitad.",
                "Agrega la flor de calabaza y dobla la tortilla.",
                "Dora 2 minutos por lado hasta que el queso se derrita.",
                "Repite con las otras 3 tortillas y sirve con 2 cucharadas de salsa."
            )
        ),
        recipe(
            title = "Enfrijoladas sencillas con queso",
            description = "Tortillas de maíz bañadas en salsa de frijol, con cebolla, crema y queso fresco. La cena más sencilla y reconfortante de la semana.",
            minutes = 15,
            difficulty = "Fácil",
            mealType = "Cena",
            dayOfWeek = "Lunes",
            ingredients = listOf(
                "4 piezas de tortilla de maíz",
                "1 taza de frijoles cocidos",
                "80 g de queso fresco",
                "1/4 pieza de cebolla",
                "3 cucharadas de crema",
                "1 pieza de chile serrano"
            ),
            steps = listOf(
                "Licúa 1 taza de frijoles cocidos con 1/2 taza de agua y 1 chile serrano.",
                "Calienta la salsa en una olla con 1 cucharada de aceite durante 5 minutos.",
                "Calienta 4 tortillas de maíz en el comal 30 segundos por lado.",
                "Pasa las tortillas por la salsa hasta cubrirlas.",
                "Colócalas en el plato y baña con más salsa.",
                "Corona con cebolla picada, 3 cucharadas de crema y 80 g de queso fresco."
            )
        )
    )
}
