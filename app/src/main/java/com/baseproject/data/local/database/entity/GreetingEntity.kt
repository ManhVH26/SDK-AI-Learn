package com.baseproject.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.baseproject.domain.model.Greeting

@Entity(tableName = "greetings")
data class GreetingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(): Greeting = Greeting(message = message)
}

fun Greeting.toEntity(): GreetingEntity = GreetingEntity(message = message)
