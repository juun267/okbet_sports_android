package org.cxct.sportlottery.ui.maintab.lottery

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityWebBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebView
import org.cxct.sportlottery.view.webView.OkWebViewClient

/**
 * Create by Simon Chang
 */
class LotteryActivity : BaseActivity<MainViewModel,ActivityWebBinding>(MainViewModel::class) {
    override fun pageName() = "彩票页面"
    companion object {
        const val KEY_URL = "key-url"
    }

    private val mUrl: String by lazy { intent?.getStringExtra(KEY_URL) ?: "about:blank" }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding.customToolBar.visibility = View.GONE
        binding.okWebView.addJavascriptInterface(LotteryJsInterface(this), LotteryJsInterface.name)
        setupWebView(binding.okWebView)
        loadUrl(binding.okWebView)
    }

    @SuppressLint("WebViewApiAvailability")
    fun setupWebView(webView: OkWebView) {
//        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
//
//        webView.setWebViewCommonBackgroundColor()
//
//        val settings: WebSettings = webView.settings
//        settings.javaScriptEnabled = true
//        settings.blockNetworkImage = false
//        settings.domStorageEnabled = true //对H5支持
//        settings.useWideViewPort = true //将图片调整到适合webview的大小
//        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
//        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        settings.javaScriptCanOpenWindowsAutomatically = true
//        settings.defaultTextEncodingName = "utf-8"
//        settings.cacheMode = WebSettings.LOAD_NO_CACHE
//        settings.databaseEnabled = true
////        settings.setAppCacheEnabled(false)
//
//        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理
//        settings.allowFileAccess = true
//        settings.allowContentAccess = true
//        settings.allowFileAccessFromFileURLs = true
//        settings.allowUniversalAccessFromFileURLs = true
        webView.webChromeClient = object : OkWebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message,
            ): Boolean {
                val newWebView = WebView(view.context)
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        //20191120 記錄問題： target=_black 允許跳轉新窗口處理
                        //在此处进行跳转URL的处理, 一般情况下_black需要重新打开一个页面
                        try {
                            //使用系統默認外部瀏覽器跳轉
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(i)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val PERMISSIONS_AT_WEBVIEW = 0
                val permissionCheck = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_AT_WEBVIEW
                    )
                } else {
                    request?.grant(request.resources)
                }
            }
        }

        webView.webViewClient = object : OkWebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
            }

            override fun shouldInterceptRequest(
                view: WebView?, url: String?,
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!url.startsWith("http")) {
                    try {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        i.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(i)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                JumpUtil.toExternalWeb(this@LotteryActivity, url)
                return true
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError,
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                //handler.proceed()
                if (isFinishing)
                    return
                AlertDialog.Builder(this@LotteryActivity)
                    .setMessage(android.R.string.httpErrorUnsupportedScheme)
                    .setPositiveButton(
                        "continue"
                    ) { dialog, which -> handler.proceed() }
                    .setNegativeButton(
                        "cancel"
                    ) { dialog, which -> handler.cancel() }
                    .create()
                    .show()

            }
        }

        //H5调用系统下载
        webView.setDownloadListener { url, _, _, _, _ ->
            kotlin.runCatching { Uri.parse(url) }.getOrNull()?.let {
                val intent = Intent(Intent.ACTION_VIEW, it)
                startActivity(intent)
            }
        }
    }

    fun loadUrl(webView: WebView) {
        webView.loadUrl(mUrl)
    }

    override fun onBackPressed() {
        if (binding.okWebView.canGoBack()) {
            binding.okWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    class LotteryJsInterface(val activity: LotteryActivity) {
        companion object {
            const val name = "LotteryJsInterface"
        }

        @JavascriptInterface
        fun backClick() {
            activity.runOnUiThread {
                activity.onBackPressed()
            }
        }

        @JavascriptInterface
        fun login() {
            activity.runOnUiThread {
                activity.startLogin()
            }
        }
    }
}