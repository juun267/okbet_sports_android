package org.cxct.sportlottery.view.webView

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor

class OkWebViewDefaultSettings {

    private lateinit var mWebSettings: WebSettings

    @SuppressLint("SetJavaScriptEnabled")
    fun setSettings(webView: WebView) {
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        webView.setInitialScale(1)
        webView.setWebViewCommonBackgroundColor()
        WebView.enableSlowWholeDocumentDraw()
        mWebSettings = webView.settings
        mWebSettings.javaScriptEnabled = true
        mWebSettings.setSupportZoom(true)
        mWebSettings.builtInZoomControls = false
        mWebSettings.textZoom = 100
        mWebSettings.loadsImagesAutomatically = true
        mWebSettings.blockNetworkImage = false //是否阻塞加载网络图片  协议http or https
        mWebSettings.allowFileAccess = true //允许加载本地文件html  file协议
        mWebSettings.javaScriptCanOpenWindowsAutomatically = true
        mWebSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        mWebSettings.domStorageEnabled = true //对H5支持
        mWebSettings.useWideViewPort = true //将图片调整到适合webview的大小
        mWebSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
        mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mWebSettings.javaScriptCanOpenWindowsAutomatically = true
        mWebSettings.cacheMode = WebSettings.LOAD_DEFAULT
        mWebSettings.databaseEnabled = false
        mWebSettings.blockNetworkImage = false
//        mWebSettings.setAppCacheEnabled(false)

//        mWebSettings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理
        mWebSettings.allowContentAccess = true
        mWebSettings.allowFileAccessFromFileURLs = true
        mWebSettings.allowUniversalAccessFromFileURLs = true
        mWebSettings.setNeedInitialFocus(true)
        mWebSettings.defaultTextEncodingName = "utf-8" //设置编码格式
        mWebSettings.setGeolocationEnabled(true)
    }


}