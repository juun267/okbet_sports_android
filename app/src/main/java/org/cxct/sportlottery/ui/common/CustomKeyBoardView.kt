package org.cxct.sportlottery.ui.common


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.inputmethodservice.Keyboard
import android.inputmethodservice.Keyboard.KEYCODE_DELETE
import android.inputmethodservice.Keyboard.KEYCODE_DONE
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R


@Suppress("DEPRECATION")
class CustomKeyBoardView(context: Context?, attrs: AttributeSet?) : KeyboardView(context, attrs) {

    private var paint: Paint = Paint()

    @SuppressLint("NewApi")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            val keys = keyboard.keys
            for (key in keys) {
                if (
                    key.codes[0] == KeyBoardCode.PLUS_10.code ||
                    key.codes[0] == KeyBoardCode.PLUS_50.code ||
                    key.codes[0] == KeyBoardCode.PLUS_100.code ||
                    key.codes[0] == KeyBoardCode.MAX.code ||
                    key.codes[0] == KEYCODE_DONE
                ) {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_count)

                    if(key.codes[0] == KeyBoardCode.PLUS_10.code ||
                        key.codes[0] == KeyBoardCode.PLUS_50.code ||
                        key.codes[0] == KeyBoardCode.PLUS_100.code ||
                        key.codes[0] == KeyBoardCode.MAX.code){
                        paint.typeface = Typeface.DEFAULT_BOLD
                    }else{
                        paint.typeface = Typeface.DEFAULT
                    }

                    if( key.codes[0] == KeyBoardCode.PLUS_10.code ||
                        key.codes[0] == KeyBoardCode.PLUS_50.code ||
                        key.codes[0] == KeyBoardCode.PLUS_100.code){
                        paint.color = context.getColor(R.color.colorGray)
                    }else{
                        paint.color = context.getColor(R.color.colorBlackLight)
                    }
                    
                } else if (key.codes[0] == KEYCODE_DELETE) {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_delete)
                    paint.typeface = Typeface.DEFAULT
                    paint.color = context.getColor(R.color.colorBlackLight)
                } else {
                    setDrawable(canvas, key, R.drawable.bg_keyboard_number)
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.color = context.getColor(R.color.colorBlackLight)
                }

                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 36f
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
        val drawable = ContextCompat.getDrawable(context, res)
        drawable?.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
        drawable?.draw(canvas)
    }


}