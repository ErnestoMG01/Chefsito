package com.example.chefsito.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.entity.RecipeEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595f
    private const val PAGE_HEIGHT = 842f
    private const val MARGIN = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    private const val HEADER_HEIGHT = 50f
    private const val FOOTER_HEIGHT = 36f
    private const val CONTENT_TOP = MARGIN + HEADER_HEIGHT + 16f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT - 16f

    private const val COLOR_ACCENT = 0xFFFF6B4A.toInt()
    private const val COLOR_ACCENT_LIGHT = 0xFFFFF0EB.toInt()
    private const val COLOR_PRIMARY = 0xFFFFC145.toInt()
    private const val COLOR_SUCCESS = 0xFF4CAF7D.toInt()
    private const val COLOR_SUCCESS_LIGHT = 0xFFE8F5EE.toInt()
    private const val COLOR_TEXT_DARK = 0xFF1A1A1A.toInt()
    private const val COLOR_TEXT_MEDIUM = 0xFF333333.toInt()
    private const val COLOR_TEXT_MUTED = 0xFF999999.toInt()
    private const val COLOR_BORDER = 0xFFE0E0E0.toInt()
    private const val COLOR_WHITE = 0xFFFFFFFF.toInt()
    private const val COLOR_CARD_BG = 0xFFFAFAFA.toInt()

    private fun pageInfo(pageNumber: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber).create()

    private fun paint(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun textPaint(size: Float, color: Int, bold: Boolean): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

    data class IngredientRow(
        val name: String,
        val cost: Double,
        val status: String,
        val statusColor: Int
    )

    private fun buildIngredientRows(breakdown: RecipeCostBreakdown): List<IngredientRow> {
        val rows = mutableListOf<IngredientRow>()
        breakdown.owned.forEach { ing ->
            rows.add(IngredientRow(ing.name, ing.userCost, "Tengo", COLOR_SUCCESS))
        }
        breakdown.missing.forEach { ing ->
            rows.add(IngredientRow(ing.name, ing.estimatedCost, "Falta", COLOR_ACCENT))
        }
        return rows
    }

    fun exportRecipe(
        context: Context,
        recipe: RecipeEntity,
        breakdown: RecipeCostBreakdown
    ): File? {
        return try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val safeTitle = recipe.title
                .replace(Regex("[^A-Za-z0-9ÁÉÍÓÚáéíóúÑñ _-]"), "")
                .trim()
                .replace(Regex("\\s+"), "_")
                .take(48)
                .ifBlank { "receta" }
            val file = File(documentsDir, "${safeTitle}_${System.currentTimeMillis()}.pdf")

            val document = PdfDocument()
            var pageNumber = 1
            var page = document.startPage(pageInfo(pageNumber))
            var canvas = page.canvas
            var y = CONTENT_TOP

            val steps = RecipeCosting.parseSteps(recipe)
            val rows = buildIngredientRows(breakdown)
            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val accentPaint = paint(COLOR_ACCENT)
            val titlePaint = textPaint(24f, COLOR_TEXT_DARK, true)
            val sectionTitlePaint = textPaint(14f, COLOR_ACCENT, true)
            val bodyPaint = textPaint(11f, COLOR_TEXT_MEDIUM, false)
            val bodyBoldPaint = textPaint(11f, COLOR_TEXT_DARK, true)
            val smallPaint = textPaint(9f, COLOR_TEXT_MUTED, false)
            val dividerPaint = paint(COLOR_BORDER).apply { strokeWidth = 1f }
            val tableHeaderPaint = textPaint(10f, COLOR_WHITE, true)
            val cardBgPaint = paint(COLOR_CARD_BG)
            val successPaint = paint(COLOR_SUCCESS)
            val successBgPaint = paint(COLOR_SUCCESS_LIGHT)
            val accentBgPaint = paint(COLOR_ACCENT_LIGHT)

            fun drawHeader() {
                canvas.drawRect(0f, 0f, PAGE_WIDTH, HEADER_HEIGHT, accentPaint)
                canvas.drawText("Chefsito", MARGIN, 32f, textPaint(18f, COLOR_WHITE, true))
            }

            fun drawFooter() {
                val footerY = PAGE_HEIGHT - MARGIN + 10f
                canvas.drawLine(MARGIN, footerY - 14f, PAGE_WIDTH - MARGIN, footerY - 14f, dividerPaint)
                canvas.drawText("Generado por Chefsito · $dateStr", MARGIN, footerY, smallPaint)
                val pageText = "Página $pageNumber"
                val pageTextWidth = smallPaint.measureText(pageText)
                canvas.drawText(pageText, PAGE_WIDTH - MARGIN - pageTextWidth, footerY, smallPaint)
            }

            fun checkPageOverflow(needed: Float) {
                if (y + needed > CONTENT_BOTTOM) {
                    drawFooter()
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(pageInfo(pageNumber))
                    canvas = page.canvas
                    drawHeader()
                    y = CONTENT_TOP
                }
            }

            // Dibuja Encabezado de Página 1
            drawHeader()

            // Título de la Receta
            canvas.drawText(recipe.title, MARGIN, y, titlePaint)
            y += 32f

            // Metadatos (Tiempo, Dificultad, Precio Estimado)
            val metaText = "⏱ ${recipe.prepTimeMinutes} min  |  📊 ${recipe.difficulty}  |  💰 \$${recipe.estimatedPrice.toInt()} MXN"
            canvas.drawText(metaText, MARGIN, y, bodyPaint)
            y += 24f

            // Resumen de Costos (Tarjetas)
            val cardWidth = (CONTENT_WIDTH - 16f) / 3
            val cardHeight = 64f

            val titles = listOf("En casa", "Te falta", "Total")
            val costs = listOf(breakdown.ownedCost, breakdown.missingCost, breakdown.totalCost)
            val iconColors = listOf(successPaint, accentPaint, if (breakdown.fitsBudget) successPaint else accentPaint)

            checkPageOverflow(cardHeight + 16f)
            for (i in titles.indices) {
                val x = MARGIN + i * (cardWidth + 8f)
                canvas.drawRoundRect(x, y, x + cardWidth, y + cardHeight, 10f, 10f, cardBgPaint)
                canvas.drawRect(x, y, x + cardWidth, y + 4f, iconColors[i])
                canvas.drawText(titles[i], x + 10f, y + 20f, smallPaint)
                canvas.drawText("\$${costs[i].toInt()}", x + 10f, y + 44f, textPaint(16f, COLOR_TEXT_DARK, true))
            }
            y += cardHeight + 20f

            // Sección: Ingredientes
            checkPageOverflow(40f)
            canvas.drawText("INGREDIENTES", MARGIN, y, sectionTitlePaint)
            y += 18f

            val colWidths = floatArrayOf(CONTENT_WIDTH * 0.50f, CONTENT_WIDTH * 0.25f, CONTENT_WIDTH * 0.25f)
            val colStarts = floatArrayOf(MARGIN, MARGIN + colWidths[0], MARGIN + colWidths[0] + colWidths[1])
            val rowHeight = 24f

            // Cabecera de Tabla
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight, accentPaint)
            canvas.drawText("INGREDIENTE", colStarts[0] + 8f, y + 16f, tableHeaderPaint)
            canvas.drawText("COSTO", colStarts[1] + 8f, y + 16f, tableHeaderPaint)
            canvas.drawText("ESTADO", colStarts[2] + 8f, y + 16f, tableHeaderPaint)
            y += rowHeight

            // Filas de Ingredientes
            rows.forEachIndexed { index, row ->
                checkPageOverflow(rowHeight)
                val rowBg = if (index % 2 == 0) paint(COLOR_WHITE) else paint(COLOR_CARD_BG)
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight, rowBg)

                canvas.drawText(row.name, colStarts[0] + 8f, y + 16f, bodyPaint)
                canvas.drawText("\$${row.cost.toInt()}", colStarts[1] + 8f, y + 16f, bodyBoldPaint)

                val statusColor = row.statusColor
                val statusBg = if (statusColor == COLOR_SUCCESS) successBgPaint else accentBgPaint
                val statusTextPaint = textPaint(9f, statusColor, true)
                canvas.drawRoundRect(colStarts[2] + 4f, y + 3f, colStarts[2] + 60f, y + 20f, 6f, 6f, statusBg)
                canvas.drawText(row.status, colStarts[2] + 12f, y + 15f, statusTextPaint)

                y += rowHeight
            }
            y += 24f

            // Sección: Preparación
            checkPageOverflow(40f)
            canvas.drawText("PREPARACIÓN", MARGIN, y, sectionTitlePaint)
            y += 20f

            steps.forEachIndexed { index, stepText ->
                checkPageOverflow(36f)
                val stepNum = "${index + 1}. "
                canvas.drawText(stepNum, MARGIN, y, bodyBoldPaint)

                val numWidth = bodyBoldPaint.measureText(stepNum)
                val maxWidth = CONTENT_WIDTH - numWidth
                val words = stepText.split(" ")
                var line = ""
                var lineCount = 0

                words.forEach { word ->
                    val candidate = if (line.isEmpty()) word else "$line $word"
                    if (bodyPaint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                        checkPageOverflow(16f)
                        canvas.drawText(line, MARGIN + if (lineCount == 0) numWidth else 20f, y, bodyPaint)
                        y += 16f
                        line = word
                        lineCount++
                    } else {
                        line = candidate
                    }
                }
                if (line.isNotEmpty()) {
                    checkPageOverflow(16f)
                    canvas.drawText(line, MARGIN + if (lineCount == 0) numWidth else 20f, y, bodyPaint)
                    y += 20f
                }
            }

            drawFooter()
            document.finishPage(page)

            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openPdf(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        try {
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val uri = FileProvider.getUriForFile(context, authority, file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Compartir Receta PDF").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (shareErr: Exception) {
                shareErr.printStackTrace()
            }
        }
    }
}
