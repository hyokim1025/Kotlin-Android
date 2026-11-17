package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MyPageActivity : CheckoutActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCheckout(R.layout.activity_mypage, "나의페이지")

        findViewById<Button>(R.id.open_memo).setOnClickListener {
            startActivity(Intent(this, MemoActivity::class.java))
        }
    }
}
