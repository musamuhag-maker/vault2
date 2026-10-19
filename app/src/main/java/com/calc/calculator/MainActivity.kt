package com.calc.calculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat

class MainActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private var expr = ""
    private val ops = listOf("+", "−", "×", "÷")

    private val rows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bg = Color.parseColor("#202124")
        window.statusBarColor = bg
        window.navigationBarColor = bg

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        display = TextView(this).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            setTextColor(Color.WHITE)
            setPadding(40, 40, 40, 40)
            text = "0"
            maxLines = 2
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 28, 72, 2, TypedValue.COMPLEX_UNIT_SP
            )
        }
        root.addView(display, LinearLayout.LayoutParams(MATCH_PARENT, 0, 2f))

        val pad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 24)
        }
        for (row in rows) {
            val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (label in row) {
                val lp = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f).apply { setMargins(8, 8, 8, 8) }
                r.addView(makeButton(label), lp)
            }
            pad.addView(r, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        }
        root.addView(pad, LinearLayout.LayoutParams(MATCH_PARENT, 0, 4f))
        setContentView(root)
    }

    private fun makeButton(label: String): Button = Button(this).apply {
        text = label
        isAllCaps = false
        minHeight = 0
        minWidth = 0
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
        setTextColor(Color.WHITE)
        setBackgroundColor(
            when {
                label == "=" -> Color.parseColor("#FF9800")
                label in ops -> Color.parseColor("#5F6368")
                label in listOf("C", "⌫", "%") -> Color.parseColor("#3C4043")
                else -> Color.parseColor("#2D2F31")
            }
        )
        setOnClickListener { press(label) }
    }

    private fun press(label: String) {
        if (expr == "Error") expr = ""
        when (label) {
            "C" -> expr = ""
            "⌫" -> expr = expr.dropLast(1)
            "=" -> { equals(); return }
            "+", "−", "×", "÷" -> {
                if (expr.isEmpty()) {
                    if (label == "−") expr = "−"
                } else if (ops.contains(expr.last().toString())) {
                    expr = expr.dropLast(1) + label
                } else expr += label
            }
            "%" -> if (expr.isNotEmpty() && expr.last().isDigit()) expr += "%"
            "." -> {
                val n = expr.takeLastWhile { it.isDigit() || it == '.' }
                if (!n.contains('.')) expr += if (n.isEmpty()) "0." else "."
            }
            else -> expr += label
        }
        show()
    }

    private fun equals() {
        val code = getSharedPreferences("c", 0).getString("code", "1234")
        if (expr == code) {
            expr = ""
            show()
            startActivity(Intent(this, VaultActivity::class.java))
            return
        }
        if (expr.isEmpty()) return
        expr = try {
            Calc.format(Calc.eval(expr.trimEnd('+', '−', '×', '÷')))
        } catch (e: Exception) {
            "Error"
        }
        show()
    }

    private fun show() {
        display.text = if (expr.isEmpty()) "0" else expr
    }
}
