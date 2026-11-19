package com.example.chefsito.ui.screens.recipe

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.ui.components.ChefsitoBackButton
import com.example.chefsito.ui.theme.*
import com.example.chefsito.utils.PdfExporter
import java.util.Locale

/**
 * Detalle de receta:
 * 1. Resumen con descripción, ingredientes (con costos) y ajuste al presupuesto.
 * 2. Modo "Comenzar": instrucciones paso a paso, avanzando de una en una.
 */
@Composable
fun RecipeDetailScreen(
    recipe: RecipeEntity,
    pantry: List<IngredientEntity>,
    budget: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val breakdown = remember(recipe, pantry, budget) {
        RecipeCosting.breakdown(recipe, pantry, budget)
    }
    val steps = remember(recipe) { RecipeCosting.parseSteps(recipe) }

    var started by remember(recipe.id) { mutableStateOf(false) }
    var stepIndex by remember(recipe.id) { mutableStateOf(0) }
    var finished by remember(recipe.id) { mutableStateOf(false) }

    // ---- Funcionalidad 2: TextToSpeech nativo en español (es-MX) ----
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.forLanguageTag("es-MX")
                engine?.setSpeechRate(0.95f)
                engine?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                        }

                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                        }
                    }
                )
            }
        }
        ttsEngine = engine

        onDispose {
            engine?.stop()
            engine?.shutdown()
            ttsEngine = null
            isSpeaking = false
        }
    }

    val speakText: (String) -> Unit = { text ->
        val engine = ttsEngine
        if (engine == null) {
            Toast.makeText(
                context,
                "El motor de voz todavía se está preparando…",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            engine.stop()
            engine.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "chefsito-${System.currentTimeMillis()}"
            )
            isSpeaking = true
        }
    }

    val stopAudio: () -> Unit = {
        ttsEngine?.stop()
        isSpeaking = false
    }

    val exportPdf: () -> Unit = {
        val pdfFile = PdfExporter.exportRecipe(context, recipe, breakdown)
        if (pdfFile != null) {
            Toast.makeText(
                context,
                "PDF guardado en:\n${pdfFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
            PdfExporter.openPdf(context, pdfFile)
        } else {
            Toast.makeText(
                context,
                "No se pudo generar el PDF de la receta.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {
        if (!started) {
            RecipeSummary(
                recipe = recipe,
                breakdown = breakdown,
                isSpeaking = isSpeaking,
                onListen = { speakText("${recipe.title}. ${recipe.description}") },
                onStopListening = stopAudio,
                onExportPdf = exportPdf,
                onBack = onBack,
                onStart = {
                    stopAudio()
                    started = true
                    stepIndex = 0
                    finished = false
                }
            )
        } else {
            StepByStep(
                recipe = recipe,
                steps = steps,
                stepIndex = stepIndex,
                finished = finished,
                isSpeaking = isSpeaking,
                onListenStep = {
                    if (steps.isNotEmpty()) speakText(steps[stepIndex])
                },
                onStopListening = stopAudio,
                onPrevious = { if (stepIndex > 0) stepIndex-- },
                onNext = {
                    if (stepIndex < steps.lastIndex) {
                        stepIndex++
                    } else {
                        stopAudio()
                        finished = true
                    }
                },
                onBackToSummary = {
                    stopAudio()
                    started = false
                    finished = false
                },
                onClose = {
                    stopAudio()
                    onBack()
                }
            )
        }
    }
}

@Composable
private fun RecipeSummary(
    recipe: RecipeEntity,
    breakdown: RecipeCostBreakdown,
    isSpeaking: Boolean,
    onListen: () -> Unit,
    onStopListening: () -> Unit,
    onExportPdf: () -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ChefsitoBackButton(text = "Regresar", onBackClick = onBack)
        Spacer(modifier = Modifier.height(12.dp))

        MealBadge(recipe.mealType)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recipe.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = TintaComic
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recipe.description,
            fontSize = 13.sp,
            color = TintaComic.copy(alpha = 0.75f),
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        ListenButton(
            isSpeaking = isSpeaking,
            label = "Escuchar receta",
            onClick = { if (isSpeaking) onStopListening() else onListen() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InfoPill("${recipe.prepTimeMinutes} min")
            InfoPill(recipe.difficulty)
            InfoPill("$${recipe.estimatedPrice.toInt()} estimado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        BudgetCard(breakdown)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "INGREDIENTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (breakdown.owned.isNotEmpty()) {
            Text(
                text = "Ya tienes en casa",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeFresco
            )
            Spacer(modifier = Modifier.height(4.dp))
            breakdown.owned.forEach { item ->
                IngredientRow(
                    name = item.name,
                    costText = "$${item.userCost.toInt()} (tu costo)",
                    color = VerdeFresco,
                    icon = Icons.Default.CheckCircle
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (breakdown.missing.isNotEmpty()) {
            Text(
                text = "Te faltan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NaranjaCoral
            )
            Spacer(modifier = Modifier.height(4.dp))
            breakdown.missing.forEach { item ->
                IngredientRow(
                    name = item.name,
                    costText = "$${item.estimatedCost.toInt()} estimado",
                    color = NaranjaCoral,
                    icon = Icons.Default.AddCircle
                )
            }
        }

        if (breakdown.missing.isEmpty()) {
            Text(
                text = "¡Tienes todos los ingredientes para esta receta!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeFresco
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onExportPdf,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TintaComic)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = TintaComic,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Descargar Receta en PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(2.5.dp, TintaComic, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
        ) {
            Text(
                text = "Comenzar",
                color = BlancoPuro,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = BlancoPuro,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BudgetCard(breakdown: RecipeCostBreakdown) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoPuro)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "AJUSTE A TU PRESUPUESTO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            CostLine("Ingredientes que ya tienes", "$${breakdown.ownedCost.toInt()}", VerdeFresco)
            CostLine("Ingredientes que faltan", "$${breakdown.missingCost.toInt()}", NaranjaCoral)
            HorizontalDivider(color = TintaComic.copy(alpha = 0.12f))
            CostLine("Costo total", "$${breakdown.totalCost.toInt()}", TintaComic, bold = true)
            CostLine("Tu presupuesto", "$${breakdown.budget.toInt()}", TintaComic)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
                    .background(
                        if (breakdown.fitsBudget) VerdeFresco.copy(alpha = 0.18f)
                        else NaranjaCoral.copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (breakdown.fitsBudget) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (breakdown.fitsBudget) VerdeFresco else NaranjaCoral,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (breakdown.fitsBudget) {
                            "Esta receta cabe en tu presupuesto. Te quedan $${breakdown.remainingBudget.toInt()}."
                        } else {
                            "Te pasas por $${(-breakdown.remainingBudget).toInt()}. Puedes ajustar porciones o cambiar ingredientes."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (breakdown.fitsBudget) VerdeFresco else NaranjaCoral
                    )
                }
            }
        }
    }
}

@Composable
private fun StepByStep(
    recipe: RecipeEntity,
    steps: List<String>,
    stepIndex: Int,
    finished: Boolean,
    isSpeaking: Boolean,
    onListenStep: () -> Unit,
    onStopListening: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onBackToSummary: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackToSummary, contentPadding = PaddingValues(0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TintaComic,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resumen",
                        color = TintaComic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            MealBadge(recipe.mealType)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = recipe.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TintaComic
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (finished) {
            FinishedCard(onClose = onClose, onBackToSummary = onBackToSummary)
            return@Column
        }

        if (steps.isEmpty()) {
            Text(
                text = "Esta receta todavía no tiene pasos capturados.",
                fontSize = 13.sp,
                color = TintaComic.copy(alpha = 0.7f)
            )
            return@Column
        }

        Text(
            text = "PASO ${stepIndex + 1} DE ${steps.size}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NaranjaCoral
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Barra de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
                .background(BlancoPuro, RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (stepIndex + 1).toFloat() / steps.size)
                    .background(VerdeFresco, RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, TintaComic, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BlancoPuro)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(10.dp))
                        .background(AmarilloMostaza, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stepIndex + 1}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TintaComic
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = steps[stepIndex],
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TintaComic,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListenButton(
            isSpeaking = isSpeaking,
            label = "Escuchar paso",
            onClick = { if (isSpeaking) onStopListening() else onListenStep() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPrevious,
                enabled = stepIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlancoPuro,
                    disabledContainerColor = BlancoPuro.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Anterior",
                    color = TintaComic,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (stepIndex < steps.lastIndex) "Siguiente" else "Finalizar",
                        color = BlancoPuro,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (stepIndex < steps.lastIndex) {
                            Icons.AutoMirrored.Filled.ArrowForward
                        } else {
                            Icons.Default.Check
                        },
                        contentDescription = null,
                        tint = BlancoPuro,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "RESUMEN DE PASOS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        steps.forEachIndexed { index, step ->
            val isDone = index < stepIndex
            val isCurrent = index == stepIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.width(22.dp)) {
                    when {
                        isDone -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Paso completado",
                            tint = VerdeFresco,
                            modifier = Modifier.size(13.dp)
                        )
                        isCurrent -> Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Paso actual",
                            tint = NaranjaCoral,
                            modifier = Modifier.size(13.dp)
                        )
                        else -> Text(
                            text = "${index + 1}.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TintaComic.copy(alpha = 0.5f)
                        )
                    }
                }
                Text(
                    text = step,
                    fontSize = 11.sp,
                    color = if (isCurrent) TintaComic else TintaComic.copy(alpha = 0.55f),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ListenButton(
    isSpeaking: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSpeaking) VerdeFresco else AmarilloMostaza
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = if (isSpeaking) BlancoPuro else TintaComic,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpeaking) "Detener audio" else label,
                color = if (isSpeaking) BlancoPuro else TintaComic,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun FinishedCard(onClose: () -> Unit, onBackToSummary: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.5.dp, TintaComic, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeFresco)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = BlancoPuro,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¡Buen provecho!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = BlancoPuro
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Terminaste todos los pasos de la receta.",
                fontSize = 13.sp,
                color = BlancoPuro.copy(alpha = 0.9f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onBackToSummary,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BlancoPuro)
    ) {
        Text("Volver al resumen", color = TintaComic, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onClose,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
    ) {
        Text("Listo", color = BlancoPuro, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IngredientRow(
    name: String,
    costText: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                color = TintaComic
            )
        }
        Text(
            text = costText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CostLine(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TintaComic.copy(alpha = 0.7f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MealBadge(mealType: String) {
    Box(
        modifier = Modifier
            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
            .background(mealColor(mealType))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${mealEmoji(mealType)} ${mealType.uppercase()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (mealType == "Desayuno") TintaComic else BlancoPuro
        )
    }
}

@Composable
private fun InfoPill(text: String) {
    Box(
        modifier = Modifier
            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
            .background(BlancoPuro)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic
        )
    }
}

private fun mealEmoji(mealType: String): String = when (mealType) {
    "Desayuno" -> "🍳"
    "Comida" -> "🍲"
    "Cena" -> "🌙"
    else -> ""
}

private fun mealColor(mealType: String): Color = when (mealType) {
    "Desayuno" -> AmarilloMostaza
    "Comida" -> NaranjaCoral
    "Cena" -> VerdeFresco
    else -> TintaComic
}
