package org.cxct.sportlottery.widget

import android.graphics.Paint
import android.text.TextPaint
import android.text.style.CharacterStyle

/**
 * @author kevin
 * @create 2022/5/4
 * @description
 * strokeWidth = 0f 為初始粗細度 越大越粗
 */
class FakeBoldSpan(val width: Float) : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint) {
        tp.style = Paint.Style.FILL_AND_STROKE;
        tp.strokeWidth = width
    }
}