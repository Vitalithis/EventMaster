package com.danocampo.eventmaster

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
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

class EventViewModel : ViewModel() {
    var categories = mutableStateListOf("Música", "Tecnología", "Deportes")
        private set

    var events = mutableStateListOf<Event>()
        private set

    fun addCategory(name: String) {
        if (name.isNotBlank() && !categories.contains(name)) {
            categories.add(name)
        }
    }

    fun addEvent(event: Event) {
        events.add(event)
    }

    fun updateEvent(updatedEvent: Event) {
        val index = events.indexOfFirst { it.id == updatedEvent.id }
        if (index != -1) {
            events[index] = updatedEvent
        }
    }
}