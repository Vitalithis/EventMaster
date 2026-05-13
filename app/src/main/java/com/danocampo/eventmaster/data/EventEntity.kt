package com.danocampo.eventmaster.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val date: String,
    val location: String,
    val imageUri: String?,
    val imageScale: Float,
    val imageOffsetX: Float,
    val imageOffsetY: Float
)