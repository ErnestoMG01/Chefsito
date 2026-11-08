package com.example.chefsito.data.remote.dto

data class AiRecipeDto(
    val title: String,
    val estimatedPrice: Double,
    val prepTimeMinutes: Int,
    val steps: String
)
