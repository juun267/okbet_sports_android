package org.cxct.sportlottery.view.webView

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebChromeClient
import org.cxct.sportlottery.util.LogUtil

/**
 * webViewChromeClient类
 * 辅助WebView处理javaScript的对话框，网站图标，网站标题等。
 */
open class OkWebChromeClient : WebChromeClient() {

    /**
     * 处理Bitmap.getWidth()为空的崩溃问题
     */
    override fun getDefaultVideoPoster(): Bitmap? {
        return if (super.getDefaultVideoPoster() == null) {
            kotlin.runCatching {
                val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
                val canvas = Canvas(bitmap)
                canvas.drawARGB(255, 255, 255, 255)
                bitmap}.getOrNull()
        } else {
            super.getDefaultVideoPoster()
        }
    }

}