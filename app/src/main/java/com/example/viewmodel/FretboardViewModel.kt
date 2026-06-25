package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.GuitarTheory
import com.example.audio.PitchDetector
import com.example.db.AppDatabase
import com.example.db.GuessRecord
import com.example.db.ScoreRecord
import com.example.db.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class FretboardViewModel(
    application: Application,
    private val repository: ScoreRepository
) : AndroidViewModel(application) {

    // --- Navigation & High-Level State ---
    var activeScreen by mutableStateOf("dashboard") // dashboard, find_it, name_it, guitar_input, play_along, stats, settings

    val allScores: StateFlow<List<ScoreRecord>> = repository.allScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGuesses: StateFlow<List<GuessRecord>> = repository.allGuesses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Training Customization Settings ---
    var includeAccidentals by mutableStateOf(true) // If false, only natural notes (C, D, E, F, G, A, B)
    var maxFretSelected by mutableStateOf(12) // 5 or 12

    // Learn mode vs Quiz mode state
    var isLearnMode by mutableStateOf(true)
    var wrongGuessNote by mutableStateOf<String?>(null)
    var wrongGuessDirection by mutableStateOf<String?>(null) // "higher" or "lower"

    // "Find Note on All Strings" challenge
    var allStringsChallengeActive by mutableStateOf(false)
    var allStringsChallengeFound by mutableStateOf<Set<Int>>(emptySet())

    // Found positions (string, fret) for target note to fill bars dynamically
    var foundPositionsForCurrentNote by mutableStateOf<Set<Pair<Int, Int>>>(emptySet())

    fun getTargetBarCount(note: String): Int {
        val clean = note.uppercase().trim()
        return when (clean) {
            "E" -> 8
            "A", "D", "G", "B" -> 7
            else -> 6 // Accidentals, C, F
        }
    }

    // Filtered chromatic notes based on settings
    private fun getAvailableNotes(): List<String> {
        return if (includeAccidentals) {
            GuitarTheory.chromaticScale
        } else {
            listOf("C", "D", "E", "F", "G", "A", "B")
        }
    }

    // --- Game / Session States ---
    var sessionScore by mutableStateOf(0)
    var sessionTotal by mutableStateOf(0)
    var currentQuestionIndex by mutableStateOf(1)
    var totalQuestions = 10
    var isRoundActive by mutableStateOf(false)
    var isAnswered by mutableStateOf(false)
    var wasCorrect by mutableStateOf(false)
    var feedbackMessage by mutableStateOf("")

    // Specific mode state
    // 1. Find-it Mode
    var findItTargetNote by mutableStateOf("C")
    var findItTargetString by mutableStateOf(5) // target string (1 to 6)
    var findItSelectedPosition by mutableStateOf<Pair<Int, Int>?>(null) // (stringIndex, fret)
    var findItRevealCorrectPositions by mutableStateOf(false)

    // 2. Name-it Mode
    var nameItHighlightedPosition by mutableStateOf(Pair(6, 3)) // Low E, 3rd fret (G)
    var nameItChoices by mutableStateOf<List<String>>(emptyList())
    var nameItSelectedAnswer by mutableStateOf<String?>(null)

    // 3. Guitar Pitch Input Mode
    var guitarTargetNote by mutableStateOf("C")
    var liveFrequency by mutableStateOf(0.0)
    var liveNoteName by mutableStateOf<String?>(null)
    var liveRms by mutableStateOf(0.0)
    private var pitchDetector: PitchDetector? = null
    private var guitarHoldCount = 0
    private val guitarHoldTarget = 4 // Require ~4 consecutive frames matching target to trigger success

    // 4. Play-along Mode
    var tempoBpm by mutableStateOf(60)
    val playAlongSequence = listOf("E", "G", "A", "C", "D", "E", "D", "C")
    var playAlongIndex by mutableStateOf(0)
    var playAlongBeatProgress by mutableStateOf(0.0f) // 0.0 to 1.0 within the current beat
    var isPlayingAlong by mutableStateOf(false)
    var playAlongScore by mutableStateOf(0)
    var playAlongFeedback by mutableStateOf("Get Ready!")
    var currentPlayedMatch by mutableStateOf(false)
    private var playAlongJob: Job? = null

    init {
        // Initialize pitch detector with callback
        pitchDetector = PitchDetector { frequency, noteName, rms ->
            viewModelScope.launch(Dispatchers.Main) {
                liveFrequency = frequency
                liveNoteName = noteName
                liveRms = rms
                
                if (isRoundActive) {
                    if (activeScreen == "guitar_input") {
                        handleLiveGuitarInput(noteName)
                    } else if (activeScreen == "play_along" && isPlayingAlong) {
                        handleLivePlayAlongInput(noteName)
                    }
                }
            }
        }
    }

    // --- Database Operations ---
    fun saveScore(mode: String, score: Int, total: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val percentage = if (total > 0) (score.toFloat() / total.toFloat() * 100f) else 0f
            repository.insertScore(
                ScoreRecord(
                    mode = mode,
                    score = score,
                    total = total,
                    percentage = percentage
                )
            )
        }
    }

    fun recordGuess(note: String, stringIndex: Int, isCorrect: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGuess(
                GuessRecord(
                    note = note,
                    stringIndex = stringIndex,
                    isCorrect = isCorrect
                )
            )
        }
    }

    fun clearStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllScores()
            repository.clearAllGuesses()
        }
    }

    // --- Mode Control Handlers ---

    // 1. Find-it Mode Logic
    fun startFindItRound() {
        sessionScore = 0
        sessionTotal = 0
        currentQuestionIndex = 1
        isRoundActive = true
        wrongGuessNote = null
        wrongGuessDirection = null
        nextFindItQuestion()
    }

    fun nextFindItQuestion() {
        val available = getAvailableNotes()
        findItTargetNote = available[Random.nextInt(available.size)]
        
        // Find strings where this note exists up to fret 12
        val positions = GuitarTheory.findNotePositions(findItTargetNote, maxFretSelected)
        if (positions.isNotEmpty()) {
            findItTargetString = positions.random().first
        } else {
            findItTargetString = Random.nextInt(1, 7)
        }
        
        findItSelectedPosition = null
        findItRevealCorrectPositions = false
        isAnswered = false
        wrongGuessNote = null
        wrongGuessDirection = null
        foundPositionsForCurrentNote = emptySet()
        
        if (allStringsChallengeActive) {
            allStringsChallengeFound = emptySet()
            feedbackMessage = "Find ${findItTargetNote} on ALL 6 strings! Progress: 0/6"
        } else {
            if (isLearnMode) {
                feedbackMessage = "Find ${findItTargetNote} on String ${findItTargetString} (Learn Mode)"
            } else {
                feedbackMessage = "Find ${findItTargetNote} on String ${findItTargetString} (Quiz Mode)"
            }
        }
    }

    fun handleFindItPositionSelected(stringIndex: Int, fret: Int) {
        val selectedNote = GuitarTheory.getNoteName(stringIndex, fret)
        val selectedMidi = GuitarTheory.strings.firstOrNull { it.index == stringIndex }?.openMidi?.plus(fret) ?: 0
        
        if (allStringsChallengeActive) {
            // Find note on all strings challenge is active
            if (selectedNote == findItTargetNote) {
                foundPositionsForCurrentNote = foundPositionsForCurrentNote + Pair(stringIndex, fret)
                val newFound = allStringsChallengeFound + stringIndex
                allStringsChallengeFound = newFound
                recordGuess(selectedNote, stringIndex, true)
                
                val targetCount = getTargetBarCount(findItTargetNote)
                if (foundPositionsForCurrentNote.size >= targetCount) {
                    isAnswered = true
                    wasCorrect = true
                    feedbackMessage = "Fantastic! You found all $targetCount positions for $findItTargetNote across all strings!"
                    sessionScore++
                    sessionTotal++
                } else {
                    feedbackMessage = "Correct! Found on String $stringIndex. Progress: ${foundPositionsForCurrentNote.size}/$targetCount positions."
                }
            } else {
                // Wrong guess in all strings challenge
                wrongGuessNote = selectedNote
                recordGuess(selectedNote, stringIndex, false)
                
                // Compare with correct position on this clicked string
                val correctTargetFretOnThisString = GuitarTheory.findNotePositions(findItTargetNote, 12)
                    .firstOrNull { it.first == stringIndex }?.second ?: 0
                val correctMidiOnThisString = GuitarTheory.strings.firstOrNull { it.index == stringIndex }?.openMidi?.plus(correctTargetFretOnThisString) ?: 0
                
                wrongGuessDirection = if (selectedMidi > correctMidiOnThisString) "higher" else "lower"
                feedbackMessage = "Incorrect! That is $selectedNote (${wrongGuessDirection}). Try again!"
            }
            return
        }

        // Standard Single-Position Find-it:
        if (isAnswered) return
        
        findItSelectedPosition = Pair(stringIndex, fret)
        isAnswered = true
        sessionTotal++
        
        // Find correct target MIDI for comparison
        val targetFret = GuitarTheory.findNotePositions(findItTargetNote, 12)
            .firstOrNull { it.first == findItTargetString }?.second ?: 0
        val targetMidi = GuitarTheory.strings.firstOrNull { it.index == findItTargetString }?.openMidi?.plus(targetFret) ?: 0
        
        if (selectedNote == findItTargetNote && stringIndex == findItTargetString) {
            foundPositionsForCurrentNote = foundPositionsForCurrentNote + Pair(stringIndex, fret)
            sessionScore++
            wasCorrect = true
            wrongGuessNote = null
            wrongGuessDirection = null
            feedbackMessage = "Correct! String $stringIndex, Fret $fret is indeed $selectedNote."
            recordGuess(selectedNote, stringIndex, true)
        } else {
            wasCorrect = false
            wrongGuessNote = selectedNote
            wrongGuessDirection = if (selectedMidi > targetMidi) "higher" else "lower"
            
            if (isLearnMode) {
                findItRevealCorrectPositions = true
            } else {
                findItRevealCorrectPositions = false // Keep hidden in Quiz
            }
            feedbackMessage = "Incorrect. That note is $selectedNote (${wrongGuessDirection})."
            recordGuess(selectedNote, stringIndex, false)
        }
    }

    fun advanceFindIt() {
        wrongGuessNote = null
        wrongGuessDirection = null
        
        if (currentQuestionIndex < totalQuestions) {
            currentQuestionIndex++
            nextFindItQuestion()
        } else {
            // Round finished
            saveScore("find_it", sessionScore, sessionTotal)
            isRoundActive = false
            allStringsChallengeActive = false
            feedbackMessage = "Session Finished! Score: $sessionScore/$sessionTotal"
        }
    }

    // 2. Name-it Mode Logic
    fun startNameItRound() {
        sessionScore = 0
        sessionTotal = 0
        currentQuestionIndex = 1
        isRoundActive = true
        nextNameItQuestion()
    }

    private fun nextNameItQuestion() {
        // Select a random position on fretboard
        val randomString = Random.nextInt(1, 7)
        val randomFret = Random.nextInt(0, maxFretSelected + 1)
        
        // Filter note if accidentals are off
        val noteName = GuitarTheory.getNoteName(randomString, randomFret)
        val isAccidental = noteName.contains("#")
        if (!includeAccidentals && isAccidental) {
            nextNameItQuestion() // retry
            return
        }

        nameItHighlightedPosition = Pair(randomString, randomFret)
        nameItSelectedAnswer = null
        isAnswered = false
        feedbackMessage = "What is the note at String $randomString, Fret $randomFret?"

        // Generate multiple choice options
        val correctAnswer = noteName
        val available = getAvailableNotes().toMutableList()
        available.remove(correctAnswer)
        available.shuffle()
        
        val wrongAnswers = available.take(3)
        val choicesList = (wrongAnswers + correctAnswer).shuffled()
        nameItChoices = choicesList
    }

    fun handleNameItAnswerSelected(answer: String) {
        if (isAnswered) return
        
        nameItSelectedAnswer = answer
        val correctAnswer = GuitarTheory.getNoteName(nameItHighlightedPosition.first, nameItHighlightedPosition.second)
        
        isAnswered = true
        sessionTotal++
        
        if (answer == correctAnswer) {
            sessionScore++
            wasCorrect = true
            feedbackMessage = "Correct! It is $correctAnswer."
        } else {
            wasCorrect = false
            feedbackMessage = "Incorrect. It is actually $correctAnswer."
        }
    }

    fun advanceNameIt() {
        if (currentQuestionIndex < totalQuestions) {
            currentQuestionIndex++
            nextNameItQuestion()
        } else {
            saveScore("name_it", sessionScore, sessionTotal)
            isRoundActive = false
            feedbackMessage = "Session Finished! Score: $sessionScore/$sessionTotal"
        }
    }

    // 3. Physical Guitar Input Mode Logic
    fun startGuitarRound() {
        sessionScore = 0
        sessionTotal = 0
        currentQuestionIndex = 1
        isRoundActive = true
        guitarHoldCount = 0
        
        // Start physical mic pitch detection
        pitchDetector?.startListening()
        nextGuitarQuestion()
    }

    fun stopGuitarRound() {
        pitchDetector?.stopListening()
        isRoundActive = false
    }

    private fun nextGuitarQuestion() {
        val available = getAvailableNotes()
        guitarTargetNote = available[Random.nextInt(available.size)]
        guitarHoldCount = 0
        isAnswered = false
        wrongGuessNote = null
        wrongGuessDirection = null
        foundPositionsForCurrentNote = emptySet()
        feedbackMessage = "Play a standard '${guitarTargetNote}' on your guitar!"
    }

    private fun handleLiveGuitarInput(detectedNote: String?) {
        if (isAnswered || !isRoundActive) return
        
        if (detectedNote != null) {
            if (detectedNote == guitarTargetNote) {
                guitarHoldCount++
                if (guitarHoldCount >= guitarHoldTarget) {
                    val correctPositions = GuitarTheory.findNotePositions(guitarTargetNote, 12)
                    val targetCount = getTargetBarCount(guitarTargetNote)
                    val nextUnfound = correctPositions.firstOrNull { it !in foundPositionsForCurrentNote }
                    
                    if (nextUnfound != null) {
                        foundPositionsForCurrentNote = foundPositionsForCurrentNote + nextUnfound
                        guitarHoldCount = 0 // Reset to let them play the next one
                        
                        val foundCount = foundPositionsForCurrentNote.size
                        if (foundCount >= targetCount) {
                            isAnswered = true
                            sessionTotal++
                            sessionScore++
                            wasCorrect = true
                            wrongGuessNote = null
                            wrongGuessDirection = null
                            feedbackMessage = "Amazing! You played all $targetCount positions for $guitarTargetNote across all strings!"
                            recordGuess(guitarTargetNote, 1, true)
                        } else {
                            feedbackMessage = "Great! Detected $guitarTargetNote. Progress: $foundCount/$targetCount positions."
                        }
                    } else {
                        // All found but maybe not marked answered yet
                        isAnswered = true
                        sessionTotal++
                        sessionScore++
                        wasCorrect = true
                        wrongGuessNote = null
                        wrongGuessDirection = null
                        feedbackMessage = "Amazing! You played all $targetCount positions for $guitarTargetNote!"
                        recordGuess(guitarTargetNote, 1, true)
                    }
                }
            } else {
                guitarHoldCount = maxOf(0, guitarHoldCount - 1)
                
                // Track wrong guess for red indicator feedback
                wrongGuessNote = detectedNote
                val targetIdx = GuitarTheory.chromaticScale.indexOf(guitarTargetNote)
                val detectedIdx = GuitarTheory.chromaticScale.indexOf(detectedNote)
                if (targetIdx != -1 && detectedIdx != -1) {
                    wrongGuessDirection = if (detectedIdx > targetIdx) "higher" else "lower"
                }
                recordGuess(detectedNote, 1, false)
            }
        }
    }

    fun skipGuitarQuestion() {
        if (isAnswered) return
        isAnswered = true
        sessionTotal++
        wasCorrect = false
        feedbackMessage = "Skipped! The target was ${guitarTargetNote}."
    }

    fun advanceGuitar() {
        if (currentQuestionIndex < totalQuestions) {
            currentQuestionIndex++
            nextGuitarQuestion()
        } else {
            saveScore("guitar", sessionScore, sessionTotal)
            stopGuitarRound()
            feedbackMessage = "Session Finished! Score: $sessionScore/$sessionTotal"
        }
    }

    // 4. Play-along Metronome Mode Logic
    fun startPlayAlong() {
        if (isPlayingAlong) return
        
        isPlayingAlong = true
        isRoundActive = true
        playAlongIndex = 0
        playAlongScore = 0
        playAlongFeedback = "Get Ready... 3, 2, 1, Go!"
        currentPlayedMatch = false
        
        // Start microphone listener
        pitchDetector?.startListening()
        
        // Calculate beat interval in ms
        val beatIntervalMs = (60.0 / tempoBpm * 1000.0).toLong()
        
        playAlongJob = viewModelScope.launch(Dispatchers.Default) {
            // Count-in for 4 beats
            for (i in 1..4) {
                viewModelScope.launch(Dispatchers.Main) {
                    playAlongFeedback = "Count-in: ${5 - i}"
                }
                delay(beatIntervalMs)
            }
            
            // Start sequence
            while (isPlayingAlong && playAlongIndex < playAlongSequence.size) {
                val targetNote = playAlongSequence[playAlongIndex]
                currentPlayedMatch = false
                
                // Beat animation loop (updates progress within this beat)
                val steps = 10
                val stepDelay = beatIntervalMs / steps
                for (s in 0 until steps) {
                    if (!isPlayingAlong) break
                    viewModelScope.launch(Dispatchers.Main) {
                        playAlongBeatProgress = s.toFloat() / steps.toFloat()
                        playAlongFeedback = "PLAY NOW: $targetNote"
                    }
                    delay(stepDelay)
                }
                
                // Evaluate beat
                viewModelScope.launch(Dispatchers.Main) {
                    sessionTotal++
                    if (currentPlayedMatch) {
                        playAlongScore++
                        playAlongFeedback = "Perfect Match!"
                    } else {
                        playAlongFeedback = "Missed Beat!"
                    }
                    
                    if (playAlongIndex < playAlongSequence.size - 1) {
                        playAlongIndex++
                    } else {
                        // End of sequence
                        finishPlayAlong()
                    }
                }
            }
        }
    }

    private fun handleLivePlayAlongInput(detectedNote: String?) {
        if (!isPlayingAlong || playAlongIndex >= playAlongSequence.size) return
        val currentTarget = playAlongSequence[playAlongIndex]
        
        if (detectedNote != null && detectedNote == currentTarget) {
            currentPlayedMatch = true
        }
    }

    fun stopPlayAlong() {
        isPlayingAlong = false
        playAlongJob?.cancel()
        playAlongJob = null
        pitchDetector?.stopListening()
        isRoundActive = false
    }

    private fun finishPlayAlong() {
        isPlayingAlong = false
        playAlongJob?.cancel()
        playAlongJob = null
        pitchDetector?.stopListening()
        
        saveScore("play_along", playAlongScore, playAlongSequence.size)
        playAlongFeedback = "Finished! Score: $playAlongScore / ${playAlongSequence.size}"
    }

    override fun onCleared() {
        super.onCleared()
        stopGuitarRound()
        stopPlayAlong()
    }
}

class FretboardViewModelFactory(
    private val application: Application,
    private val repository: ScoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FretboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FretboardViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
