# Chefsito

Aplicación Android nativa (`com.example.chefsito`) para planear recetas mexicanas
con base en la despensa del usuario, el costo real que pagó por cada ingrediente y
un presupuesto objetivo. La app es **local-first** (Room + SharedPreferences) y
consume una **API externa** para mostrar recetas de la comunidad.

## Arquitectura

```
UI (Jetpack Compose + Material 3)
        │  observa StateFlow
        ▼
ViewModels (RecipeViewModel, ChefViewModel, AuthViewModel, CommunityViewModel)
        │
        ▼
Repositorios (RecipeRepository, IngredientRepository, AuthRepository)
        │
        ├── Room (ChefsitoDatabase v4)
        ├── SharedPreferences (presupuesto y perfil por usuario)
        ├── SessionManager (sesión activa y foto de perfil)
        └── API externa (DummyJSON vía HttpURLConnection)
```

- **Paquete base:** `com.example.chefsito`
- **Navegación:** Navigation Compose (`splash`, `auth`, `home`, `plan`, `favorites`,
  `profile`, `pantry`, `recipe/{recipeId}`, `community_recipe`).
- **Tema:** cómic mexicano, paleta definida en `ui/theme/Color.kt`.
- **Autenticación:** local con Room; contraseñas hasheadas con SHA-256
  (`PasswordHasher`). Es suficiente para la demo académica, no para producción.

### Persistencia

`ChefsitoDatabase` (versión **4**) contiene:

- `IngredientEntity` → despensa del usuario con `userCost` (lo que realmente pagó).
- `RecipeEntity` → catálogo local, favoritos, tipo de comida, día y pasos.
- `UserEntity` → usuarios registrados con contraseña hasheada.

`SessionManager` guarda `userId`, `userName`, `userEmail`, `isLoggedIn`, token
local y `profileImagePath`. El presupuesto, teléfono y datos de perfil viven en
`chefsito_prefs_<userId>`.

---

## Funcionalidades principales

### 1. Galería / imagen de perfil

- En **Mi Perfil** se elige una imagen con `ActivityResultContracts.GetContent`.
- `ImageUtils` copia la imagen al almacenamiento interno con un nombre nuevo.
- La ruta se guarda en las preferencias del usuario **y** en `SessionManager`.
- La foto se muestra en el perfil y en el avatar del header de **Inicio**.

### 2. Audio con `TextToSpeech` nativo

- Motor `android.speech.tts.TextToSpeech` configurado en `es-MX`.
- En el resumen de la receta: botón **Escuchar receta**.
- En el modo paso a paso: botón **Escuchar paso** que lee el paso actual.
- Mientras habla, el botón cambia a **Detener audio**.
- El recurso se libera con `DisposableEffect { onDispose { stop(); shutdown() } }`.

### 3. Exportación de receta a PDF

- Botón **Descargar Receta en PDF** en el detalle.
- Se usa `android.graphics.pdf.PdfDocument` (100% nativo, sin librerías externas).
- El PDF incluye título, tipo de comida, tiempo, dificultad, costo calculado
  según la despensa, ingredientes con cantidades y pasos completos.
- Se guarda en `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`.
- Se muestra un `Toast` con la ruta y se abre con `ACTION_VIEW`; si no hay visor,
  se ofrece compartir con `ACTION_SEND` mediante `FileProvider`.

### 4. Recetas Inspiradoras de la Red (API externa)

- Endpoint público sin API Key: `https://dummyjson.com/recipes?limit=8`.
- Cliente nativo con `HttpURLConnection` y `org.json` (sin Retrofit).
- Sección **“Recetas Inspiradoras de la Red”** en Inicio, conectada al
  `StateFlow` de `CommunityViewModel`.
- Cada tarjeta es clicable y abre `CommunityRecipeDetailScreen`, que muestra
  imagen, título, categoría, dificultad, tiempo, ingredientes e instrucciones
  devueltas por la red.
- Estados manejados con `StateFlow`:
  - **Loading:** indicador de carga.
  - **Success:** tarjetas con miniatura, nombre, cocina, dificultad y tiempo.
  - **Error:** mensaje, botón **Reintentar** y fallback local con recetas del seed.

### 5. Motor de costos y recomendación múltiple

- Cada ingrediente del seed se guarda como `"Cantidad + Nombre|CostoEstimado"`.
- `RecipeCosting.breakdown()` separa lo que el usuario **ya tiene** (costo real
  `userCost`) de lo que le **falta** (costo estimado).
- `totalCost = ownedCost + missingCost`; `fitsBudget = totalCost <= budget`.
- `recommendRecipeOptions()` devuelve hasta **3 alternativas por comida**
  (Desayuno, Comida y Cena), priorizando: más ingredientes en despensa → cabe en
  presupuesto → menor costo faltante.
- Las alternativas se muestran en listas horizontales desplazables agrupadas por
  tipo de comida, para que el usuario elija la que más se le antoje.

### 6. Planificación Día / Semana

- Se eliminó el selector largo de Lunes a Domingo.
- El selector **Día** evalúa las recetas de hoy y muestra las alternativas por
  Desayuno, Comida y Cena.
- El selector **Semana** reparte el presupuesto entre 7 días y presenta una
  planeación compacta con la mejor opción de cada comida por día.
- Tocar cualquier comida de la vista semanal abre el detalle completo de la receta.

---

## Catálogo 

`RecipeSeed` incluye **24 recetas completas**:

- 8 desayunos, 8 comidas y 8 cenas.
- Cada receta tiene descripción, día sugerido, dificultad, tiempo y entre 5 y 7
  pasos con tiempos de cocción.
- Ingredientes con cantidades claras, por ejemplo:
  - `3 piezas de tortilla de maíz`
  - `120 g de quesillo Oaxaca deshebrado`
  - `1 taza de frijoles refritos`
  - `2 rebanadas de jamón`
  - `150 g de pechuga de pollo deshebrada`

---

## Instalación y ejecución

### Requisitos

- Android Studio (versión compatible con AGP 9.3.0).
- JDK 17.
- Android SDK 35 (`compileSdk = 35`).
- Dispositivo o emulador con Android 7.0 (API 24) o superior.
- Conexión a internet para la sección de la comunidad (la app funciona sin
  internet usando el fallback local).

### Pasos

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo.
4. Registrar un usuario o iniciar sesión.
5. (Opcional) Capturar ingredientes y costos en **Mi Despensa**.
6. Generar el plan en **Configurar Plan** y abrir una receta.

### Compilar el APK

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

APK resultante:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Estructura relevante

```
app/src/main/java/com/example/chefsito/
├── MainActivity.kt                  # Navegación y Scaffold
├── ChefsitoApplication.kt           # Room, repositorios y seed
├── data/
│   ├── local/                       # Entidades, DAOs, base Room y RecipeSeed
│   ├── remote/                      # Cliente de la API externa
│   ├── repository/                  # Auth, ingredientes y recetas
│   └── domain/RecipeCosting.kt      # Motor de costos y matching
├── ui/
│   ├── navigation/                  # Rutas
│   ├── screens/                     # Splash, Auth, Home, Plan, Favoritos, Perfil, Despensa, Receta y Comunidad
│   ├── components/                  # BottomBar, botones, banners, loaders
│   ├── theme/                       # Colores y tipografía
│   ├── recipe/RecipeViewModel.kt    # Estado del plan y favoritos
│   └── viewmodel/                   # ChefViewModel, AuthViewModel, CommunityViewModel
└── utils/
    ├── ImageUtils.kt                # Copia de la foto de perfil
    ├── PdfExporter.kt               # Generación de PDF nativo
    ├── PasswordHasher.kt            # SHA-256
    └── SessionManager.kt            # Sesión y ruta de la foto
```

---

## Notas para la demo

- La despensa y el catálogo son compartidos; presupuesto y perfil son por usuario.
- Al subir la versión de la base a **4**, `fallbackToDestructiveMigration` recrea
  las tablas y vuelve a sembrar las 24 recetas (comportamiento esperado en demo).
- El plan semanal reparte el presupuesto entre 7 días y muestra la mejor opción
  de cada comida; la vista **Día** ofrece hasta 3 alternativas por comida.
- La sección de la red requiere internet; sin conexión muestra el fallback local
  y el botón **Reintentar**.
- La interfaz usa iconos nativos de Material Design y conserva solo los emojis de
  comida (🍳 Desayuno, 🍲 Comida, 🌙 Cena) y el avatar de perfil (👤).
