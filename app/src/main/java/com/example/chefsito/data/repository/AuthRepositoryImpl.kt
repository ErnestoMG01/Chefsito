package com.example.chefsito.data.repository

import android.content.Context
import com.example.chefsito.data.local.dao.UserDao
import com.example.chefsito.data.local.entity.UserEntity
import com.example.chefsito.data.remote.ChefsitoCloudApiService
import com.example.chefsito.utils.PasswordHasher
import com.example.chefsito.utils.SessionManager

/**
 * Repositorio híbrido de Autenticación (Cloud AWS RDS + Respaldo Local Room).
 *
 * 1. Primero intenta registrar/autenticar contra el backend desplegado en AWS EC2 + RDS
 *    (que utiliza Bcrypt con salt y devuelve un JWT para concurrencia multiusuario).
 * 2. Sincroniza el usuario en Room local y guarda el token JWT en `SessionManager`.
 * 3. Si el dispositivo está temporalmente sin conexión al servidor AWS, aplica fallback
 *    transparente a Room local para no bloquear la experiencia.
 */
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val appContext: Context? = null
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        val cleanName = name.trim()
        val cleanEmail = normalizeEmail(email)

        if (cleanName.isBlank()) {
            return Result.failure(Exception("El nombre es requerido"))
        }
        if (cleanEmail.isBlank()) {
            return Result.failure(Exception("El correo es requerido"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        // 1. Intentar registro en el servidor AWS (PostgreSQL RDS)
        if (appContext != null) {
            val baseUrl = SessionManager.getApiBaseUrl(appContext)
            val cloudAttempt = runCatching {
                ChefsitoCloudApiService.register(baseUrl, cleanName, cleanEmail, password)
            }
            if (cloudAttempt.isSuccess) {
                val cloudAuth = cloudAttempt.getOrThrow()
                val localMirror = UserEntity(
                    id = cloudAuth.user.id,
                    name = cloudAuth.user.name,
                    email = cloudAuth.user.email,
                    passwordHash = PasswordHasher.sha256(password)
                )
                userDao.registerUser(localMirror)
                SessionManager.saveSession(
                    context = appContext,
                    userId = localMirror.id,
                    userName = localMirror.name,
                    email = localMirror.email,
                    jwtToken = cloudAuth.accessToken
                )
                return Result.success(localMirror)
            } else {
                val errorMsg = cloudAttempt.exceptionOrNull()?.message.orEmpty()
                if (errorMsg.contains("Ya existe", ignoreCase = true)) {
                    return Result.failure(Exception("Ya existe una cuenta con ese correo en el servidor"))
                }
            }
        }

        // 2. Respaldo local (Offline-First)
        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(Exception("Ya existe una cuenta con ese correo"))
        }

        return try {
            val user = UserEntity(
                name = cleanName,
                email = cleanEmail,
                passwordHash = PasswordHasher.sha256(password)
            )
            val id = userDao.registerUser(user)
            val created = user.copy(id = id)
            appContext?.let {
                SessionManager.saveSession(it, created.id, created.name, created.email)
            }
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo crear la cuenta: ${e.message ?: "error desconocido"}"))
        }
    }

    override suspend fun login(email: String, password: String): Result<UserEntity> {
        val cleanEmail = normalizeEmail(email)
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Ingresa tu correo y contraseña"))
        }

        // 1. Intentar login contra el servidor AWS (PostgreSQL RDS + JWT)
        if (appContext != null) {
            val baseUrl = SessionManager.getApiBaseUrl(appContext)
            val cloudAttempt = runCatching {
                ChefsitoCloudApiService.login(baseUrl, cleanEmail, password)
            }
            if (cloudAttempt.isSuccess) {
                val cloudAuth = cloudAttempt.getOrThrow()
                val localMirror = UserEntity(
                    id = cloudAuth.user.id,
                    name = cloudAuth.user.name,
                    email = cloudAuth.user.email,
                    passwordHash = PasswordHasher.sha256(password)
                )
                userDao.registerUser(localMirror)
                SessionManager.saveSession(
                    context = appContext,
                    userId = localMirror.id,
                    userName = localMirror.name,
                    email = localMirror.email,
                    jwtToken = cloudAuth.accessToken
                )
                return Result.success(localMirror)
            } else {
                val errMsg = cloudAttempt.exceptionOrNull()?.message.orEmpty()
                if (errMsg.contains("incorrectos", ignoreCase = true)) {
                    return Result.failure(Exception("Correo o contraseña incorrectos"))
                }
            }
        }

        // 2. Respaldo local en Room
        return try {
            val user = userDao.authenticate(cleanEmail, PasswordHasher.sha256(password))
                ?: return Result.failure(Exception("Correo o contraseña incorrectos"))
            appContext?.let {
                SessionManager.saveSession(it, user.id, user.name, user.email)
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo iniciar sesión: ${e.message ?: "error desconocido"}"))
        }
    }

    override suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)

    override suspend fun emailExists(email: String): Boolean =
        userDao.getUserByEmail(normalizeEmail(email)) != null

    private fun normalizeEmail(email: String): String = email.trim().lowercase()
}
