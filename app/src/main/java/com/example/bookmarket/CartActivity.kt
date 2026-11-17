package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : CheckoutActivity() {
    private lateinit var store: CartStore
    private lateinit var adapter: CartAdapter
    private lateinit var selectAll: CheckBox
    private lateinit var orderButton: Button
    private lateinit var totalView: TextView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCheckout(R.layout.activity_cart, "장바구니")
        store = CartStore(this)
        selectAll = findViewById(R.id.cart_select_all)
        orderButton = findViewById(R.id.cart_order)
        totalView = findViewById(R.id.cart_total)
        emptyView = findViewById(R.id.cart_empty)

        adapter = CartAdapter(
            onSelected = { id, selected ->
                store.select(id, selected)
                refresh()
            },
            onDelete = { line ->
                AlertDialog.Builder(this)
                    .setTitle("도서 삭제")
                    .setMessage(line.book.title + "을(를) 장바구니에서 삭제할까요?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("삭제") { _, _ ->
                        store.remove(line.book.id)
                        refresh()
                    }.show()
            }
        )
        findViewById<RecyclerView>(R.id.cart_items).apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = this@CartActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        selectAll.setOnClickListener {
            store.selectAll(selectAll.isChecked)
            refresh()
        }
        orderButton.setOnClickListener {
            startActivity(Intent(this, ShippingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val items = store.lines()
        val selected = items.filter { it.selected }
        selectAll.isChecked = items.isNotEmpty() && items.all { it.selected }
        selectAll.isEnabled = items.isNotEmpty()
        emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        totalView.text = "합계 " + won(selected.sumOf { it.subtotal })
        orderButton.isEnabled = selected.isNotEmpty()
        adapter.update(items)
    }
}

private class CartAdapter(
    private val onSelected: (String, Boolean) -> Unit,
    private val onDelete: (CartLine) -> Unit
) : RecyclerView.Adapter<CartAdapter.Row>() {
    private var lines: List<CartLine> = emptyList()

    class Row(view: View) : RecyclerView.ViewHolder(view) {
        val selected: CheckBox = view.findViewById(R.id.cart_row_selected)
        val cover: ImageView = view.findViewById(R.id.cart_row_cover)
        val title: TextView = view.findViewById(R.id.cart_row_title)
        val details: TextView = view.findViewById(R.id.cart_row_details)
        val delete: Button = view.findViewById(R.id.cart_row_delete)
    }

    fun update(items: List<CartLine>) {
        lines = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = lines.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Row {
        return Row(LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false))
    }

    override fun onBindViewHolder(holder: Row, position: Int) {
        val line = lines[position]
        holder.cover.setImageResource(line.book.coverRes)
        holder.title.text = line.book.title
        holder.details.text = "%s × %d권 · %s".format(
            won(line.book.price.toLong()), line.quantity, won(line.subtotal)
        )
        holder.selected.contentDescription = line.book.title + " 선택"
        holder.selected.isChecked = line.selected
        holder.selected.setOnClickListener { onSelected(line.book.id, holder.selected.isChecked) }
        holder.delete.setOnClickListener { onDelete(line) }
    }
}
