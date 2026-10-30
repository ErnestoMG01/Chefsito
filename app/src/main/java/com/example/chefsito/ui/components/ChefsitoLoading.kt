package com.example.chefsito.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chefsito.ui.screens.auth.ChefHatIcon
import com.example.chefsito.ui.theme.*

@Composable
fun ChefsitoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    showText: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .border(3.dp, TintaComic, RoundedCornerShape(size / 3))
                .clip(RoundedCornerShape(size / 3))
                .background(BlancoPuro),
            contentAlignment = Alignment.Center
        ) {
            ChefHatIcon(size = size * 0.65f)
        }

        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MiMenú",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TintaComic
            )
            Text(
                text = "Come bien, gasta poco",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun ChefsitoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 22.dp,
    showPercentage: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .border(2.5.dp, TintaComic, RoundedCornerShape(height / 2))
                .clip(RoundedCornerShape(height / 2))
                .background(BlancoPuro)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = clampedProgress)
                    .background(NaranjaCoral)
                    .border(
                        width = 1.dp,
                        color = TintaComic,
                        shape = RoundedCornerShape(height / 2)
                    )
            )
        }

        if (showPercentage) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${(clampedProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = TintaComic
                )
            }
        }
    }
}

@Composable
fun ChefsitoLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * 0.12f
            drawCircle(
                color = AmarilloMostaza,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = NaranjaCoral,
                startAngle = 0f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawCircle(
                color = TintaComic,
                style = Stroke(width = strokeWidth * 0.3f)
            )
        }
    }
}

@Composable
fun ChefsitoLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String = "Cargando...",
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = modifier
                .padding(16.dp)
                .border(2.5.dp, TintaComic, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCrema)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ChefsitoLoadingSpinner(size = 56.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
            }
        }
    }
}
