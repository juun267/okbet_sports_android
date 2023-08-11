package org.cxct.sportlottery.view.webView

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

open class OkWebView  : WebView {

    constructor(context: Context):super(context,null)

    constructor(context: Context,attributeSet: AttributeSet): super(context,attributeSet)

    constructor(context: Context,attributeSet: AttributeSet, defStyleArr:Int): super(context,attributeSet,defStyleArr)

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

