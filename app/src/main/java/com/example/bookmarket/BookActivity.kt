package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BOOK_INDEX = "book_index"
    }

    private data class Book(
        val id: String,
        val title: String,
        val price: String,
        val date: String,
        val author: String,
        val description: String,
        val coverRes: Int
    )

    private val books = listOf(
        Book("BOOK1234", "쉽게 배우는 알고리즘", "22,000원", "2024-01-03", "문병로",
            "자료구조와 알고리즘을 바탕으로 문제를 체계적으로 해결하는 방법을 배웁니다.",
            R.drawable.books_book1),
        Book("BOOK1235", "난생 처음 MySQL", "34,000원", "2024-01-17", "배은영",
            "무역회사 데이터베이스를 활용한 예제로 SQL과 쿼리 작성을 실습합니다.",
            R.drawable.books_book2),
        Book("BOOK1236", "쉽게 배우는 PHP 웹 프로그래밍", "30,000원", "2024-06-30", "송미영",
            "PHP 기초를 익히고 북마켓 쇼핑몰을 단계별로 만들어 봅니다.",
            R.drawable.books_book3),
        Book("BOOK1237", "미라클 HTML5 + CSS3 + 자바스크립트", "28,000원", "2024-07-11", "조성호",
            "다양한 예제를 따라 하며 웹 페이지의 구조와 스타일, 동작을 익힙니다.",
            R.drawable.books_book4)
    )

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
        findViewById<TextView>(R.id.book_tv03).text = book.price
        findViewById<TextView>(R.id.book_tv04).text = book.date
        findViewById<TextView>(R.id.book_tv05).text = book.author
        findViewById<TextView>(R.id.book_tv06).text = book.description
        findViewById<TextView>(R.id.book_tv07).text = "IT/모바일"
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
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
