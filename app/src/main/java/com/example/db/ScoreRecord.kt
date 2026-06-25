package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String, // "find_it", "name_it", "guitar", "play_along"
    val score: Int,
    val total: Int,
    val percentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "guesses")
data class GuessRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val note: String, // e.g. "C", "F#", "A"
    val stringIndex: Int, // 1 to 6
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
