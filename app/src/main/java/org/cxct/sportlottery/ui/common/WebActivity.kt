package org.cxct.sportlottery.ui.common

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.android.synthetic.main.activity_web.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Create by Simon Chang
 */
open class WebActivity : BaseActivity<MainViewModel>(MainViewModel::class) {
    companion object {
        const val KEY_URL = "key-url"
        const val KEY_TITLE = "key-title"
        const val KEY_TOOLBAR_VISIBILITY = "key-toolbar-visibility"
    }

    private val mTitle: String by lazy { intent?.getStringExtra(KEY_TITLE) ?: "" }
    private val mUrl: String by lazy { intent?.getStringExtra(KEY_URL) ?: "about:blank" }
    private val mToolbarVisibility: Boolean by lazy { intent?.getBooleanExtra(KEY_TOOLBAR_VISIBILITY, true) ?: true }
    private var mUploadCallbackAboveL: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    open fun init() {
        setContentView(R.layout.activity_web)
        if (!mToolbarVisibility) custom_tool_bar.visibility = View.GONE else initToolBar()
        setCookie()
        setupWebView(web_view)
        loadUrl(web_view)
    }

    private fun initToolBar() {
        custom_tool_bar.setOnBackPressListener {
            onBackPressed()
        }
        custom_tool_bar.titleText = mTitle
    }

    fun setCookie() {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val oldCookie = cookieManager.getCookie(mUrl)
            Timber.i("Cookie:oldCookie:$oldCookie")

            cookieManager.setCookie(mUrl, "x-session-token=" + URLEncoder.encode(viewModel.token, "utf-8")) //cookies是在HttpClient中获得的cookie
            cookieManager.flush()

            val newCookie = cookieManager.getCookie(mUrl)
            Timber.i("Cookie:newCookie:$newCookie")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun setupWebView(webView: WebView) {
        if (BuildConfig.DEBUG)
            WebView.setWebContentsDebuggingEnabled(true)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.domStorageEnabled = true //对H5支持
        settings.useWideViewPort = true //将图片调整到适合webview的大小
        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.databaseEnabled = false
        settings.setAppCacheEnabled(false)
        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val newWebView = WebView(view.context)
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        //20191120 記錄問題： target=_black 允許跳轉新窗口處理
                        //在此处进行跳转URL的处理, 一般情况下_black需要重新打开一个页面
                        try {
                            //使用系統默認外部瀏覽器跳轉
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
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

            // For Android 5.0+
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                mUploadCallbackAboveL = filePathCallback
                openImageChooserActivity()
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
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

                view.loadUrl(url)
                return true
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                //handler.proceed()
                val builder: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
                builder.setMessage(android.R.string.httpErrorUnsupportedScheme)
                builder.setPositiveButton("continue"
                ) { dialog, which -> handler.proceed() }
                builder.setNegativeButton("cancel"
                ) { dialog, which -> handler.cancel() }
                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
        }

        //H5调用系统下载
        webView.setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    fun loadUrl(webView: WebView) {
        webView.loadUrl(mUrl)
    }

    private fun openImageChooserActivity() {
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL?.onReceiveValue(arrayOf(it))
                mUploadCallbackAboveL = null
            } else {
                mUploadMessage?.onReceiveValue(it)
                mUploadMessage = null
            }
        }.launch(arrayOf("image/*"))
    }

}