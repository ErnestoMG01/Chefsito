package com.example.chefsito.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
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

/**
 * Genera un PDF de la receta usando únicamente `android.graphics.pdf.PdfDocument`
 * (SDK nativo, sin librerías externas) y lo guarda en la carpeta pública de
 * documentos de la app: `getExternalFilesDir(DIRECTORY_DOCUMENTS)`.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f

    fun exportRecipe(
        context: Context,
        recipe: RecipeEntity,
        breakdown: RecipeCostBreakdown
    ): File? {
        val documentsDir = (
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "documents")
            ).apply { if (!exists()) mkdirs() }

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
        var y = MARGIN

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(230, 126, 90)
        }
        val titlePaint = textPaint(21f, Color.rgb(30, 30, 30), bold = true)
        val sectionPaint = textPaint(12f, Color.rgb(230, 126, 90), bold = true)
        val bodyPaint = textPaint(11f, Color.rgb(55, 55, 55))
        val boldBodyPaint = textPaint(11f, Color.rgb(30, 30, 30), bold = true)
        val mutedPaint = textPaint(10f, Color.rgb(105, 105, 105))
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 220, 220)
            strokeWidth = 1f
        }

        fun newPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(pageInfo(pageNumber))
            canvas = page.canvas
            y = MARGIN
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) newPage()
        }

        fun gap(value: Float) {
            y += value
        }

        fun divider() {
            ensureSpace(12f)
            gap(6f)
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
            gap(10f)
        }

        fun drawWrapped(text: String, paint: Paint, lineHeight: Float) {
            val maxWidth = PAGE_WIDTH - MARGIN * 2
            val words = text.split(" ")
            var line = ""
            words.forEach { word ->
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                    ensureSpace(lineHeight)
                    canvas.drawText(line, MARGIN, y, paint)
                    y += lineHeight
                    line = word
                } else {
                    line = candidate
                }
            }
            if (line.isNotEmpty()) {
                ensureSpace(lineHeight)
                canvas.drawText(line, MARGIN, y, paint)
                y += lineHeight
            }
        }

        fun section(title: String) {
            gap(12f)
            drawWrapped(title, sectionPaint, 17f)
            gap(4f)
        }

        try {
            // Franja superior de color.
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, accentPaint)
            y = MARGIN + 6f

            drawWrapped(recipe.title, titlePaint, 27f)
            gap(2f)
            drawWrapped(
                "${recipe.mealType} · ${recipe.prepTimeMinutes} min · ${recipe.difficulty}",
                mutedPaint,
                15f
            )

            section("DESCRIPCIÓN")
            drawWrapped(recipe.description, bodyPaint, 16f)

            section("COSTO CALCULADO SEGÚN TU DESPENSA")
            drawWrapped(
                "Ingredientes que ya tienes: $${breakdown.ownedCost.toInt()} MXN",
                bodyPaint,
                16f
            )
            drawWrapped(
                "Ingredientes que te faltan: $${breakdown.missingCost.toInt()} MXN",
                bodyPaint,
                16f
            )
            drawWrapped("Costo total: $${breakdown.totalCost.toInt()} MXN", boldBodyPaint, 16f)
            drawWrapped("Tu presupuesto: $${breakdown.budget.toInt()} MXN", bodyPaint, 16f)
            drawWrapped(
                if (breakdown.fitsBudget) {
                    "La receta cabe en tu presupuesto."
                } else {
                    "Te pasas por $${(-breakdown.remainingBudget).toInt()} MXN de tu presupuesto."
                },
                bodyPaint,
                16f
            )

            section("INGREDIENTES CON CANTIDADES")
            breakdown.owned.forEach { item ->
                drawWrapped("Ya lo tienes: ${item.name} ($${item.userCost.toInt()})", bodyPaint, 16f)
                gap(2f)
            }
            breakdown.missing.forEach { item ->
                drawWrapped("Falta: ${item.name} (estimado: $${item.estimatedCost.toInt()})", bodyPaint, 16f)
                gap(2f)
            }

            divider()

            section("PASOS DE PREPARACIÓN")
            RecipeCosting.parseSteps(recipe).forEachIndexed { index, step ->
                drawWrapped("${index + 1}. $step", bodyPaint, 16f)
                gap(6f)
            }

            divider()
            drawWrapped("Generado por Chefsito · Receta 100% mexicana", mutedPaint, 14f)

            document.finishPage(page)
            FileOutputStream(file).use { output -> document.writeTo(output) }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        } finally {
            document.close()
        }

        return file
    }

    /** Abre el PDF con cualquier visor instalado o, si no hay, lo comparte. */
    fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(viewIntent)
        } catch (exception: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(
                    Intent.createChooser(shareIntent, "Compartir receta en PDF")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (ignored: Exception) {
                // El Toast con la ruta ya se mostró desde la pantalla.
            }
        }
    }

    private fun pageInfo(pageNumber: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()

    private fun textPaint(size: Float, color: Int, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = Typeface.create(
                Typeface.DEFAULT,
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        }
}
