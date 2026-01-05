package com.d4vram.cbdcounter

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class DayModalFragment : DialogFragment() {

    private var date: String? = null
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var audioSeekBar: SeekBar
    private lateinit var playAudioButton: MaterialButton
    private lateinit var deleteAudioButton: MaterialButton
    private lateinit var audioControlsLayout: android.widget.LinearLayout


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

        fun newInstance(date: String): DayModalFragment {
            return DayModalFragment().apply {
                arguments = Bundle().apply {
                    putString("date", date)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        date = arguments?.getString("date")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_day_modal, null)

        val modalTitle = view.findViewById<TextView>(R.id.modalTitle)
        val takesLabel = view.findViewById<TextView>(R.id.takesLabel)
        val noteEditText = view.findViewById<TextInputEditText>(R.id.noteEditText)
        val recordAudioButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.recordAudioButton)
        val saveButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        val audioStatus = view.findViewById<TextView>(R.id.audioStatus)
        audioSeekBar = view.findViewById(R.id.audioSeekBar)
        playAudioButton = view.findViewById(R.id.playAudioButton)
        deleteAudioButton = view.findViewById(R.id.deleteAudioButton)
        audioControlsLayout = view.findViewById(R.id.audioControlsLayout)


        date?.let { d ->
            modalTitle.text = "Día: $d"

            // Load count
            val count = requireContext().getSharedPreferences("CBDCounter", android.content.Context.MODE_PRIVATE)
                .getInt("count_$d", 0)
            takesLabel.text = "Tomas: $count"

            // Load note
            noteEditText.setText(Prefs.getNote(requireContext(), d) ?: "")

            // Check if audio exists
            audioFile = getAudioFile(d)
            if (audioFile?.exists() == true) {
                audioStatus.text = "Audio grabado disponible"
                audioSeekBar.visibility = View.VISIBLE
                audioControlsLayout.visibility = View.VISIBLE
                setupAudioPlayer(audioFile!!)
            } else {
                audioSeekBar.visibility = View.GONE
                audioControlsLayout.visibility = View.GONE
            }
        }

        recordAudioButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                recordAudioButton.text = "Grabar Audio"
                audioStatus.text = "Grabación detenida"
            } else {
                startRecording()
                recordAudioButton.text = "Detener Grabación"
                audioStatus.text = "Grabando..."
            }
        }



        playAudioButton.setOnClickListener {
            togglePlayback()
        }

        deleteAudioButton.setOnClickListener {
            audioFile?.delete()
            audioFile = null
            mediaPlayer?.release()
            mediaPlayer = null
            audioStatus.text = ""
            audioSeekBar.visibility = View.GONE
            audioControlsLayout.visibility = View.GONE
            Toast.makeText(context, "Audio borrado", Toast.LENGTH_SHORT).show()
        }



        saveButton.setOnClickListener {
            date?.let { d ->
                val note = noteEditText.text?.toString()?.trim()
                Prefs.setNote(requireContext(), d, if (note.isNullOrEmpty()) null else note)

                // Notify listeners (like MainActivity or StatsActivity)
                (parentFragment as? NoteBottomSheet.Listener ?: activity as? NoteBottomSheet.Listener)?.onNoteChanged(d)

                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()
    }

    private fun getAudioFile(date: String): File {
        val dir = File(requireContext().filesDir, "audios")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "audio_$date.3gp")
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        date?.let { d ->
            try {
                val audioFile = getAudioFile(d)
                if (!audioFile.parentFile?.exists()!!) {
                    audioFile.parentFile?.mkdirs()
                }
                mediaRecorder = android.media.MediaRecorder(requireContext()).apply {
                    setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                    setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFile.absolutePath)
                    prepare()
                    start()
                }
                isRecording = true
                Toast.makeText(context, "Grabación iniciada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al iniciar grabación: ${e.message} (Código: ${e.cause})", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                Toast.makeText(context, "Grabación guardada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al detener grabación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(context, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setupAudioPlayer(file: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    updatePlayButtonText()
                    audioSeekBar.setProgress(0)
                }
            }
            val duration = mediaPlayer!!.duration
            audioSeekBar.setMax(duration)
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            audioSeekBar.setProgress(it.currentPosition)
                            handler.postDelayed(this, 100)
                        }
                    }
                }
            }
            handler.post(runnable)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar audio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.start()
            }
            updatePlayButtonText()
        }
    }

    private fun updatePlayButtonText() {
        mediaPlayer?.let {
            playAudioButton.text = if (it.isPlaying) "⏸️ Pause" else "▶️ Play"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}