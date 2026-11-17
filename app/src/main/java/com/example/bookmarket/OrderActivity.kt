package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrderActivity : CheckoutActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shipping = intent.getStringArrayExtra(EXTRA_SHIPPING)
        val ids = intent.getStringArrayExtra(EXTRA_IDS).orEmpty()
        val quantities = intent.getIntArrayExtra(EXTRA_QUANTITIES) ?: intArrayOf()
        val ordered = ids.zip(quantities.toTypedArray()).mapNotNull { (id, quantity) ->
            books.find { it.id == id }?.takeIf { quantity > 0 }?.let { CartLine(it, quantity, true) }
        }
        if (shipping?.size != 5 || ordered.isEmpty()) {
            finish()
            return
        }

        showCheckout(R.layout.activity_order, "주문서")
        val (name, date, phone, zip, address) = shipping
        findViewById<TextView>(R.id.order_date).text = "배송일 : " + date
        findViewById<TextView>(R.id.order_address).text =
            "성명 : " + name + " (" + phone + ")\n우편번호 : " + zip + "\n주소 : " + address
        findViewById<TextView>(R.id.order_total).text = "총액 : " + won(ordered.sumOf { it.subtotal })

        findViewById<RecyclerView>(R.id.order_items).apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = OrderAdapter(ordered)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        findViewById<Button>(R.id.order_cancel).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("도서 주문 취소")
                .setMessage("배송 정보 화면으로 돌아갈까요?")
                .setNegativeButton("아니요", null)
                .setPositiveButton("예") { _, _ -> finish() }
                .show()
        }
        findViewById<Button>(R.id.order_submit).setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(R.drawable.icon_order)
                .setTitle("도서 주문 완료")
                .setMessage("도서를 주문해 주셔서 감사합니다.")
                .setNegativeButton("아니요", null)
                .setPositiveButton("예") { _, _ ->
                    CartStore(this).removeOrdered(ordered)
                    val home = Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(home)
                    finish()
                }
                .show()
        }
    }

    companion object {
        const val EXTRA_SHIPPING = "shipping"
        const val EXTRA_IDS = "ordered_book_ids"
        const val EXTRA_QUANTITIES = "ordered_quantities"
    }
}

private class OrderAdapter(private val items: List<CartLine>) :
    RecyclerView.Adapter<OrderAdapter.Row>() {

    class Row(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.order_row_title)
        val id: TextView = view.findViewById(R.id.order_row_id)
        val price: TextView = view.findViewById(R.id.order_row_price)
        val quantity: TextView = view.findViewById(R.id.order_row_quantity)
        val subtotal: TextView = view.findViewById(R.id.order_row_subtotal)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Row {
        return Row(LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false))
    }

    override fun onBindViewHolder(holder: Row, position: Int) {
        val line = items[position]
        holder.title.text = line.book.title
        holder.id.text = line.book.id
        holder.price.text = won(line.book.price.toLong())
        holder.quantity.text = line.quantity.toString()
        holder.subtotal.text = won(line.subtotal)
    }
}
