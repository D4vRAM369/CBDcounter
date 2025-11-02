package com.d4vram.cbdcounter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.noties.markwon.Markwon

class NoteBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onNoteChanged(date: String)
    }

    private val dateArg by lazy { requireArguments().getString(ARG_DATE)!! }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(context)
            .inflate(R.layout.dialog_note_bottomsheet, null, false)
        dialog.setContentView(v)

        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        val etNote = v.findViewById<TextInputEditText>(R.id.etNote)
        val preview = v.findViewById<TextView>(R.id.preview)
        val btnSave = v.findViewById<MaterialButton>(R.id.btnSave)
        val btnDelete = v.findViewById<MaterialButton>(R.id.btnDelete)
        val markwon = Markwon.create(requireContext())

        tvTitle.text = getString(R.string.note_title_with_date, dateArg)
        val initialNote = Prefs.getNote(requireContext(), dateArg) ?: ""
        etNote.setText(initialNote)
        renderMarkdown(markwon, preview, initialNote)

        etNote.addTextChangedListener { text ->
            renderMarkdown(markwon, preview, text?.toString().orEmpty())
        }

        btnSave.setOnClickListener {
            val text = etNote.text?.toString()?.trim()
            // si está vacío, elimina; si no, guarda
            Prefs.setNote(requireContext(), dateArg, text.takeUnless { it.isNullOrEmpty() })
            (parentFragment as? Listener ?: activity as? Listener)?.onNoteChanged(dateArg)
            dismiss()
        }

        btnDelete.setOnClickListener {
            Prefs.setNote(requireContext(), dateArg, null) // elimina la nota
            (parentFragment as? Listener ?: activity as? Listener)?.onNoteChanged(dateArg)
            dismiss()
        }

        return dialog
    }

    private fun renderMarkdown(markwon: Markwon, target: TextView, content: String) {
        if (content.isBlank()) {
            target.visibility = View.GONE
        } else {
            target.visibility = View.VISIBLE
            markwon.setMarkdown(target, content)
        }
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        fun new(date: String) = NoteBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_DATE, date) }
        }
    }
}
