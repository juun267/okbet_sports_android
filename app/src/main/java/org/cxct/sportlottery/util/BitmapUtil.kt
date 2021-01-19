package org.cxct.sportlottery.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object BitmapUtil {

    //base64 轉化二進制字符成 Bitmap
    fun stringToBitmap(data: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bitmapArray: ByteArray = Base64.decode(data, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

}
