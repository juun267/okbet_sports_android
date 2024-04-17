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
        return super.getDefaultVideoPoster() ?: kotlin.runCatching{Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)}.getOrNull()
    }

}