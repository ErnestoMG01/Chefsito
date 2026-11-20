package com.example.chefsito.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chefsito.data.local.dao.IngredientDao
import com.example.chefsito.data.local.dao.RecipeDao
import com.example.chefsito.data.local.dao.UserDao
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.local.entity.UserEntity

@Database(
    entities = [IngredientEntity::class, RecipeEntity::class, UserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class ChefsitoDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: ChefsitoDatabase? = null

        fun getDatabase(context: Context): ChefsitoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChefsitoDatabase::class.java,
                    "chefsito_database"
                )
                    // Entrega inmediata: si cambia el schema se recrea la BD en lugar
                    // de lanzar una excepción de migración.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
