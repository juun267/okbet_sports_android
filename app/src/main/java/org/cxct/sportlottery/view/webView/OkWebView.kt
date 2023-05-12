package org.cxct.sportlottery.view.webView

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import timber.log.Timber

class OkWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : WebView(context, attrs, defStyle) {

    init {
        initWebView()
    }

    var okWebViewClient: OkWebViewClient? = null
        set(value) {
            value?.apply {
                webViewClient = this
            }
            field = value
        }

    var okWebChromeClient: OkWebChromeClient? = null
        set(value) {
            value?.apply {
                webChromeClient = this
            }
            field = value
        }


    private fun initWebView() {
        OkWebViewDefaultSettings().setSettings(this)
    }

}

