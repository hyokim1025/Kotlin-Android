package com.example.bookmarket

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

internal data class Book(
    val id: String,
    val title: String,
    val price: Int,
    val date: String,
    val author: String,
    val description: String,
    val coverRes: Int
)

internal val books = listOf(
    Book("BOOK1234", "쉽게 배우는 알고리즘", 22000, "2024-01-03", "문병로",
        "자료구조와 알고리즘을 바탕으로 문제를 체계적으로 해결하는 방법을 배웁니다.",
        R.drawable.books_book1),
    Book("BOOK1235", "난생 처음 MySQL", 34000, "2024-01-17", "배은영",
        "무역회사 데이터베이스를 활용한 예제로 SQL과 쿼리 작성을 실습합니다.",
        R.drawable.books_book2),
    Book("BOOK1236", "쉽게 배우는 PHP 웹 프로그래밍", 30000, "2024-06-30", "송미영",
        "PHP 기초를 익히고 북마켓 쇼핑몰을 단계별로 만들어 봅니다.",
        R.drawable.books_book3),
    Book("BOOK1237", "미라클 HTML5 + CSS3 + 자바스크립트", 28000, "2024-07-11", "조성호",
        "다양한 예제를 따라 하며 웹 페이지의 구조와 스타일, 동작을 익힙니다.",
        R.drawable.books_book4)
)

internal data class CartLine(val book: Book, val quantity: Int, val selected: Boolean) {
    val subtotal: Long get() = book.price.toLong() * quantity
}

internal fun won(amount: Long): String = NumberFormat
    .getIntegerInstance(Locale.KOREA)
    .format(amount) + "원"

internal class CartStore(context: Context) {
    private val storage = context.getSharedPreferences("bookmarket_cart", Context.MODE_PRIVATE)

    fun lines(): List<CartLine> = books.mapNotNull { book ->
        val quantity = storage.getInt(quantityKey(book.id), 0)
        if (quantity <= 0) null
        else CartLine(book, quantity, storage.getBoolean(selectionKey(book.id), true))
    }

    fun add(id: String) {
        val quantity = storage.getInt(quantityKey(id), 0)
        storage.edit().putInt(quantityKey(id), quantity + 1)
            .putBoolean(selectionKey(id), true).apply()
    }

    fun select(id: String, selected: Boolean) {
        storage.edit().putBoolean(selectionKey(id), selected).apply()
    }

    fun selectAll(selected: Boolean) {
        val edit = storage.edit()
        for (line in lines()) edit.putBoolean(selectionKey(line.book.id), selected)
        edit.apply()
    }

    fun remove(id: String) {
        storage.edit().remove(quantityKey(id)).remove(selectionKey(id)).apply()
    }

    fun removeOrdered(ordered: List<CartLine>) {
        val edit = storage.edit()
        for (line in ordered) {
            val id = line.book.id
            val remaining = storage.getInt(quantityKey(id), 0) - line.quantity
            if (remaining > 0) {
                edit.putInt(quantityKey(id), remaining)
            } else {
                edit.remove(quantityKey(id)).remove(selectionKey(id))
            }
        }
        edit.apply()
    }

    private fun quantityKey(id: String) = "quantity_" + id
    private fun selectionKey(id: String) = "selected_" + id
}
