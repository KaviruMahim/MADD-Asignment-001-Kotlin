package com.example.mysleepzyapp

import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainHostActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SELECTED_TAB = "extra_selected_tab"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host)

        // Draw behind system bars with fully transparent bars so gradient shows edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            val requested = intent.getIntExtra(EXTRA_SELECTED_TAB, R.id.nav_analytics)
            bottom.selectedItemId = requested
            selectTab(requested)
        }

        bottom.setOnItemSelectedListener { item ->
            selectTab(item.itemId)
            true
        }
    }

    private fun selectTab(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_profile -> ProfileFragment()
            R.id.nav_analytics -> AnalyticsFragment()
            R.id.nav_sleep -> SleepFragment()
            R.id.nav_music -> CalmMusicFragment()
            R.id.nav_tips -> TipsFragment()
            else -> AnalyticsFragment()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navHostContainer, fragment)
            .commit()
    }
}
