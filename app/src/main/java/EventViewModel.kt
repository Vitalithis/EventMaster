package com.danocampo.eventmaster

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danocampo.eventmaster.data.CategoryEntity
import com.danocampo.eventmaster.data.EventEntity
import com.danocampo.eventmaster.data.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Event(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String,
    val date: String,
    val location: String,
    val imageUri: String? = null,
    val imageScale: Float = 1f,
    val imageOffsetX: Float = 0f,
    val imageOffsetY: Float = 0f
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    var categories = mutableStateListOf<String>()
        private set

    var events = mutableStateListOf<Event>()
        private set

    init {
        // Observar categorías desde Room
        viewModelScope.launch {
            repository.getAllCategories().collectLatest { categoryEntities ->
                categories.clear()
                if (categoryEntities.isEmpty()) {
                    // Categorías por defecto si la base de datos está vacía
                    val defaultCategories = listOf("Música", "Tecnología", "Deportes")
                    defaultCategories.forEach { addCategory(it) }
                } else {
                    categories.addAll(categoryEntities.map { it.name })
                }
            }
        }

        // Observar eventos desde Room
        viewModelScope.launch {
            repository.getAllEvents().collectLatest { eventEntities ->
                events.clear()
                events.addAll(eventEntities.map { it.toDomain() })
            }
        }
    }

    fun addCategory(name: String) {
        if (name.isNotBlank() && !categories.contains(name)) {
            viewModelScope.launch {
                repository.insertCategory(CategoryEntity(name))
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event.toEntity())
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event.toEntity())
        }
    }
    
    private fun Event.toEntity() = EventEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        date = date,
        location = location,
        imageUri = imageUri,
        imageScale = imageScale,
        imageOffsetX = imageOffsetX,
        imageOffsetY = imageOffsetY
    )

    private fun EventEntity.toDomain() = Event(
        id = id,
        title = title,
        description = description,
        category = category,
        date = date,
        location = location,
        imageUri = imageUri,
        imageScale = imageScale,
        imageOffsetX = imageOffsetX,
        imageOffsetY = imageOffsetY
    )
}