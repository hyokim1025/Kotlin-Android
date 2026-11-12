package com.example.tablelayoutproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val turtleInput = findViewById<EditText>(R.id.turtle_count)
        val fishInput = findViewById<EditText>(R.id.fish_count)
        val resultButton = findViewById<Button>(R.id.result_button)

        resultButton.setOnClickListener {
            val turtleCount = turtleInput.text.toString().toIntOrNull()
            val fishCount = fishInput.text.toString().toIntOrNull()

            val message = when {
                turtleCount == null || fishCount == null -> "두 칸에 숫자를 입력하세요"
                turtleCount == 2 && fishCount == 5 -> "맞았습니다"
                else -> "틀렸습니다"
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}