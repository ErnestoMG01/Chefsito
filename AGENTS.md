# Contexto y Memoria del Proyecto: Chefsito (Sin IA + Auth Local)

## 1. Objetivo Principal (Entrega Inmediata)
Aplicación Android `com.example.chefsito` 100% local (sin IA) con:
1. **Generación de recetas por coincidencia local:** basada en la despensa del usuario y en los **costos que él mismo captura** por ingrediente, consultando `ChefsitoDatabase` (Room) con un catálogo pre-cargado de recetas mexicanas.
2. **Autenticación y Registro de Usuarios:** flujo completo de Login/Registro con persistencia en Room, contraseñas hasheadas y sesión persistente en `SharedPreferences`.

## 2. Arquitectura Final Detectada
- **Paquete base:** `com.example.chefsito`
- **UI:** Jetpack Compose + Material 3 (tema cómic, paleta en `ui/theme/Color.kt`).
- **Navegación:** Navigation Compose con rutas centralizadas en `ui/navigation/AppNavigation.kt`.
  - Rutas: `splash`, `auth`, `home`, `plan`, `favorites`, `profile`, `pantry`, `recipe/{recipeId}`, `community_recipe`.
  - El detalle de receta local y el detalle de receta de la API ocultan la barra inferior.
- **Persistencia:** Room 2.8.4 + KSP. `ChefsitoDatabase` (versión **4**) con:
  - `IngredientEntity` → `IngredientDao` (`userCost: Double` = costo real capturado por el usuario).
  - `RecipeEntity` → `RecipeDao` (`description`, `mealType`, `dayOfWeek`, `isFavorite`).
  - `UserEntity` → `UserDao`.
  - `fallbackToDestructiveMigration(dropAllTables = true)` para demo/entrega.
- **Autenticación (Local-First):** `AuthRepository` / `AuthRepositoryImpl` sobre `UserDao`. Contraseñas con `PasswordHasher` (SHA-256). Sin Retrofit/Firebase.
- **Sesión:** `SessionManager` (SharedPreferences) guarda `userId`, `userName`, `userEmail`, `isLoggedIn` y token local. `logout()` borra solo claves de sesión.
- **Datos por usuario:** presupuesto, foto y datos de perfil en `chefsito_prefs_<userId>`. Despensa y catálogo son compartidos en Room.
- **Motor de costos (`data/domain/RecipeCosting.kt`):**
  - Cada receta guarda ingredientes como líneas `"Nombre|CostoEstimado"`.
  - `breakdown(recipe, pantry, budget)` separa ingredientes **que el usuario ya tiene** (usa `userCost`) de los **faltantes** (usa costo estimado).
  - `totalCost = ownedCost + missingCost`; `fitsBudget = totalCost <= budget`.
  - Coincidencia tolerante a mayúsculas, acentos y nombres compuestos.
- **Recomendación (`RecipeRepositoryImpl`):** `recommendRecipes` conserva la mejor opción por comida y `recommendRecipeOptions(pantry, budget, dayOfWeek, maxPerMeal)` devuelve hasta 3 alternativas por Desayuno, Comida y Cena. Prioriza más ingredientes en despensa → cabe en presupuesto → menor costo faltante. Filtra por `dayOfWeek` cuando hay recetas asignadas a ese día y completa con el resto del catálogo para garantizar variedad.
- **ViewModel (`RecipeViewModel`):** `generateRecipe(budget, planType, dayOfWeek)`, `recommendations`, `mealOptions: List<MealOptionGroup>`, `weeklyPlans: List<DayPlan>`, `planType`, `selectedDay`, `toggleFavorite(recipeId)`.
- **Seed:** `RecipeSeed` contiene **24 recetas mexicanas completas** (8 Desayuno, 8 Comida, 8 Cena) con `description`, `dayOfWeek`, ingredientes con costo estimado y `steps` detallados. `ChefsitoApplication.onCreate()` llama `recipeRepository.seedIfEmpty()`.
- **UI de recetas:**
  - `PlanScreen`: selector Día/Semana, presupuesto, alternativas horizontales agrupadas por Desayuno/Comida/Cena con badge, desglose de costos, ajuste al presupuesto, corazón y “Ver receta”; la vista Semana muestra un resumen compacto de los 7 días.
  - `FavoritesScreen`: solo recetas con `isFavorite = 1` (consume `uiState.favoriteRecipes`), badge de comida, corazón y “Ver receta”.
  - `RecipeDetailScreen`: resumen (descripción, ingredientes con costos, ajuste al presupuesto) + botón **Comenzar** + modo paso a paso interactivo con progreso y finalización.
  - `CommunityRecipeDetailScreen`: detalle de una receta de la API con imagen, título, categoría, ingredientes e instrucciones.
- **API externa:** `CommunityViewModel` expone `uiState` (Loading/Success/Error) y `selectedRecipe`; `HomeScreen` muestra “Recetas Inspiradoras de la Red” con tarjetas clicables.
- **Despensa (`PantryScreen`):** captura `userCost` por ingrediente, edición de costo, total de despensa y campo de **Presupuesto objetivo**.
- **Compilación:** Gradle Kotlin DSL + KSP. Sin SDK de Gemini ni `GEMINI_API_KEY`.

## 3. Reglas de Memoria para Agentes (Opencode)
- **SIEMPRE** antes de empezar una tarea o tras cambiar de modelo en el chat, lee este archivo (`AGENTS.md`) para conocer el estado actual.
- **SIEMPRE** que completes uno de los módulos de la lista de tareas, actualiza la sección correspondiente marcando con `[x]` lo completado y anotando qué archivos modificaste.

## 4. Progreso Iteración 1 (Completada)

- [x] **Tarea 1: Limpieza del SDK/Cliente de Gemini.**
  - Eliminados: `data/remote/AiRecipeService.kt`, `data/remote/GeminiRecipeServiceImpl.kt`, `data/remote/dto/AiRecipeDto.kt`, `data/repository/AiRecipeRepository.kt`, `ui/recipe/RecipeScreenRoute.kt`.
  - `app/build.gradle.kts`: quitada la lectura de `local.properties`, el `buildConfigField("GEMINI_API_KEY")` y la dependencia `com.google.ai.client.generativeai`.
  - `ChefsitoApplication.kt`: eliminados `aiRecipeService` y `aiRecipeRepository`.
  - Verificado con búsqueda: no quedan referencias a `generativeai`, `AiRecipe`, `GEMINI` ni `generateAiRecipe`.

- [x] **Tarea 2: Backend/Persistencia de Usuarios.**
  - Nuevos: `data/local/entity/UserEntity.kt`, `data/local/dao/UserDao.kt`, `data/repository/AuthRepository.kt`, `data/repository/AuthRepositoryImpl.kt`, `utils/PasswordHasher.kt`.
  - `ChefsitoDatabase.kt`: `UserEntity` registrada y `fallbackToDestructiveMigration(dropAllTables = true)`.
  - `SessionManager.kt`: `userId`, `userName`, `userEmail`, `isLoggedIn`, `logout()` y compatibilidad con `isAuthenticated`/`clearSession`.
  - `ChefViewModel.kt`: preferencias aisladas por usuario (`chefsito_prefs_<userId>`).

- [x] **Tarea 3: Pantallas UI de Login/Registro + Navegación.**
  - `AuthViewModel.kt`: `StateFlow`, validación de email/contraseña, duplicados, `isLoading`, errores y `authenticatedUser`.
  - `AuthScreen.kt`: conectada a `AuthRepository` vía `AuthViewModelFactory`, guarda sesión y navega con `onLoginSuccess`.
  - `MainActivity.kt`: logout limpia la sesión antes de navegar a Auth.
  - `HomeScreen.kt` / `ProfileScreen.kt`: usan nombre/correo de la sesión.

- [x] **Tarea 4: Motor local de recetas + Seed.**
  - `RecipeEntity.kt`, `RecipeDao.kt`, `RecipeRepository.kt`, `RecipeRepositoryImpl.kt`, `RecipeSeed.kt`.
  - `RecipeViewModel.kt` / `RecipeUiState.kt`: generación sin IA.
  - `ChefsitoApplication.kt`: seed automático al iniciar si la tabla está vacía.

- [x] **Tarea 5: Compilación limpia (`./gradlew assembleDebug`) sin errores.**
  - Resultado: **BUILD SUCCESSFUL**. APK en `app/build/outputs/apk/debug/app-debug.apk`.

## 5. Iteración 2: Correcciones de QA y Nuevas Reglas de Negocio

### 5.1 Lógica de Costos Definidos por el Usuario + Presupuesto
- [x] `IngredientEntity.price` reemplazado por `userCost: Double` (costo real pagado por el usuario).
- [x] `PantryScreen` permite capturar y editar el costo de cada ingrediente (“¿Cuánto pagaste?”).
- [x] `PantryScreen` incluye campo de **Presupuesto objetivo** compartido con Planificar (`ChefViewModel.updateBudget`).
- [x] El motor separa ingredientes que el usuario ya tiene (con `userCost`) de los faltantes (costo estimado).
- [x] El costo total de la receta se compara contra el presupuesto del día (`fitsBudget`).
- [x] Las tarjetas y el detalle muestran el desglose: ya tienes, te falta, costo total y ajuste al presupuesto.
- [x] Prioridad de recomendación: más ingredientes en despensa → cabe en presupuesto → menor costo faltante.
  - Archivos: `data/local/entity/IngredientEntity.kt`, `data/domain/RecipeCosting.kt`, `data/repository/RecipeRepositoryImpl.kt`, `ui/viewmodel/ChefViewModel.kt`, `ui/screens/pantry/PantryScreen.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/recipe/RecipeDetailScreen.kt`.

### 5.2 Categorización por Tiempo de Comida y Filtro por Día
- [x] `RecipeEntity.mealType` con valores exactos `"Desayuno"`, `"Comida"`, `"Cena"`.
- [x] `RecipeEntity.dayOfWeek` (Lunes–Domingo) para filtrar en Planificar.
- [x] Badge visible de Desayuno/Comida/Cena en recomendadas, favoritos y detalle.
- [x] Selector de día Lunes–Domingo en Planificar (por defecto el día actual).
- [x] Resultados agrupados por Desayuno, Comida y Cena para el día seleccionado.
  - Archivos: `data/local/entity/RecipeEntity.kt`, `data/local/RecipeSeed.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/favorites/FavoritesScreen.kt`, `ui/screens/recipe/RecipeDetailScreen.kt`.

### 5.3 Separación Estricta entre Planificar y Favoritos
- [x] Las recetas del seed nacen con `isFavorite = false`.
- [x] Corazón en cada tarjeta recomendada de Planificar.
- [x] `toggleFavorite(recipeId)` actualiza `isFavorite` en Room.
- [x] Favoritos consume **solo** `uiState.favoriteRecipes` (`SELECT * FROM recipes WHERE isFavorite = 1`).
- [x] Se eliminó el botón de “buscar recetas” de Favoritos para evitar mezclar resultados del plan.
- [x] Quitar el corazón elimina la receta de Favoritos.
  - Archivos: `ui/recipe/RecipeViewModel.kt`, `MainActivity.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/favorites/FavoritesScreen.kt`, `data/local/dao/RecipeDao.kt`.

### 5.4 Flujo “Ver Receta” → “Comenzar” (Paso a Paso)
- [x] Ruta `recipe/{recipeId}` y navegación desde Planificar y Favoritos.
- [x] Pantalla de resumen: nombre, badge de comida, descripción, ingredientes con costos y ajuste al presupuesto.
- [x] Botón destacado **Comenzar**.
- [x] Modo paso a paso numerado e interactivo: avanza/retrocede, barra de progreso, resumen de pasos y pantalla de finalización “¡Buen provecho!”.
  - Archivos: `ui/screens/recipe/RecipeDetailScreen.kt`, `ui/navigation/AppNavigation.kt`, `MainActivity.kt`.

### 5.5 Catálogo Mexicano Completo
- [x] `RecipeSeed` con **24 recetas completas** (8 Desayuno, 8 Comida, 8 Cena).
- [x] Ingredientes mexicanos: tortilla de maíz/harina, frijol, jamón, quesos, huevo, arroz, bolillo, jitomate, cebolla, chile, aguacate, crema, pollo, bistec, atún, nopales, papa, chorizo, etc.
- [x] Cada receta incluye `description`, `dayOfWeek`, ingredientes con costo estimado y `steps` detallados.
- [x] Distribución por día que garantiza Desayuno + Comida + Cena en cada día de la semana.
  - Archivos: `data/local/RecipeSeed.kt`.

### 5.6 Compilación y Memoria
- [x] `ChefsitoDatabase` subido a **versión 3** con `.fallbackToDestructiveMigration(dropAllTables = true)` activo.
- [x] `./gradlew assembleDebug` ejecutado con **0 errores** (solo advertencias menores corregidas).
- [x] APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
- [x] `AGENTS.md` actualizado con el resumen de la Iteración 2.

## 6. Cierre de Rúbrica, Correcciones UI y 3 Funcionalidades

### 6.1 Funcionalidad 1: Galería / Imagen de Perfil (conservada y persistida)
- [x] Se conservó la selección de imagen desde galería con `ActivityResultContracts.GetContent` en `ProfileScreen`.
- [x] `ImageUtils.saveImageToInternalStorage` copia la imagen a `filesDir` con nombre único.
- [x] La ruta se guarda en las preferencias del usuario (`chefsito_prefs_<userId>`) y ahora también en `SessionManager`.
- [x] `SessionManager` expone `getProfileImagePath`, `saveProfileImagePath` y `clearProfileImagePath`.
- [x] La foto se muestra en `ProfileScreen` y en el avatar del header de `HomeScreen`.
- [x] `ChefViewModel.updateProfileImage` sincroniza la ruta con `SessionManager`.
  - Archivos: `utils/SessionManager.kt`, `ui/viewmodel/ChefViewModel.kt`, `ui/screens/profile/ProfileScreen.kt`, `ui/screens/home/HomeScreen.kt`, `utils/ImageUtils.kt`.

### 6.2 Funcionalidad 2: Audio con TextToSpeech Nativo
- [x] Motor `android.speech.tts.TextToSpeech` configurado en español `es-MX` (`Locale.forLanguageTag("es-MX")`).
- [x] Botón **🔊 Escuchar receta** en el resumen de la receta.
- [x] Botón **🔊 Escuchar paso** en el modo paso a paso; lee el paso actual.
- [x] El botón cambia a **⏹ Detener audio** mientras habla.
- [x] El audio se detiene al iniciar el paso a paso, finalizar, volver al resumen o salir.
- [x] El recurso se libera con `DisposableEffect { onDispose { stop(); shutdown() } }`.
  - Archivos: `ui/screens/recipe/RecipeDetailScreen.kt`.

### 6.3 Funcionalidad 3: Exportación de Receta a PDF
- [x] Botón **📄 Descargar Receta en PDF** en el detalle de la receta.
- [x] Generación con `android.graphics.pdf.PdfDocument` (nativo, sin librerías externas).
- [x] El PDF incluye título, tipo de comida, tiempo, dificultad, costo calculado según la despensa, ingredientes con cantidades y pasos completos.
- [x] Se guarda en `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)` con respaldo a `filesDir/documents`.
- [x] Se muestra un `Toast` con la ruta y se abre con `ACTION_VIEW`; si no hay visor, se comparte con `ACTION_SEND` mediante `FileProvider`.
- [x] Se agregó `FileProvider` al `AndroidManifest.xml` y `res/xml/file_paths.xml`.
  - Archivos: `utils/PdfExporter.kt`, `ui/screens/recipe/RecipeDetailScreen.kt`, `AndroidManifest.xml`, `res/xml/file_paths.xml`.

### 6.4 Conexión Operativa a API Externa (sin API Key)
- [x] Cliente nativo con `HttpURLConnection` + `org.json` (sin Retrofit ni dependencias nuevas).
- [x] Endpoint: `https://dummyjson.com/recipes?limit=8`.
- [x] `CommunityRecipeService` parsea nombre, cocina, dificultad, tiempo, ingredientes, instrucciones e imagen.
- [x] `CommunityViewModel` expone `StateFlow<CommunityUiState>` con **Loading**, **Success** y **Error**.
- [x] Sección visible en Inicio: **“🌐 Recetas de la Comunidad (Conectado vía API)”**.
- [x] En caso de error se muestra el mensaje, un botón **Reintentar** y un fallback local con recetas del seed.
- [x] `HomeScreen` ahora es desplazable y recibe el estado de la comunidad desde `MainActivity`.
  - Archivos: `data/remote/CommunityRecipe.kt`, `data/remote/CommunityRecipeService.kt`, `ui/viewmodel/CommunityViewModel.kt`, `ui/screens/home/HomeScreen.kt`, `MainActivity.kt`, `AndroidManifest.xml` (permiso INTERNET ya existente).

### 6.5 Correcciones de UI/UX
- [x] Se revisaron los botones: los colores se aplican con `shape` + `ButtonDefaults.buttonColors(...)`; no hay `Modifier.background(...)` fuera del contorno en botones.
- [x] `PantryScreen`: el nombre, categoría y costo ahora viven en una columna con `Modifier.weight(1f)`.
- [x] Los botones de editar y eliminar son compactos (32 dp) y ya no desbordan la fila.
- [x] `PlanScreen`: el bloque “Despensa registrada” usa `weight(1f)` y el botón **Editar** tiene `contentPadding` compacto.
- [x] `HomeScreen`: header con avatar real y contenido desplazable.
  - Archivos: `ui/screens/pantry/PantryScreen.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/home/HomeScreen.kt`, `ui/screens/recipe/RecipeDetailScreen.kt`.

### 6.6 Catálogo Mexicano Específico
- [x] `RecipeSeed` reescrito con **24 recetas** (8 Desayuno, 8 Comida, 8 Cena).
- [x] Ingredientes con cantidades y medidas claras (piezas, gramos, tazas, cucharadas, rebanadas).
- [x] Pasos de 5 a 7 con tiempos de cocción e instrucciones detalladas.
- [x] El costo estimado se resuelve por nombre base aunque el ingrediente incluya cantidad (`costFor` + claves ordenadas por longitud).
- [x] `RecipeCosting.findPantryMatch` sigue funcionando con las nuevas líneas con cantidad.
  - Archivos: `data/local/RecipeSeed.kt`, `data/domain/RecipeCosting.kt`.

### 6.7 Documentación y Compilación Final
- [x] `README.md` creado en la raíz con el cumplimiento de la rúbrica, arquitectura, funcionalidades, API, catálogo e instrucciones de instalación.
- [x] `ChefsitoDatabase` subido de versión 3 a **versión 4** con `fallbackToDestructiveMigration(dropAllTables = true)`.
- [x] `.\gradlew.bat assembleDebug` ejecutado con **BUILD SUCCESSFUL** y 0 errores.
- [x] APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
- [x] `AGENTS.md` actualizado con esta sección y todos los puntos marcados `[x]`.
  - Archivos: `README.md`, `data/local/ChefsitoDatabase.kt`, `AGENTS.md`.

## 7. Ajustes Finales de UI, API y Múltiples Opciones

### 7.1 Detalle de recetas provenientes de la API externa
- [x] `CommunityViewModel` expone `selectedRecipe: StateFlow<CommunityRecipe?>` y `selectRecipe()` / `clearSelection()`.
- [x] Nueva ruta `community_recipe` en `Routes` y en el `NavHost`.
- [x] Nueva pantalla `CommunityRecipeDetailScreen` con imagen (Coil), título, categoría, dificultad, tiempo, ingredientes e instrucciones.
- [x] Las tarjetas de la API son clicables, muestran miniatura y flecha, y abren el detalle al pulsarlas.
- [x] Sección de Inicio renombrada a **“Recetas Inspiradoras de la Red”** y conectada al `StateFlow` de `CommunityViewModel`.
  - Archivos: `ui/viewmodel/CommunityViewModel.kt`, `ui/navigation/AppNavigation.kt`, `ui/screens/community/CommunityRecipeDetailScreen.kt`, `ui/screens/home/HomeScreen.kt`, `MainActivity.kt`.

### 7.2 Limpieza de emojis e iconos Material
- [x] Se eliminaron emojis/símbolos de Inicio, Planificar, Detalle de receta, Perfil y Auth (`🌐`, `🍽`, `🌎`, `⚠`, `🛒`, `🔊`, `📄`, `▶`, `✓`, `⏹`, `🎉`, `⬆`, `→`, `←`).
- [x] Los títulos, botones, mensajes de error y ayudas usan texto plano profesional.
- [x] Los controles usan iconos nativos de Material Design (`Icons.Default.*` y `Icons.AutoMirrored.*`).
- [x] Se conservan únicamente los emojis justificados: 🍳 Desayuno, 🍲 Comida, 🌙 Cena y 👤 en el perfil.
  - Archivos: `ui/screens/home/HomeScreen.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/recipe/RecipeDetailScreen.kt`, `ui/screens/profile/ProfileScreen.kt`, `ui/screens/favorites/FavoritesScreen.kt`, `ui/screens/auth/AuthScreen.kt`.

### 7.3 Planificación simplificada Día / Semana
- [x] Se eliminó el selector largo de Lunes a Domingo.
- [x] El selector quedó en **Día** (recetas de hoy) y **Semana** (planeación compacta de 7 días).
- [x] `RecipeViewModel.generateRecipe` calcula el plan del día con alternativas y el plan semanal repartiendo el presupuesto entre 7 días.
- [x] `RecipeUiState` añade `planType`, `mealOptions: List<MealOptionGroup>` y `weeklyPlans: List<DayPlan>`.
- [x] `PlanScreen` muestra alternativas horizontales por comida en Día y un resumen tocable de 7 días en Semana.
  - Archivos: `ui/recipe/RecipeViewModel.kt`, `ui/recipe/RecipeUiState.kt`, `ui/screens/plan/PlanScreen.kt`.

### 7.4 Múltiples opciones de Desayuno, Comida y Cena
- [x] `RecipeRepository.recommendRecipeOptions(pantry, budget, dayOfWeek, maxPerMeal)` devuelve hasta **3 alternativas por comida**.
- [x] `recommendRecipes` se conserva delegando en la nueva función para no romper compatibilidad.
- [x] El orden prioriza más ingredientes en despensa → cabe en presupuesto → menor costo faltante → menor costo total.
- [x] Las opciones se muestran en listas horizontales desplazables agrupadas por Desayuno, Comida y Cena.
- [x] Cada tarjeta conserva corazón de favoritos, desglose de costos, ajuste al presupuesto y “Ver receta”.
  - Archivos: `data/repository/RecipeRepository.kt`, `data/repository/RecipeRepositoryImpl.kt`, `ui/recipe/RecipeViewModel.kt`, `ui/screens/plan/PlanScreen.kt`.

### 7.5 Compilación y documentación final
- [x] `.\gradlew.bat assembleDebug` ejecutado con **BUILD SUCCESSFUL** y sin errores.
- [x] APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
- [x] `README.md` actualizado con la API navegable, la limpieza de emojis, Día/Semana y las múltiples opciones.
- [x] `AGENTS.md` actualizado con esta sección y todos los puntos marcados `[x]`.
  - Archivos: `README.md`, `AGENTS.md`.

## 8. Limitaciones / Notas para la Demo
- No hay backend REST ni Firebase; autenticación y recetas son 100% locales (Room).
- La despensa y el catálogo son globales (no aislados por usuario); presupuesto/perfil sí son por usuario.
- Los costos estimados del seed son por porción y sirven como referencia para los ingredientes faltantes.
- La contraseña se hashea con SHA-256 sin salt (suficiente para demo académica, no para producción).
- El plan semanal reparte el presupuesto entre 7 días y muestra la mejor opción por comida; la vista Día ofrece hasta 3 alternativas por comida.
- La sección de la red requiere internet; sin conexión usa el fallback local y el botón **Reintentar**. Las recetas de la API se abren en un detalle con imagen e instrucciones.
- Al subir la base a la versión 4 se recrean las tablas y se vuelven a sembrar las 24 recetas (esperado para la demo).
- Archivos vacíos heredados de refactors previos en `data/entity/` y `data/ChefsitoDatabase.kt` no afectan la compilación.

## 9. Iteración 3 (Rama `feat/243873`): Despliegue en AWS, Multiusuario Concurrente y Catálogo Dinámico

### 9.1 Backend Cloud API (FastAPI + PostgreSQL en AWS RDS)
- [x] Creación del servidor backend en `backend/` con **FastAPI + SQLAlchemy 2.0 + Uvicorn (multi-worker)**.
- [x] Configuración de pool de conexiones (`DB_POOL_SIZE=15`, `DB_MAX_OVERFLOW=25`, `pool_pre_ping=True`) en `backend/app/database.py` para soportar múltiples usuarios simultáneos en Amazon RDS (`db.t3.micro`).
- [x] Esquema relacional multiusuario en `backend/app/models.py`: `users`, `recipes`, `user_pantry` (con restricción única `user_id + name`) y `user_favorites` (`user_id + recipe_id`).
- [x] Seguridad en `backend/app/security.py`: hash de contraseñas con **Bcrypt (12 rondas + salt)**, autenticación **JWT (`Bearer`)**, control de **Rate Limiting por IP** contra fuerza bruta y cabecera `X-Admin-Key` para administración del catálogo.
  - Archivos: `backend/requirements.txt`, `backend/.env.example`, `backend/app/database.py`, `backend/app/models.py`, `backend/app/security.py`, `backend/app/schemas.py`, `backend/app/main.py`.

### 9.2 Ingesta y Actualización Dinámica de Recetas
- [x] `backend/app/recipe_ingestor.py` incluye las 24 recetas base mexicanas + **12 nuevas recetas mexicanas en la nube (`AWS_CLOUD_CATALOG`)** = 36 recetas iniciales en el servidor.
- [x] Endpoint `POST /api/v1/recipes/sync-external`: consulta proveedores externos en vivo (**TheMealDB Mexican Cuisine API** y **DummyJSON Recipes**), traduce ingredientes al español, calcula el costo estimado en MXN por porción y los inserta en PostgreSQL sin duplicados (`external_id`).
- [x] Endpoints `POST /api/v1/recipes` y `POST /api/v1/recipes/bulk` para agregar o actualizar recetas en vivo sin recompilar la app Android.
  - Archivos: `backend/app/recipe_ingestor.py`, `backend/app/main.py`.

### 9.3 Adaptación Multiusuario y Cloud Sync en la App Android
- [x] `ChefsitoDatabase` subido a **versión 5**: `IngredientEntity` ahora incluye `userId`, `RecipeEntity` incluye `source` y `externalId`, y se añade `UserFavoriteEntity` (`user_favorites`) para aislar favoritos por usuario.
- [x] `SessionManager` expone `activeUserIdFlow`, almacena el token JWT de AWS y permite configurar en vivo la URL del servidor EC2 (`getApiBaseUrl` / `saveApiBaseUrl`).
- [x] Nuevo cliente `ChefsitoCloudApiService` (`data/remote/ChefsitoCloudApiService.kt`) para login/registro JWT, sincronización de despensa por usuario, favoritos y catálogo dinámico de recetas.
- [x] `PlanScreen` incorpora la tarjeta **Catálogo Dinámico** con los botones **"Actualizar de AWS"** (sincroniza e importa nuevas recetas externas) y **"+ Nueva receta"** (publica recetas nuevas en AWS RDS y en Room).
- [x] `ProfileScreen` incorpora la sección **Servidor AWS Cloud (EC2 + RDS)** para configurar la IP/DNS pública de la instancia EC2 del laboratorio de AWS.
  - Archivos: `data/local/ChefsitoDatabase.kt`, `data/local/entity/IngredientEntity.kt`, `data/local/entity/RecipeEntity.kt`, `data/local/entity/UserFavoriteEntity.kt`, `data/local/dao/IngredientDao.kt`, `data/local/dao/RecipeDao.kt`, `data/remote/ChefsitoCloudApiService.kt`, `data/repository/AuthRepositoryImpl.kt`, `data/repository/RecipeRepository.kt`, `data/repository/RecipeRepositoryImpl.kt`, `data/repository/IngredientRepositoryImpl.kt`, `utils/SessionManager.kt`, `ui/viewmodel/ChefViewModel.kt`, `ui/recipe/RecipeViewModel.kt`, `ui/recipe/RecipeUiState.kt`, `ui/screens/plan/PlanScreen.kt`, `ui/screens/profile/ProfileScreen.kt`, `ui/screens/auth/AuthScreen.kt`, `AndroidManifest.xml`.

### 9.4 Contenedores Docker y Guía de Laboratorio AWS
- [x] `backend/Dockerfile` con usuario no privilegiado (`chefsito`), `HEALTHCHECK` y workers concurrentes.
- [x] `docker-compose.yml` en la raíz con servicio `db` (`postgres:16-alpine`) y `api` (`chefsito_cloud_api`).
- [x] Script `deploy_aws_ec2.sh` con configuración de firewall `UFW` y despliegue automatizado con Docker Compose.
- [x] Guía completa `AWS_DEPLOYMENT_GUIDE.md` para AWS Academy Learner Lab (VPC, Security Groups `sg-chefsito-backend` y `sg-chefsito-rds`, RDS PostgreSQL privado, EC2 con `LabRole`).
  - Archivos: `backend/Dockerfile`, `docker-compose.yml`, `deploy_aws_ec2.sh`, `AWS_DEPLOYMENT_GUIDE.md`.
