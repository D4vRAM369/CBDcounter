package com.d4vram.cbdcounter

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.io.File

class VoiceNoteBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onVoiceNoteChanged(date: String)
    }

    private val dateArg by lazy { requireArguments().getString(ARG_DATE)!! }
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var isRecording = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_voice_note, null, false)
        dialog.setContentView(v)

        val btnRecord = v.findViewById<MaterialButton>(R.id.btnRecord)
        val btnPlay = v.findViewById<MaterialButton>(R.id.btnPlay)
        val btnDelete = v.findViewById<MaterialButton>(R.id.btnDeleteVoice)
        val waveform = v.findViewById<TextView>(R.id.waveformIndicator)

        val audioPath = Prefs.getVoiceNotePath(requireContext(), dateArg)

        if (File(audioPath).exists()) {
            btnPlay.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
        }

        btnRecord.setOnClickListener {
            if (!isRecording) startRecording(audioPath, btnRecord, waveform)
            else stopRecording(btnRecord, waveform, btnPlay, btnDelete)
        }

        btnPlay.setOnClickListener { playAudio(audioPath) }

        btnDelete.setOnClickListener {
            File(audioPath).delete()
            (activity as? Listener)?.onVoiceNoteChanged(dateArg)
            dismiss()
        }

        return dialog
    }

    private fun startRecording(path: String, btn: MaterialButton, wave: TextView) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) return

        recorder = MediaRecorder(requireContext()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(path)
            prepare()
            start()
        }
        isRecording = true
        btn.text = "⏹ Stop"
        wave.visibility = View.VISIBLE
    }

    private fun stopRecording(
        btn: MaterialButton, wave: TextView,
        btnPlay: MaterialButton, btnDelete: MaterialButton
    ) {
        recorder?.stop()
        recorder?.release()
        recorder = null
        isRecording = false
        btn.text = "🎙️"
        wave.visibility = View.GONE
        btnPlay.visibility = View.VISIBLE
        btnDelete.visibility = View.VISIBLE
        (activity as? Listener)?.onVoiceNoteChanged(dateArg)
    }

    private fun playAudio(path: String) {
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recorder?.release()
        player?.release()
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        fun new(date: String) = VoiceNoteBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_DATE, date) }
        }
    }
}
