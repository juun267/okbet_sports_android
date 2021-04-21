package org.cxct.sportlottery.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import org.cxct.sportlottery.R

@SuppressLint("UseCompatLoadingForDrawables", "DrawAllocation")
class CustomKeyBoardView(context: Context?, attrs: AttributeSet?) : KeyboardView(context, attrs) {


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            val keys = keyboard.keys
            for (key in keys) {
                if (key.codes[0] == KeyBoardCode.PLUS_100.code || key.codes[0] == KeyBoardCode.PLUS_1000.code || key.codes[0] == KeyBoardCode.PLUS_10000.code) {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_count)
                } else if (key.codes[0] == KeyBoardCode.DELETE.code) {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_delete)
                } else {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_number)
                }
                val paint = Paint()
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 36f
                paint.color = Color.BLACK
                if (key.label != null) {
                    canvas.drawText(
                        key.label.toString(), (key.x + key.width / 2).toFloat(), (
                                key.y + key.height / 2 + 14).toFloat(), paint
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setDrawable(canvas: Canvas, key: Keyboard.Key, res: Int) {
        val drawable = resources.getDrawable(res)
        drawable.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
        drawable.draw(canvas)
    }


}