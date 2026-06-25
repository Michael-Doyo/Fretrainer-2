package com.example.db

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val allScores: Flow<List<ScoreRecord>> = scoreDao.getAllScores()
    val allGuesses: Flow<List<GuessRecord>> = scoreDao.getAllGuesses()

    fun getScoresByMode(mode: String): Flow<List<ScoreRecord>> = scoreDao.getScoresByMode(mode)

    suspend fun insertScore(score: ScoreRecord) = scoreDao.insertScore(score)

    suspend fun clearAllScores() = scoreDao.clearAllScores()

    suspend fun insertGuess(guess: GuessRecord) = scoreDao.insertGuess(guess)

    suspend fun clearAllGuesses() = scoreDao.clearAllGuesses()
}
