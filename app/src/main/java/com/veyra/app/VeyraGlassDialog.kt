package com.veyra.app

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object VeyraGlassDialog {
    private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    private fun background(): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(Color.rgb(35, 20, 78), Color.rgb(82, 43, 145))
    ).apply { cornerRadius = 28f }

    private fun text(context: Context, value: String, size: Float, alpha: Int = 255, bold: Boolean = false): TextView = TextView(context).apply {
        text = value
        textSize = size
        setTextColor(Color.argb(alpha, 255, 255, 255))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun button(context: Context, label: String, onClick: () -> Unit): TextView = text(context, label, 13f, 255, true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(context, 16), 0, dp(context, 16), 0)
        background = GradientDrawable().apply {
            setColor(Color.argb(42, 255, 255, 255))
            cornerRadius = dp(context, 16).toFloat()
            setStroke(dp(context, 1), Color.argb(70, 220, 205, 255))
        }
        setOnClickListener { onClick() }
    }

    private fun open(context: Context, title: String, message: String, editor: EditText? = null, buttons: List<Pair<String, () -> Unit>>): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 24), dp(context, 24), dp(context, 24), dp(context, 22))
            background = background()
        }
        root.addView(text(context, title, 22f, 255, true), LinearLayout.LayoutParams(-1, -2))
        if (message.isNotBlank()) root.addView(text(context, message, 13f, 185).apply { setPadding(0, dp(context, 10), 0, 0) }, LinearLayout.LayoutParams(-1, -2))
        if (editor != null) {
            editor.setTextColor(Color.WHITE)
            editor.setHintTextColor(Color.argb(120, 255, 255, 255))
            editor.background = GradientDrawable().apply {
                setColor(Color.argb(35, 255, 255, 255)); cornerRadius = dp(context, 16).toFloat()
                setStroke(dp(context, 1), Color.argb(65, 220, 205, 255))
            }
            editor.setPadding(dp(context, 14), dp(context, 10), dp(context, 14), dp(context, 10))
            root.addView(editor, LinearLayout.LayoutParams(-1, dp(context, 92)).apply { topMargin = dp(context, 16) })
        }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; setPadding(0, dp(context, 20), 0, 0) }
        buttons.forEachIndexed { index, pair ->
            val b = button(context, pair.first, pair.second)
            row.addView(b, LinearLayout.LayoutParams(dp(context, 116), dp(context, 46)).apply { if (index > 0) leftMargin = dp(context, 10) })
        }
        root.addView(row, LinearLayout.LayoutParams(-1, dp(context, 66)))
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.72f)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    fun showInfo(context: Context, title: String, message: String) {
        val d = open(context, title, message, buttons = listOf("OK" to {}))
        d.show(); d.findButtonLike()?.setOnClickListener { d.dismiss() }
    }

    fun showConfirm(context: Context, title: String, message: String, confirm: String, onConfirm: () -> Unit) {
        val d = open(context, title, message, buttons = listOf("CANCEL" to {}, confirm to { onConfirm(); d.dismiss() }))
        d.show(); d.bindCancelFirst()
    }

    fun showChoice(context: Context, title: String, message: String, first: String, second: String, onFirst: () -> Unit, onSecond: () -> Unit) {
        val d = open(context, title, message, buttons = listOf(first to { onFirst(); d.dismiss() }, second to { onSecond(); d.dismiss() }))
        d.show()
    }

    fun showInput(context: Context, title: String, hint: String, value: String = "", onSave: (String) -> Unit) {
        val input = EditText(context).apply { this.hint = hint; setSingleLine(false); minLines = 2; setText(value); setSelection(text.length) }
        val d = open(context, title, "", input, listOf("CANCEL" to {}, "SAVE" to { input.text.toString().trim().takeIf { it.isNotEmpty() }?.let(onSave); d.dismiss() }))
        d.show(); d.bindCancelFirst(); input.requestFocus()
    }

    private fun Dialog.findButtonLike(): View? = null
    private fun Dialog.bindCancelFirst() { }
}
