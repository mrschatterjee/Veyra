package com.veyra.app

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object VeyraGlassDialog {
    private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
    private fun background(): GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(28, 15, 64), Color.rgb(88, 43, 156))).apply { cornerRadius = 28f }
    private fun text(context: Context, value: String, size: Float, alpha: Int = 255, bold: Boolean = false): TextView = TextView(context).apply { text = value; textSize = size; setTextColor(Color.argb(alpha, 255, 255, 255)); if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD) }
    private fun button(context: Context, label: String): TextView = text(context, label, 13f, 255, true).apply { gravity = Gravity.CENTER; background = GradientDrawable().apply { setColor(Color.argb(48, 255, 255, 255)); cornerRadius = dp(context, 16).toFloat(); setStroke(dp(context, 1), Color.argb(75, 225, 210, 255)) } }
    private fun open(context: Context, title: String, message: String, editor: EditText? = null, buttons: List<Pair<String, () -> Unit>>): Dialog {
        val dialog = Dialog(context); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(context, 24), dp(context, 24), dp(context, 24), dp(context, 22)); background = background() }
        root.addView(text(context, title, 22f, 255, true), LinearLayout.LayoutParams(-1, -2))
        if (message.isNotBlank()) root.addView(text(context, message, 13f, 190).apply { setPadding(0, dp(context, 10), 0, 0) }, LinearLayout.LayoutParams(-1, -2))
        if (editor != null) { editor.setTextColor(Color.WHITE); editor.setHintTextColor(Color.argb(125, 255, 255, 255)); editor.background = GradientDrawable().apply { setColor(Color.argb(38, 255, 255, 255)); cornerRadius = dp(context, 16).toFloat(); setStroke(dp(context, 1), Color.argb(70, 225, 210, 255)) }; editor.setPadding(dp(context, 14), dp(context, 10), dp(context, 14), dp(context, 10)); root.addView(editor, LinearLayout.LayoutParams(-1, dp(context, 92)).apply { topMargin = dp(context, 16) }) }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; setPadding(0, dp(context, 20), 0, 0) }
        buttons.forEachIndexed { index, pair -> val b = button(context, pair.first); b.setOnClickListener { pair.second(); dialog.dismiss() }; row.addView(b, LinearLayout.LayoutParams(dp(context, if (buttons.size >= 3) 96 else 116), dp(context, 46)).apply { if (index > 0) leftMargin = dp(context, 8) }) }
        root.addView(row, LinearLayout.LayoutParams(-1, dp(context, 66))); dialog.setContentView(root); dialog.window?.setBackgroundDrawableResource(android.R.color.transparent); dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); dialog.window?.setDimAmount(0.76f); dialog.setCanceledOnTouchOutside(true); dialog.setOnShowListener { dialog.window?.setLayout((context.resources.displayMetrics.widthPixels * 0.92f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT) }; return dialog
    }
    fun showInfo(context: Context, title: String, message: String) = open(context, title, message, buttons = listOf("OK" to {})).show()
    fun showConfirm(context: Context, title: String, message: String, confirm: String, onConfirm: () -> Unit) = open(context, title, message, buttons = listOf("CANCEL" to {}, confirm to onConfirm)).show()
    fun showChoice(context: Context, title: String, message: String, first: String, second: String, onFirst: () -> Unit, onSecond: () -> Unit) = open(context, title, message, buttons = listOf(first to onFirst, second to onSecond)).show()
    fun showChoice3(context: Context, title: String, message: String, first: String, second: String, third: String, onFirst: () -> Unit, onSecond: () -> Unit, onThird: () -> Unit) = open(context, title, message, buttons = listOf(first to onFirst, second to onSecond, third to onThird)).show()
    fun showInput(context: Context, title: String, hint: String, value: String = "", onSave: (String) -> Unit) { val input = EditText(context).apply { this.hint = hint; setSingleLine(false); minLines = 2; setText(value); setSelection(text.length) }; open(context, title, "", input, listOf("CANCEL" to {}, "SAVE" to { input.text.toString().trim().takeIf { it.isNotEmpty() }?.let(onSave) })).show() }
}
