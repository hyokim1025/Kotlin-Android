package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class MenuActivity : AppCompatActivity() {
    private var secondBanner = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        val drawer = findViewById<DrawerLayout>(R.id.menu)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navigation = findViewById<NavigationView>(R.id.navigation)

        val screen = findViewById<View>(R.id.menu_screen)
        ViewCompat.setOnApplyWindowInsetsListener(screen) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        toolbar.title = "BookMarket"
        toolbar.setNavigationIcon(R.drawable.icon_navi)
        toolbar.navigationContentDescription = "탐색 메뉴 열기"
        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        navigation.setNavigationItemSelectedListener { item ->
            drawer.closeDrawer(GravityCompat.START)
            if (item.itemId == R.id.menu03) {
                startActivity(Intent(this, MyPageActivity::class.java))
            } else {
                showMessage(item.title)
            }
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    fun onBookListClick(view: View) {
        startActivity(Intent(this, BooksActivity::class.java))
    }

    fun onEBookClick(view: View) = showMessage("E-Book")
    fun onCustomerInquiryClick(view: View) = showMessage("고객문의")
    fun onMyPageClick(view: View) {
        startActivity(Intent(this, MyPageActivity::class.java))
    }
    fun onMarketInfoClick(view: View) = showMessage("마켓정보")
    fun onSettingClick(view: View) = showMessage("설정")

    fun onBannerClick(view: View) {
        val banner = findViewById<ImageView>(R.id.imageView2)
        secondBanner = !secondBanner
        banner.setImageResource(
            if (secondBanner) R.drawable.menu_banner2 else R.drawable.menu_banner1
        )
    }

    private fun showMessage(title: CharSequence?) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show()
    }
}
