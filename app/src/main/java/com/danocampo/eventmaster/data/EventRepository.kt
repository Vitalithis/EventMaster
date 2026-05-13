package com.danocampo.eventmaster.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {
    fun getAllEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: EventEntity) = eventDao.insertEvent(event)

    fun getAllCategories(): Flow<List<CategoryEntity>> = eventDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) = eventDao.insertCategory(category)
}