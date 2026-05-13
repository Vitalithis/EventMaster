package com.danocampo.eventmaster.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EventEntity::class, CategoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
