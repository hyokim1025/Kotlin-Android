package com.example.bookmarket

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.Calendar

class ShippingActivity : CheckoutActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCheckout(R.layout.activity_shipping, "배송정보")

        val name = findViewById<EditText>(R.id.shipping_name)
        val date = findViewById<EditText>(R.id.shipping_date)
        val phone = findViewById<EditText>(R.id.shipping_phone)
        val zip = findViewById<EditText>(R.id.shipping_zip)
        val address = findViewById<EditText>(R.id.shipping_address)

        val calendar = Calendar.getInstance()
        date.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    date.setText("%d년 %d월 %d일".format(year, month + 1, day))
                    date.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        findViewById<Button>(R.id.shipping_cancel).setOnClickListener { finish() }
        findViewById<Button>(R.id.shipping_submit).setOnClickListener {
            val fields = listOf(name, date, phone, zip, address)
            val empty = fields.firstOrNull { it.text.isBlank() }
            if (empty != null) {
                empty.error = "필수 항목입니다"
                if (empty == date) date.performClick() else empty.requestFocus()
                return@setOnClickListener
            }

            val selected = CartStore(this).lines().filter { it.selected }
            if (selected.isEmpty()) {
                Toast.makeText(this, "주문할 도서를 선택하세요", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            startActivity(Intent(this, OrderActivity::class.java).apply {
                putExtra(OrderActivity.EXTRA_SHIPPING, fields.map { it.text.toString().trim() }.toTypedArray())
                putExtra(OrderActivity.EXTRA_IDS, selected.map { it.book.id }.toTypedArray())
                putExtra(OrderActivity.EXTRA_QUANTITIES, selected.map { it.quantity }.toIntArray())
            })
        }
    }
}
