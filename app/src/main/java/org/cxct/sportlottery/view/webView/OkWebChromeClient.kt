package org.cxct.sportlottery.view.webView

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.WebChromeClient
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication

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
            BitmapFactory.decodeResource(
                MultiLanguagesApplication.appContext.resources, R.drawable.ic_video
            )
        } else {
            super.getDefaultVideoPoster()
        }
    }

}