package com.example.bookmarket

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal data class Memo(val id: Long, val date: String, val title: String, val content: String)

internal class DatabaseHelperMemo(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "Memo.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE MemoTABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                title TEXT,
                content TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("지원하지 않는 메모 DB 버전 변경: " + oldVersion + " → " + newVersion)
    }

    fun read(keyword: String): List<Memo> {
        val search = keyword.trim()
        val selection = if (search.isEmpty()) null else
            "instr(lower(coalesce(title, '')), lower(?)) > 0 OR " +
                "instr(lower(coalesce(content, '')), lower(?)) > 0"
        val args = if (search.isEmpty()) null else arrayOf(search, search)

        return readableDatabase.query(
            "MemoTABLE", arrayOf("id", "date", "title", "content"),
            selection, args, null, null, "id ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Memo(
                            cursor.getLong(0), cursor.getString(1).orEmpty(),
                            cursor.getString(2).orEmpty(), cursor.getString(3).orEmpty()
                        )
                    )
                }
            }
        }
    }

    fun insert(date: String, title: String, content: String): Long =
        writableDatabase.insert("MemoTABLE", null, values(date, title, content))

    fun update(id: Long, date: String, title: String, content: String): Int =
        writableDatabase.update(
            "MemoTABLE", values(date, title, content), "id = ?", arrayOf(id.toString())
        )

    fun delete(id: Long): Int =
        writableDatabase.delete("MemoTABLE", "id = ?", arrayOf(id.toString()))

    private fun values(date: String, title: String, content: String) = ContentValues().apply {
        put("date", date)
        put("title", title)
        put("content", content)
    }
}
