package com.example.bookmarket

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

abstract class CheckoutActivity : AppCompatActivity() {
    protected fun showCheckout(layout: Int, title: String) {
        enableEdgeToEdge()
        setContentView(layout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkout_root)) { view, insets ->
            val padding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom)
            insets
        }

        setSupportActionBar(findViewById<Toolbar>(R.id.checkout_toolbar))
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
