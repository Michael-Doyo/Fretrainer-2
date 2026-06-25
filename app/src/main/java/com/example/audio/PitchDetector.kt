package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.GuitarTheory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class PitchDetector(
    private val onPitchDetected: (frequency: Double, noteName: String?, rms: Double) -> Unit
) {
    private val sampleRate = 44100
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    
    // Buffer size: 4096 samples provides excellent resolution for low frequencies (guitar E2)
    private val bufferSize = 4096 
    
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var listeningJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Range of interest for guitar (roughly 70Hz to 1000Hz)
    // Low E2 is ~82.4Hz, High E5 is ~659.2Hz
    private val minFrequency = 70.0
    private val maxFrequency = 1000.0
    private val minTau = (sampleRate / maxFrequency).toInt() // ~44 samples
    private val maxTau = (sampleRate / minFrequency).toInt() // ~630 samples

    // Noise threshold (RMS)
    private val silenceThreshold = 100.0 // Noise gate threshold for short values

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (isListening) return
        
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val actualBufferSize = maxOf(minBufferSize, bufferSize)
            
            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                actualBufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("PitchDetector", "AudioRecord initialization failed.")
                return
            }

            audioRecord?.startRecording()
            isListening = true
            
            listeningJob = coroutineScope.launch {
                val audioBuffer = ShortArray(bufferSize)
                
                while (isActive && isListening) {
                    val readResult = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                    if (readResult > 0) {
                        processAudio(audioBuffer, readResult)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PitchDetector", "Error starting audio record", e)
        }
    }

    fun stopListening() {
        isListening = false
        listeningJob?.cancel()
        listeningJob = null
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("PitchDetector", "Error stopping audio record", e)
        } finally {
            audioRecord = null
        }
    }

    private fun processAudio(buffer: ShortArray, size: Int) {
        // 1. Calculate RMS to filter out silence/static background noise
        var sumSquares = 0.0
        for (i in 0 until size) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rms = sqrt(sumSquares / size)
        
        if (rms < silenceThreshold) {
            // Under threshold -> Silence
            onPitchDetected(0.0, null, rms)
            return
        }

        // 2. Perform Autocorrelation
        val r = DoubleArray(maxTau + 1)
        
        // Compute autocorrelation for each lag (tau) within guitar frequency range
        for (tau in minTau..maxTau) {
            var sum = 0.0
            for (t in 0 until (size - tau)) {
                sum += buffer[t].toDouble() * buffer[t + tau].toDouble()
            }
            r[tau] = sum
        }

        // 3. Peak picking with parabolic interpolation or simple max finding
        var bestTau = -1
        var maxVal = -Double.MAX_VALUE
        
        // Find the absolute maximum peak in the autocorrelation array
        for (tau in minTau..maxTau) {
            if (r[tau] > maxVal) {
                maxVal = r[tau]
                bestTau = tau
            }
        }

        // Check if the correlation peak is strong enough relative to r[0] (which we approximate)
        var zeroLagSum = 0.0
        for (i in 0 until size) {
            zeroLagSum += buffer[i].toDouble() * buffer[i].toDouble()
        }
        
        if (zeroLagSum == 0.0) {
            onPitchDetected(0.0, null, rms)
            return
        }

        val correlationCoeff = maxVal / zeroLagSum
        
        // A correlation coefficient above 0.35 suggests a strong periodic fundamental signal (guitar note)
        if (bestTau != -1 && correlationCoeff > 0.35) {
            val detectedFrequency = sampleRate.toDouble() / bestTau
            
            // Validate that the frequency sits in the correct range
            if (detectedFrequency in minFrequency..maxFrequency) {
                val noteName = GuitarTheory.frequencyToNoteName(detectedFrequency)
                onPitchDetected(detectedFrequency, noteName, rms)
                return
            }
        }

        // Default: No clear note pitch detected
        onPitchDetected(0.0, null, rms)
    }
}
