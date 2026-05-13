package com.danocampo.eventmaster.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.danocampo.eventmaster.data.EventEntity
import com.danocampo.eventmaster.data.CategoryEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)
}