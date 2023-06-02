package org.cxct.sportlottery.util

import android.text.InputFilter
import android.text.Spanned

class MoneyInputFilter : InputFilter {
    companion object {
        val POINT_LENGTH = 3//保留小數點位數
    }

    override fun filter(
        source: CharSequence,//將要輸入的字符串,如果是刪除操作則為空
        start: Int,//將要輸入的字符串起始下標，一般為0
        end: Int,//start + source字符的長度
        dest: Spanned,//輸入之前文本框中的內容
        dstart: Int,//將會被替換的起始位置
        dend: Int//dstart+將會被替換的字符串長度
    ): CharSequence {

        val start = dest.subSequence(0, dstart)
        val end = dest.subSequence(dend, dest.length)
        val target = start.toString() + source + end//字符串變化後的結果
        val backup = dest.subSequence(dstart, dend)//將要被替換的字符串

        //限制小數點後面只能有兩位小數
        val index = target.indexOf(".")
        if (index >= 0 && index + POINT_LENGTH + 1 <= target.length) {
            return backup
        }

        return source
    }
}