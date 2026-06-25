package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<ScoreRecord>>

    @Query("SELECT * FROM scores WHERE mode = :mode ORDER BY timestamp DESC")
    fun getScoresByMode(mode: String): Flow<List<ScoreRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecord)

    @Query("DELETE FROM scores")
    suspend fun clearAllScores()

    @Query("SELECT * FROM guesses ORDER BY timestamp DESC")
    fun getAllGuesses(): Flow<List<GuessRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuess(guess: GuessRecord)

    @Query("DELETE FROM guesses")
    suspend fun clearAllGuesses()
}
