package com.neuralfloat.ai

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingButton: TextView? = null
    private var panel: LinearLayout? = null
    private var panelVisible = false

    private lateinit var buttonParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun createFloatingButton() {
        val button = TextView(this).apply {
            text = "N"
            textSize = 23f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 25, 30))

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(232, 195, 105))
                setStroke(dp(2), Color.WHITE)
            }

            elevation = dp(8).toFloat()
        }

        buttonParams = WindowManager.LayoutParams(
            dp(58),
            dp(58),
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(220)
        }

        floatingButton = button

        button.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f
            private var moved = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = buttonParams.x
                        startY = buttonParams.y
                        touchX = event.rawX
                        touchY = event.rawY
                        moved = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()

                        if (kotlin.math.abs(dx) > dp(5) || kotlin.math.abs(dy) > dp(5)) {
                            moved = true
                        }

                        buttonParams.x = startX + dx
                        buttonParams.y = startY + dy

                        try {
                            windowManager.updateViewLayout(button, buttonParams)
                        } catch (_: Exception) {
                        }

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!moved) {
                            togglePanel()
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager.addView(button, buttonParams)
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر إنشاء الزر العائم", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun createPanel() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))

            background = GradientDrawable().apply {
                setColor(Color.argb(245, 20, 23, 36))
                cornerRadius = dp(22).toFloat()
                setStroke(dp(1), Color.rgb(232, 195, 105))
            }
        }

        val heading = TextView(this).apply {
            text = "NEURAL FLOAT AI"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(232, 195, 105))
            setPadding(0, 0, 0, dp(12))
        }

        container.addView(heading)

        addPanelButton(container, "🤖 مساعد الذكاء الاصطناعي") {
            showMessage("واجهة المساعد ستضاف في المرحلة التالية")
        }

        addPanelButton(container, "📋 استخدام النص المنسوخ") {
            showMessage("انسخ النص أولا، ثم سنضيف ميزة قراءته")
        }

        addPanelButton(container, "💡 توصية ذكية") {
            showMessage("ميزة التوصيات قيد التطوير")
        }

        addPanelButton(container, "⚙️ الإعدادات") {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        addPanelButton(container, "✕ إغلاق اللوحة") {
            hidePanel()
        }

        panelParams = WindowManager.LayoutParams(
            dp(290),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        panel = container
    }

    private fun addPanelButton(
        container: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val item = TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(13), dp(12), dp(13))

            background = GradientDrawable().apply {
                setColor(Color.argb(35, 232, 195, 105))
                cornerRadius = dp(12).toFloat()
            }

            isClickable = true
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(4), 0, dp(4))
        container.addView(item, params)
    }

    private fun togglePanel() {
        if (panelVisible) hidePanel() else showPanel()
    }

    private fun showPanel() {
        try {
            if (panel == null) createPanel()

            panel?.let {
                if (it.parent == null) {
                    windowManager.addView(it, panelParams)
                }
            }

            panelVisible = true
        } catch (_: Exception) {
            showMessage("تعذر فتح اللوحة")
        }
    }

    private fun hidePanel() {
        try {
            panel?.let {
                if (it.parent != null) {
                    windowManager.removeView(it)
                }
            }
        } catch (_: Exception) {
        }

        panelVisible = false
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        hidePanel()

        try {
            floatingButton?.let {
                if (it.parent != null) {
                    windowManager.removeView(it)
                }
            }
        } catch (_: Exception) {
        }

        floatingButton = null
        panel = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
