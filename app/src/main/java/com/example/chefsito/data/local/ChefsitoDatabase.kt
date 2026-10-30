package com.example.chefsito.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chefsito.data.local.dao.IngredientDao
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity

@Database(entities = [IngredientEntity::class, RecipeEntity::class], version = 1, exportSchema = false)
abstract class ChefsitoDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: ChefsitoDatabase? = null

        fun getDatabase(context: Context): ChefsitoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChefsitoDatabase::class.java,
                    "chefsito_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}