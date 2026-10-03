package com.example.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceMediaManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null

    fun startRecording(): File? {
        return try {
            val audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            currentAudioFile = audioFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(32000)
                setAudioSamplingRate(16000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioFile
        } catch (e: Exception) {
            null
        }
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentAudioFile
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaRecorder = null
            currentAudioFile?.delete()
            currentAudioFile = null
        }
    }
}
