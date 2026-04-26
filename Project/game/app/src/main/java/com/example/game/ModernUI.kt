package com.example.game

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.graphics.ColorUtils

object ModernUI {

    object ColorPalette {
        // คืนค่าสีโทนเทาดำ/เงิน สำหรับธีมดันเจี้ยนมืด
        val neonGold = Color.parseColor("#CCCCCC") // Silver
        val neonGreen = Color.parseColor("#BBBBBB") // Light Gray
        val neonPurple = Color.parseColor("#999999") // Gray
        val neonBlue = Color.parseColor("#AAAAAA") // Gray
        val neonPink = Color.parseColor("#888888") // Dark Gray

        val gradientOrangePink = intArrayOf(Color.parseColor("#444444"), Color.parseColor("#222222"))
        val gradientCyan = intArrayOf(Color.parseColor("#333333"), Color.parseColor("#111111"))
        val gradientDark = intArrayOf(Color.parseColor("#000000"), Color.parseColor("#1A1A1A"))
    }

    fun createGameBackground(context: Context, resId: Int): ImageView {
        return ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            try {
                setImageResource(resId)
            } catch (e: Exception) {
                setBackgroundColor(Color.BLACK)
            }
        }
    }

    fun createGradientProgressBar(context: Context, colors: IntArray, heightDp: Int): ProgressBar {
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        
        val heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp.toFloat(), context.resources.displayMetrics).toInt()
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        lp.setMargins(0, (4 * context.resources.displayMetrics.density).toInt(), 0, (4 * context.resources.displayMetrics.density).toInt())
        progressBar.layoutParams = lp
        
        progressBar.max = 100
        progressBar.progress = 100

        try {
            val shape = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
            shape.cornerRadius = 100f

            val clip = ClipDrawable(shape, Gravity.START, ClipDrawable.HORIZONTAL)
            
            val background = GradientDrawable()
            background.setColor(Color.parseColor("#33FFFFFF"))
            background.cornerRadius = 100f

            val layers = LayerDrawable(arrayOf(background, clip))
            layers.setId(0, android.R.id.background)
            layers.setId(1, android.R.id.progress)
            
            progressBar.progressDrawable = layers
        } catch (e: Exception) {}
        
        return progressBar
    }

    fun createModernCard(context: Context, gradient: IntArray, radius: Float, alpha: Float): FrameLayout {
        val card = FrameLayout(context)
        
        val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR, gradient)
        gd.cornerRadius = radius * context.resources.displayMetrics.density
        gd.alpha = (alpha * 255).toInt()
        
        card.background = gd
        card.elevation = 8f * context.resources.displayMetrics.density
        
        return card
    }

    fun createNeonButton(context: Context, text: String, color: Int, onClick: () -> Unit): FrameLayout {
        val container = FrameLayout(context)
        
        val button = Button(context).apply {
            this.text = text
            this.setTextColor(Color.WHITE)
            this.typeface = Typeface.DEFAULT_BOLD
            this.isAllCaps = false
            this.background = createNeonDrawable(color)
            
            setOnClickListener {
                this.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        this.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                            onClick()
                        }.start()
                    }
                    .start()
            }
        }

        container.setPadding(4, 4, 4, 4)
        container.addView(button, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        return container
    }

    private fun createNeonDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            // ปรับปุ่มให้โปร่งใสเล็กน้อยเพื่อให้เห็นพื้นหลังมืดๆ แต่มีขอบชัดเจน
            setColor(ColorUtils.setAlphaComponent(color, 60))
            setStroke(4, color)
        }
    }
}
