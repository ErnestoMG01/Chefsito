package com.example.chefsito.data.domain

import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity

/** Un ingrediente de la receta con su costo estimado de mercado. */
data class IngredientLine(
    val name: String,
    val estimatedCost: Double
)

/** Ingrediente que el usuario YA tiene, con el costo que él registró. */
data class OwnedIngredient(
    val name: String,
    val userCost: Double
)

/** Ingrediente que le FALTA al usuario, con su costo estimado. */
data class MissingIngredient(
    val name: String,
    val estimatedCost: Double
)

/**
 * Desglose de costos de una receta contra la despensa y el presupuesto.
 *
 * - [ownedCost] suma los costos capturados por el usuario (`userCost`).
 * - [missingCost] suma los costos estimados de lo que no tiene.
 * - [totalCost] = ownedCost + missingCost.
 */
data class RecipeCostBreakdown(
    val recipe: RecipeEntity,
    val owned: List<OwnedIngredient>,
    val missing: List<MissingIngredient>,
    val ownedCost: Double,
    val missingCost: Double,
    val totalCost: Double,
    val budget: Double
) {
    val fitsBudget: Boolean get() = totalCost <= budget
    val remainingBudget: Double get() = budget - totalCost
    val matchCount: Int get() = owned.size
    val totalIngredients: Int get() = owned.size + missing.size
}

/**
 * Utilidades puras del motor determinista de recetas.
 * No depende de Android ni de Room, por lo que es fácil de probar.
 */
object RecipeCosting {

    /** Parsea las líneas `"Nombre|Costo"` del campo ingredients. */
    fun parseIngredients(recipe: RecipeEntity): List<IngredientLine> =
        recipe.ingredients.lines().mapNotNull { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@mapNotNull null
            val name = line.substringBefore("|").trim()
            val cost = line.substringAfter("|", "").trim().toDoubleOrNull() ?: 0.0
            if (name.isBlank()) null else IngredientLine(name, cost)
        }

    /** Parsea los pasos numerados y les quita el prefijo "1. ". */
    fun parseSteps(recipe: RecipeEntity): List<String> =
        recipe.steps.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.replaceFirst(Regex("^\\d+[.)]\\s*"), "").trim() }
            .filter { it.isNotBlank() }

    /** Calcula el desglose completo de una receta contra la despensa. */
    fun breakdown(
        recipe: RecipeEntity,
        pantry: List<IngredientEntity>,
        budget: Double
    ): RecipeCostBreakdown {
        val owned = mutableListOf<OwnedIngredient>()
        val missing = mutableListOf<MissingIngredient>()

        parseIngredients(recipe).forEach { line ->
            val match = findPantryMatch(line.name, pantry)
            if (match != null) {
                owned += OwnedIngredient(name = line.name, userCost = match.userCost)
            } else {
                missing += MissingIngredient(name = line.name, estimatedCost = line.estimatedCost)
            }
        }

        val ownedCost = owned.sumOf { it.userCost }
        val missingCost = missing.sumOf { it.estimatedCost }

        return RecipeCostBreakdown(
            recipe = recipe,
            owned = owned,
            missing = missing,
            ownedCost = ownedCost,
            missingCost = missingCost,
            totalCost = ownedCost + missingCost,
            budget = budget
        )
    }

    /** Busca coincidencias tolerantes a acentos, mayúsculas y nombres compuestos. */
    fun findPantryMatch(
        ingredientName: String,
        pantry: List<IngredientEntity>
    ): IngredientEntity? {
        val target = normalize(ingredientName)
        if (target.isBlank()) return null

        return pantry.firstOrNull { item ->
            val candidate = normalize(item.name)
            when {
                candidate.isBlank() -> false
                candidate == target -> true
                target.contains(candidate) || candidate.contains(target) -> true
                else -> {
                    val tokens = candidate.split(" ").filter { it.length > 3 }
                    tokens.any { token -> target.contains(token) }
                }
            }
        }
    }

    /** Minúsculas y sin acentos para comparar de forma determinista. */
    fun normalize(text: String): String {
        val replacements = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u',
            'ü' to 'u', 'Á' to 'a', 'É' to 'e', 'Í' to 'i', 'Ó' to 'o',
            'Ú' to 'u', 'Ü' to 'u', 'ñ' to 'n', 'Ñ' to 'n'
        )
        return buildString {
            text.trim().lowercase().forEach { char -> append(replacements[char] ?: char) }
        }
    }
}
