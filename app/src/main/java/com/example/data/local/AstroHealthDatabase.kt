package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.HealthLog

@Database(entities = [HealthLog::class], version = 1, exportSchema = false)
abstract class AstroHealthDatabase : RoomDatabase() {
    abstract fun healthLogDao(): HealthLogDao

    companion object {
        @Volatile
        private var INSTANCE: AstroHealthDatabase? = null

        fun getDatabase(context: Context): AstroHealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroHealthDatabase::class.java,
                    "astro_health_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
