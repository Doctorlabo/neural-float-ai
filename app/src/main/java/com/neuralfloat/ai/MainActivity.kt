package com.neuralfloat.ai

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(16, 18, 29)
        window.navigationBarColor = Color.rgb(16, 18, 29)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(16, 18, 29))
        }

        val title = TextView(this).apply {
            text = "NEURAL FLOAT AI"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(232, 195, 105))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "مساعدك الذكي العائم"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(30))
        }

        val startButton = makeButton("تشغيل الزر العائم")
        val stopButton = makeButton("إيقاف الزر العائم")
        val info = TextView(this).apply {
            text = "الإصدار 0.1.0 Alpha"
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, 0)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(startButton)
        root.addView(stopButton)
        root.addView(info)

        setContentView(root)

        startButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)
            ) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(
                    this,
                    "فعّل السماح بالظهور فوق التطبيقات",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startService(Intent(this, FloatingService::class.java))
                Toast.makeText(
                    this,
                    "تم طلب تشغيل الزر العائم",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        stopButton.setOnClickListener {
            stopService(Intent(this, FloatingService::class.java))
            Toast.makeText(
                this,
                "تم إيقاف الزر العائم",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun makeButton(label: String): Button =
        Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.rgb(25, 25, 30))
            setBackgroundColor(Color.rgb(232, 195, 105))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
            params.setMargins(0, dp(8), 0, dp(8))
            layoutParams = params
        }
}
