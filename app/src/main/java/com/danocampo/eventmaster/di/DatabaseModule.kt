package com.danocampo.eventmaster.di

import android.content.Context
import androidx.room.Room
import com.danocampo.eventmaster.data.AppDatabase
import com.danocampo.eventmaster.data.EventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "event_master_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
}