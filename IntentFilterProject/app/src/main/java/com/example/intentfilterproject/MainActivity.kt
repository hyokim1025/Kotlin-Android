package com.example.intentfilterproject

import android.content.ActivityNotFoundException
import android.content.Intent
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

        val root = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val padding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom)
            insets
        }

        val emailInput = findViewById<EditText>(R.id.email_input)
        val subjectInput = findViewById<EditText>(R.id.subject_input)
        val bodyInput = findViewById<EditText>(R.id.body_input)
        val sendButton = findViewById<Button>(R.id.send_button)

        sendButton.setOnClickListener {
            val recipient = emailInput.text.toString().trim()

            if (recipient.isEmpty()) {
                emailInput.error = "받는 사람을 입력하세요"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subjectInput.text.toString())
                putExtra(Intent.EXTRA_TEXT, bodyInput.text.toString())
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "앱을 선택하세요"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "메일 앱을 열 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}