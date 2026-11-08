package com.example.chefsito.ui.screens.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chefsito.ui.components.AuthLogoWebView
import com.example.chefsito.ui.components.ChefsitoLoading
import com.example.chefsito.ui.components.ChefsitoProgressBar
import com.example.chefsito.ui.navigation.Routes
import com.example.chefsito.ui.theme.*
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    onSplashFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    val isFirstRun = remember { SessionManager.isFirstRun(context) }

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loadingMessage by remember { mutableStateOf("Cargando ingredientes...") }

    LaunchedEffect(Unit) {
        if (isFirstRun) {
            // Simulación progresiva de carga de 0% a 100% para la primera vez
            val steps = 20
            for (i in 1..steps) {
                delay(100)
                currentProgress = i / steps.toFloat()
                loadingMessage = when {
                    currentProgress < 0.35f -> "Cargando ingredientes..."
                    currentProgress < 0.70f -> "Sincronizando recetas económicamente..."
                    currentProgress < 0.95f -> "Preparando el menú del día..."
                    else -> "¡Todo listo para cocinar!"
                }
            }
            delay(300)
            SessionManager.setFirstRunCompleted(context)
        } else {
            delay(450)
        }

        if (navController != null) {
            if (SessionManager.isAuthenticated(context)) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            } else {
                navController.navigate(Routes.AUTH) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        } else {
            onSplashFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema),
        contentAlignment = Alignment.Center
    ) {
        // Círculos de adorno de fondo estilo cómic (amarillo arriba-derecha, naranja abajo-izquierda)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = AmarilloMostaza.copy(alpha = 0.35f),
                radius = size.width * 0.40f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
                color = NaranjaCoral.copy(alpha = 0.20f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.10f, size.height * 0.85f)
            )
        }

        if (isFirstRun) {
            // Primera vez: Logo animado HTML + Título + Eslogan + Barra de Progreso + Mensajes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ChefsitoLoading(
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chefsito",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = TintaComic,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Come bien, gasta poco",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic.copy(alpha = 0.80f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                ChefsitoProgressBar(
                    progress = currentProgress,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    height = 24.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = loadingMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Entradas posteriores: Carga rápida del logo animado sin letras ni barra
            AuthLogoWebView(
                modifier = Modifier.size(220.dp),
                assetName = "logo_card.html"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    ChefsitoTheme {
        SplashScreen()
    }
}
