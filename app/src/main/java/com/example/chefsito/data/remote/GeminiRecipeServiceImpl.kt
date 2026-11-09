package com.example.chefsito.data.remote

import com.example.chefsito.BuildConfig
import com.example.chefsito.data.remote.dto.AiRecipeDto
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiRecipeServiceImpl : AiRecipeService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun generateRecipe(ingredients: List<String>): Result<AiRecipeDto> {
        return withContext(Dispatchers.IO) {
            try {
                val joinedIngredients = if (ingredients.isEmpty()) "ingredientes sorpresa" else ingredients.joinToString(", ")
                
                val prompt = """
                    Actúa como un chef profesional. Genera una receta deliciosa utilizando los siguientes ingredientes: $joinedIngredients.
                    Regla estricta: Devuelve ÚNICAMENTE un objeto JSON válido (sin formato Markdown, sin comillas invertidas ni bloques de código). 
                    El JSON debe contener exactamente las siguientes claves:
                    - "title" (String)
                    - "estimatedPrice" (Double)
                    - "prepTimeMinutes" (Int)
                    - "ingredientsList" (Array de Strings)
                    - "instructions" (Array de Strings)
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                var responseText = response.text ?: throw Exception("La IA no devolvió ningún texto")
                
                // Limpiar el texto de etiquetas Markdown si la IA decide incluirlas de todos modos
                responseText = responseText.trim()
                if (responseText.startsWith("```json", ignoreCase = true)) {
                    responseText = responseText.substringAfter("```json").substringAfter("```JSON")
                } else if (responseText.startsWith("```")) {
                    responseText = responseText.substringAfter("```")
                }
                if (responseText.endsWith("```")) {
                    responseText = responseText.substringBeforeLast("```")
                }
                responseText = responseText.trim()

                // Parsear el JSON
                val jsonObject = JSONObject(responseText)
                val title = jsonObject.getString("title")
                val estimatedPrice = jsonObject.getDouble("estimatedPrice")
                val prepTimeMinutes = jsonObject.getInt("prepTimeMinutes")
                
                val ingredientsArray = jsonObject.getJSONArray("ingredientsList")
                val ingredientsListStr = java.lang.StringBuilder("Ingredientes:\n")
                for (i in 0 until ingredientsArray.length()) {
                    ingredientsListStr.append("• ${ingredientsArray.getString(i)}\n")
                }
                
                val instructionsArray = jsonObject.getJSONArray("instructions")
                val instructionsStr = java.lang.StringBuilder("\nInstrucciones:\n")
                for (i in 0 until instructionsArray.length()) {
                    instructionsStr.append("${i + 1}. ${instructionsArray.getString(i)}\n")
                }

                // Combinar la lista de ingredientes y los pasos en un solo String para la BD
                val combinedSteps = ingredientsListStr.toString() + instructionsStr.toString()

                val aiRecipeDto = AiRecipeDto(
                    title = title,
                    estimatedPrice = estimatedPrice,
                    prepTimeMinutes = prepTimeMinutes,
                    steps = combinedSteps.trim()
                )
                
                Result.success(aiRecipeDto)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}