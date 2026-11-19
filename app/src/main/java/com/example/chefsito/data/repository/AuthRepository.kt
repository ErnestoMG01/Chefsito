package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.UserEntity


interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<UserEntity>
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun getUserById(id: Long): UserEntity?
    suspend fun emailExists(email: String): Boolean
}
