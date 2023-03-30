package org.cxct.sportlottery.ui.common

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
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.view_bettingstation_info.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor
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
        const val KEY_BACK_EVENT = "key-back-event"
        const val GAME_CATEGORY_CODE = "game-category-code"
        const val BET_STATION = "betstation"
    }

    private val mTitle: String by lazy { intent?.getStringExtra(KEY_TITLE) ?: "" }
    private val mUrl: String by lazy { intent?.getStringExtra(KEY_URL) ?: "about:blank" }
    private val mToolbarVisibility: Boolean by lazy {
        intent?.getBooleanExtra(
            KEY_TOOLBAR_VISIBILITY, true
        ) ?: true
    }
    private val mBackEvent: Boolean by lazy {
        intent?.getBooleanExtra(KEY_BACK_EVENT, true) ?: true
    }
    private val bettingStation: BettingStation? by lazy { intent?.getSerializableExtra(BET_STATION) as? BettingStation }
    private var mUploadCallbackAboveL: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    fun getWebView(): WebView {
        return web_view
    }

    open fun init() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
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
        bettingStation?.let {
            addBetStationInfo()
        }
    }

    fun setCookie() {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val oldCookie = cookieManager.getCookie(mUrl)
            Timber.i("Cookie:oldCookie:$oldCookie")

            cookieManager.setCookie(
                mUrl, "x-session-token=" + URLEncoder.encode(viewModel.token, "utf-8")
            ) //cookies是在HttpClient中获得的cookie
            cookieManager.flush()

            val newCookie = cookieManager.getCookie(mUrl)
            Timber.i("Cookie:newCookie:$newCookie")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("WebViewApiAvailability")
    fun setupWebView(webView: WebView) {
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)

        webView.setWebViewCommonBackgroundColor()

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
//        settings.setAppCacheEnabled(false)

        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
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

            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mUploadCallbackAboveL = filePathCallback
                openImageChooserActivity()
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

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
            }

            override fun shouldInterceptRequest(
                view: WebView?, url: String?
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

                view.loadUrl(url)
                return true
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                //handler.proceed()
                if (isFinishing)
                    return
                AlertDialog.Builder(this@WebActivity)
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
                mUploadCallbackAboveL?.onReceiveValue(arrayOf(it!!))
                mUploadCallbackAboveL = null
            } else {
                mUploadMessage?.onReceiveValue(it)
                mUploadMessage = null
            }
        }.launch(arrayOf("image/*"))
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun addBetStationInfo() {
        lin_betstation.visibility = View.VISIBLE
        bettingStation?.let {
            with(lin_betstation) {
                tv_address.text = it.addr
                tv_mobile.text = it.telephone
                var startTime = if (it.officeStartTime.isNotBlank()) it.officeStartTime else "00:00"
                var endTime = if (it.officeEndTime.isNotBlank()) it.officeEndTime else "00:00"
                tv_time.text = startTime + "-" + endTime
                tv_appointment_time.text = it.appointmentTime
                lin_appointment_time.visibility =
                    if (it.appointmentTime.isNullOrBlank()) View.GONE else View.VISIBLE
                tv_mobile.setOnClickListener {
                    tv_mobile.text.toString().let {
                        if (it.isNotBlank()) {
                            val intent = Intent();
                            intent.action = Intent.ACTION_DIAL
                            intent.data = Uri.parse("tel:" + it)
                            try {
                                startActivity(intent)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                tv_address.setOnClickListener {
                    var url =
                        "https://maps.google.com/?q=@" + bettingStation!!.lon + "," + bettingStation!!.lat
                    JumpUtil.toExternalWeb(this@WebActivity, url)
                }
            }
        }
    }
}