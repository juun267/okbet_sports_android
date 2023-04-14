package org.cxct.sportlottery.view.webView

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class OkWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : WebView(context, attrs, defStyle) {

    init {
        initWebView()
    }

    var okWebViewClient: OkWebViewClient? = null
    var okWebChromeClient:  OkWebChromeClient? = null


    private fun initWebView() {
        OkWebViewDefaultSettings().setSettings(this)
        okWebChromeClient?.let {
            webChromeClient = it
        }
        okWebViewClient?.let {
            webViewClient = it
        }
    }

}

