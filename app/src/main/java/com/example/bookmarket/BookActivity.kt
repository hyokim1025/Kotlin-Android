package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BOOK_INDEX = "book_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val book = books.getOrNull(intent.getIntExtra(EXTRA_BOOK_INDEX, -1))
        if (book == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_book)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.book)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "도서상세정보"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<ImageView>(R.id.book_cover).apply {
            setImageResource(book.coverRes)
            contentDescription = book.title
        }
        findViewById<TextView>(R.id.book_tv01).text = book.id
        findViewById<TextView>(R.id.book_tv02).text = book.title
        findViewById<TextView>(R.id.book_tv03).text = won(book.price.toLong())
        findViewById<TextView>(R.id.book_tv04).text = book.date
        findViewById<TextView>(R.id.book_tv05).text = book.author
        findViewById<TextView>(R.id.book_tv06).text = book.description
        findViewById<TextView>(R.id.book_tv07).text = "IT/모바일"

        findViewById<Button>(R.id.book_add_to_cart).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("장바구니 담기")
                .setMessage("이 도서를 장바구니에 담을까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("확인") { _, _ ->
                    CartStore(this).add(book.id)
                    Toast.makeText(this, "장바구니에 담았습니다", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_book, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_home -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                true
            }
            R.id.menu_list -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                finish()
                true
            }
            R.id.menu_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
