package com.example.bookmarket

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val finishSplash = Runnable { showWelcome() }
    private var showingSplash = false
    private var splashEndsAt = 0L
    private var logoAnimation: AnimationDrawable? = null
    private var readerAnimation: AnimationDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BookMarket)
        super.onCreate(savedInstanceState)
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)

        val now = SystemClock.elapsedRealtime()
        val deadline = savedInstanceState?.getLong(SPLASH_END_KEY, 0L) ?: (now + SPLASH_DURATION)
        if (deadline - now in 1L..SPLASH_DURATION) {
            splashEndsAt = deadline
            showingSplash = true
            setContentView(R.layout.activity_splash)
            logoAnimation = findViewById<ImageView>(R.id.splash_logo).drawable as AnimationDrawable
            readerAnimation = findViewById<ImageView>(R.id.splash_reader).drawable as AnimationDrawable
        } else {
            showWelcome()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!showingSplash) return

        val remaining = splashEndsAt - SystemClock.elapsedRealtime()
        if (remaining <= 0) {
            showWelcome()
        } else {
            logoAnimation?.start()
            readerAnimation?.start()
            mainHandler.postDelayed(finishSplash, remaining)
        }
    }

    override fun onStop() {
        mainHandler.removeCallbacks(finishSplash)
        logoAnimation?.stop()
        readerAnimation?.stop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(SPLASH_END_KEY, if (showingSplash) splashEndsAt else 0L)
        super.onSaveInstanceState(outState)
    }

    private fun showWelcome() {
        mainHandler.removeCallbacks(finishSplash)
        logoAnimation?.stop()
        readerAnimation?.stop()
        logoAnimation = null
        readerAnimation = null
        showingSplash = false

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }
    }

    private companion object {
        const val SPLASH_DURATION = 2_400L
        const val SPLASH_END_KEY = "splash_ends_at"
    }
}
