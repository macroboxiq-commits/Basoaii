package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CreativeProjectEntity::class], version = 1, exportSchema = false)
abstract class CreativeDatabase : RoomDatabase() {
  abstract fun creativeProjectDao(): CreativeProjectDao

  companion object {
    @Volatile
    private var INSTANCE: CreativeDatabase? = null

    fun getDatabase(context: Context): CreativeDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          CreativeDatabase::class.java,
          "creative_studio.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
