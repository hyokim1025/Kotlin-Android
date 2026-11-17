package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BooksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_books)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.books)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "도서목록"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val coverIds = listOf(R.id.book1, R.id.book2, R.id.book3, R.id.book4)
        coverIds.forEachIndexed { index, viewId ->
            val cover = findViewById<ImageView>(viewId)
            cover.setOnClickListener {
                Toast.makeText(this, cover.contentDescription, Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, BookActivity::class.java).putExtra(BookActivity.EXTRA_BOOK_INDEX, index)
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_books, menu)
        val search = menu.findItem(R.id.menu_search).actionView as? SearchView
        search?.queryHint = "도서명을 입력해주세요."
        search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Toast.makeText(this@BooksActivity, query, Toast.LENGTH_SHORT).show()
                search?.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })
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
            R.id.main_video, R.id.menu_customer, R.id.menu_mypage -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
