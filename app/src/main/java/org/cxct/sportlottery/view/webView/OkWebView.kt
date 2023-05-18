package org.cxct.sportlottery.view.webView

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import timber.log.Timber

class OkWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : WebView(context, attrs, defStyle) {

    var okWebViewClient: OkWebViewClient = OkWebViewClient()
        set(value) {
            webViewClient = value
            field = value
        }

    var okWebChromeClient: OkWebChromeClient = OkWebChromeClient()
        set(value) {
            webChromeClient = value
            field = value
        }

    init {
        initWebView()
    }

    private fun initWebView() {
        OkWebViewDefaultSettings().setSettings(this)
        webViewClient = okWebViewClient
        okWebChromeClient = okWebChromeClient
    }

}

