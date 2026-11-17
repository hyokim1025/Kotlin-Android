package com.example.customdialogproject

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            showCalculator()
        }
    }

    private fun showCalculator() {
        val content = layoutInflater.inflate(R.layout.dialog_calculate, null)
        val calculator = Calculator()
        val expression = content.findViewById<TextView>(R.id.calculator_expression)
        val value = content.findViewById<TextView>(R.id.calculator_value)

        connectKeys(content) { key ->
            calculator.press(key)
            expression.text = calculator.expression
            value.text = calculator.display
        }

        val dialog = AlertDialog.Builder(this)
            .setView(content)
            .create()

        content.findViewById<Button>(R.id.close_calculator).setOnClickListener {
            dialog.dismiss()
        }

        val density = resources.displayMetrics.density
        val targetWidth = (340 * density).roundToInt()
        val availableWidth = window.decorView.width - (32 * density).roundToInt()

        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = if (availableWidth > 0) minOf(targetWidth, availableWidth) else targetWidth
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun connectKeys(view: View, onKey: (String) -> Unit) {
        when (view) {
            is ImageButton -> view.setOnClickListener { onKey(view.tag as String) }
            is ViewGroup -> {
                for (index in 0 until view.childCount) {
                    connectKeys(view.getChildAt(index), onKey)
                }
            }
        }
    }
}

internal class Calculator {
    private var entry = "0"
    private var leftOperand: BigDecimal? = null
    private var pendingOperator: String? = null
    private var replaceEntry = true
    private var hasError = false

    var expression = ""
        private set

    val display: String
        get() = if (hasError) "오류" else entry

    fun press(key: String) {
        if (hasError) {
            val canReset = key == "AC" || key == "C" || key == "." ||
                key.singleOrNull()?.isDigit() == true
            if (!canReset) return
            clearAll()
        }

        when (key) {
            "AC" -> clearAll()
            "C" -> {
                entry = "0"
                replaceEntry = false
                if (pendingOperator == null) expression = ""
            }
            "←" -> {
                if (!replaceEntry) entry = entry.dropLast(1).ifEmpty { "0" }
            }
            "+", "−", "×", "÷" -> selectOperator(key)
            "=" -> if (!replaceEntry) evaluatePending()
            else -> append(key)
        }
    }

    private fun append(key: String) {
        if (replaceEntry) {
            entry = "0"
            replaceEntry = false
            if (pendingOperator == null) expression = ""
        }

        if (key == ".") {
            if ('.' !in entry) entry += "."
        } else if (entry.length < 15) {
            entry = if (entry == "0") key else entry + key
        }
    }

    private fun selectOperator(operator: String) {
        if (pendingOperator != null && !replaceEntry) evaluatePending()
        if (hasError) return

        val number = leftOperand ?: entry.toBigDecimal()
        leftOperand = number
        pendingOperator = operator
        expression = format(number) + " " + operator
        replaceEntry = true
    }

    private fun evaluatePending() {
        val left = leftOperand ?: return
        val operator = pendingOperator ?: return
        val right = entry.toBigDecimal()

        if (operator == "÷" && right.signum() == 0) {
            clearAll()
            expression = "0으로 나눌 수 없습니다"
            hasError = true
            return
        }

        val result = when (operator) {
            "+" -> left.add(right, MathContext.DECIMAL64)
            "−" -> left.subtract(right, MathContext.DECIMAL64)
            "×" -> left.multiply(right, MathContext.DECIMAL64)
            "÷" -> left.divide(right, 10, RoundingMode.HALF_UP)
            else -> error("알 수 없는 연산자")
        }

        entry = format(result)
        expression = listOf(format(left), operator, format(right), "=", entry).joinToString(" ")
        leftOperand = null
        pendingOperator = null
        replaceEntry = true
    }

    private fun clearAll() {
        entry = "0"
        leftOperand = null
        pendingOperator = null
        replaceEntry = true
        expression = ""
        hasError = false
    }

    private fun format(number: BigDecimal): String {
        val clean = number.stripTrailingZeros()
        return if (clean.scale() in -12..12) clean.toPlainString() else clean.toString()
    }
}
