package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario registrado en Chefsito.
 *
 * El correo se guarda normalizado (minúsculas y sin espacios) y es único.
 * La contraseña NUNCA se guarda en texto plano: se almacena su hash SHA-256.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
