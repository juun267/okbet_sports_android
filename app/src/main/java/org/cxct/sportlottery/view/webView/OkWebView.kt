package org.cxct.sportlottery.view.webView

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
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
//        MultiLanguages.updateAppLanguage(context)
    }

    private fun initWebView() {
        OkWebViewDefaultSettings().setSettings(this)
        webViewClient = okWebViewClient
        okWebChromeClient = okWebChromeClient
    }
    fun cleanAllCache(){
        WebStorage.getInstance().deleteAllData()
        clearCache(true)
        clearFormData()
        clearHistory()
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            cookieManager.flush();
        } else {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
    }
}

