package com.example.bookmarket

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoActivity : CheckoutActivity() {
    private lateinit var db: DatabaseHelperMemo
    private val adapter = MemoAdapter()
    private var keyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCheckout(R.layout.activity_memo, "메모장")
        db = DatabaseHelperMemo(this)
        keyword = savedInstanceState?.getString("memo_keyword").orEmpty()
        adapter.selectedId = savedInstanceState?.getLong("memo_selected", -1) ?: -1

        findViewById<ListView>(R.id.memo_list).apply {
            adapter = this@MemoActivity.adapter
            emptyView = this@MemoActivity.findViewById<TextView>(R.id.memo_empty)
            setOnItemClickListener { _, _, position, _ ->
                this@MemoActivity.adapter.selectedId = this@MemoActivity.adapter.getItemId(position)
                this@MemoActivity.adapter.notifyDataSetChanged()
            }
        }
        findViewById<Button>(R.id.memo_add).setOnClickListener { showEditor(null) }
        findViewById<Button>(R.id.memo_edit).setOnClickListener {
            val selected = selectedMemo()
            if (selected != null) showEditor(selected)
        }
        findViewById<Button>(R.id.memo_delete).setOnClickListener {
            val selected = selectedMemo()
            if (selected != null) confirmDelete(selected)
        }
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_memo_search, menu)
        val searchItem = menu.findItem(R.id.memo_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "검색어를 입력하세요"

        if (keyword.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(keyword, false)
            searchView.clearFocus()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                applySearch(query.trim())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isBlank() && keyword.isNotEmpty()) applySearch("")
                return false
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                applySearch("")
                return true
            }
        })
        return true
    }

    private fun applySearch(query: String) {
        keyword = query
        adapter.selectedId = -1
        refresh()
    }

    private fun refresh() {
        val matches = db.read(keyword)
        if (matches.none { it.id == adapter.selectedId }) adapter.selectedId = -1
        findViewById<TextView>(R.id.memo_empty).text =
            if (keyword.isEmpty()) "아직 메모가 없습니다" else "검색 결과가 없습니다"
        adapter.update(matches, keyword.isNotEmpty())
    }

    private fun selectedMemo(): Memo? {
        val selected = adapter.items.firstOrNull { it.id == adapter.selectedId }
        if (selected == null) message("목록에서 메모를 선택하세요")
        return selected
    }

    private fun showEditor(memo: Memo?) {
        val contentView = layoutInflater.inflate(R.layout.dialog_memo_edit, null)
        val title = contentView.findViewById<EditText>(R.id.memo_title_input)
        val body = contentView.findViewById<EditText>(R.id.memo_body_input)
        title.setText(memo?.title.orEmpty())
        body.setText(memo?.content.orEmpty())
        val action = if (memo == null) "등록" else "수정"

        val dialog = AlertDialog.Builder(this)
            .setTitle("나의 메모장")
            .setIcon(R.drawable.icon_memo)
            .setView(contentView)
            .setNegativeButton("취소", null)
            .setPositiveButton(action, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val enteredTitle = title.text.toString().trim()
            val enteredBody = body.text.toString().trim()
            if (enteredTitle.isEmpty()) {
                title.error = "제목을 입력하세요"
                title.requestFocus()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
            val saved = if (memo == null) {
                db.insert(date, enteredTitle, enteredBody) != -1L
            } else {
                db.update(memo.id, date, enteredTitle, enteredBody) == 1
            }
            if (!saved) {
                message("메모를 저장하지 못했습니다")
                return@setOnClickListener
            }
            dialog.dismiss()
            refresh()
        }
    }

    private fun confirmDelete(memo: Memo) {
        AlertDialog.Builder(this)
            .setTitle("메모 삭제")
            .setMessage("선택한 메모를 삭제할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                if (db.delete(memo.id) == 0) message("메모를 찾을 수 없습니다")
                adapter.selectedId = -1
                refresh()
            }
            .show()
    }

    private fun message(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("memo_keyword", keyword)
        outState.putLong("memo_selected", adapter.selectedId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}

private class MemoAdapter : BaseAdapter() {
    var selectedId = -1L
    var items: List<Memo> = emptyList()
        private set
    private var showingResults = false

    fun update(memos: List<Memo>, isSearch: Boolean) {
        items = memos
        showingResults = isSearch
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Memo = items[position]
    override fun getItemId(position: Int): Long = getItem(position).id
    override fun hasStableIds(): Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo, parent, false)
        val memo = getItem(position)
        row.findViewById<TextView>(R.id.memo_row_title).text = memo.title
        row.findViewById<TextView>(R.id.memo_row_date).text = memo.date
        row.findViewById<TextView>(R.id.memo_row_content).text = memo.content
        val color = when {
            memo.id == selectedId -> 0xFFF5BC75.toInt()
            showingResults -> 0xFFF9DDAB.toInt()
            else -> Color.TRANSPARENT
        }
        row.setBackgroundColor(color)
        return row
    }
}
