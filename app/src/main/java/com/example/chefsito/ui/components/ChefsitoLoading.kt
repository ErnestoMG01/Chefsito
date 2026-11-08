package com.example.chefsito.ui.components

import android.webkit.WebView
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.chefsito.ui.screens.auth.ChefHatIcon
import com.example.chefsito.ui.theme.*

@Composable
fun ChefsitoLoading(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.displayZoomControls = false

                webViewClient = android.webkit.WebViewClient()

                loadUrl("file:///android_asset/loading.html")
            }
        }
    )
}

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
private fun SingleSteamLine(
    offsetX: Dp,
    height: Dp,
    delayMillis: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "steam_$delayMillis")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steamProgress"
    )

    val alpha = when {
        progress < 0.35f -> (progress / 0.35f) * 0.7f
        else -> (1f - (progress - 0.35f) / 0.65f) * 0.7f
    }.coerceIn(0f, 0.7f)

    val translateY = (4 - (progress * 20)).dp
    val scaleY = 0.6f + (progress * 0.5f)

    Box(
        modifier = modifier
            .offset(x = offsetX, y = translateY)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleY = scaleY
            }
            .width(5.dp)
            .height(height)
            .background(VerdeFresco, CircleShape)
    )
}

@Composable
fun ChefsitoLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 148.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner_transition")

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinRotation"
    )

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobbleRotation"
    )

    val plateSize = size * 0.53f
    val hatSize = plateSize * 0.62f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (size * 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            SingleSteamLine(offsetX = (-28).dp, height = 14.dp, delayMillis = 0)
            SingleSteamLine(offsetX = 0.dp, height = 18.dp, delayMillis = 250)
            SingleSteamLine(offsetX = 28.dp, height = 14.dp, delayMillis = 500)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(spinRotation)
        ) {
            val strokeWidth = 6.dp.toPx()

            drawCircle(
                color = TintaComic.copy(alpha = 0.08f),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = NaranjaCoral,
                startAngle = -90f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = AmarilloMostaza,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(plateSize)
                .scale(breatheScale)
                .border(2.5.dp, TintaComic.copy(alpha = 0.12f), CircleShape)
                .clip(CircleShape)
                .background(BlancoPuro),
            contentAlignment = Alignment.Center
        ) {
            ChefHatIcon(
                size = hatSize,
                modifier = Modifier.rotate(wobbleRotation)
            )
        }
    }
}

@Composable
fun ChefsitoLoadingStage(
    modifier: Modifier = Modifier,
    title: String = "ChefBolsillo — Cargando",
    subtitle: String = "Un momento por favor..."
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ChefsitoLoadingSpinner(size = 148.dp)

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic
            )
        }

        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TintaComic.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChefsitoLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String = "ChefBolsillo — Cargando",
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = modifier
                .padding(16.dp)
                .border(2.5.dp, TintaComic, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCrema)
        ) {
            Box(
                modifier = Modifier.padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                ChefsitoLoadingStage(
                    title = message,
                    subtitle = "Un momento por favor..."
                )
            }
        }
    }
}
