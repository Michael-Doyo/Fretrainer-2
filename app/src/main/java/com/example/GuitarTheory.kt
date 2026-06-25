package com.example

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

object GuitarTheory {
    val chromaticScale = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    data class GuitarString(
        val index: Int, // 1 to 6 (1 = High E, 6 = Low E)
        val name: String,
        val openMidi: Int
    )

    val strings = listOf(
        GuitarString(1, "E", 64), // High E
        GuitarString(2, "B", 59),
        GuitarString(3, "G", 55),
        GuitarString(4, "D", 50),
        GuitarString(5, "A", 45),
        GuitarString(6, "E", 40)  // Low E
    )

    // Gets the note name for a specific string and fret
    fun getNoteName(stringIndex: Int, fret: Int): String {
        val string = strings.firstOrNull { it.index == stringIndex } ?: return "C"
        val midi = string.openMidi + fret
        return chromaticScale[midi % 12]
    }

    // Gets the frequency for a specific string and fret
    fun getFrequency(stringIndex: Int, fret: Int): Double {
        val string = strings.firstOrNull { it.index == stringIndex } ?: return 0.0
        val midi = string.openMidi + fret
        return midiToFrequency(midi)
    }

    fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69.0) / 12.0)
    }

    // Converts a detected frequency to the nearest MIDI note number
    fun frequencyToMidi(frequency: Double): Int {
        if (frequency <= 20.0) return -1
        val rawMidi = 12.0 * log2(frequency / 440.0) + 69.0
        return rawMidi.roundToInt()
    }

    // Converts a detected frequency to a note name
    fun frequencyToNoteName(frequency: Double): String? {
        val midi = frequencyToMidi(frequency)
        if (midi < 0) return null
        return chromaticScale[midi % 12]
    }

    // Returns a list of all matching fretboard positions (string, fret) for a given note name (e.g. "E")
    fun findNotePositions(noteName: String, maxFret: Int = 12): List<Pair<Int, Int>> {
        val cleanNote = noteName.uppercase().trim()
        val positions = mutableListOf<Pair<Int, Int>>()
        for (string in strings) {
            for (fret in 0..maxFret) {
                if (getNoteName(string.index, fret) == cleanNote) {
                    positions.add(Pair(string.index, fret))
                }
            }
        }
        return positions
    }
}
