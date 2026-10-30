package com.example.chefsito.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.components.ChefsitoProgressBar
import com.example.chefsito.ui.screens.auth.ChefHatIcon
import com.example.chefsito.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    val logoScale = remember { Animatable(0.5f) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loadingMessage by remember { mutableStateOf("Cargando ingredientes...") }

    LaunchedEffect(Unit) {
        // Animación de pulso de entrada para el Logo
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )

        // Simulación de carga en tiempo real de 0% a 100%
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
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculos de adorno de fondo estilo cómic
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Contenedor animado del Logo
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(110.dp)
                    .border(3.dp, TintaComic, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(AmarilloMostaza),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(BlancoPuro),
                    contentAlignment = Alignment.Center
                ) {
                    ChefHatIcon(size = 56.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MiMenú",
                fontSize = 38.sp,
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

            Spacer(modifier = Modifier.height(48.dp))

            // Barra de Carga Reutilizable Estilo Cómic
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
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    ChefsitoTheme {
        SplashScreen()
    }
}
