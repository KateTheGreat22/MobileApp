package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class, GraduationStep::class, PostGradPlan::class, Reward::class], version = 1)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile
        private var INSTANCE: PlannerDatabase? = null

        fun getDatabase(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "planner_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
