package org.cxct.sportlottery.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebView
import org.cxct.sportlottery.view.webView.OkWebViewClient
import org.cxct.sportlottery.view.webView.WebViewCallBack
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class WebActivityImp(val activity: BaseActivity<*>,val overrideUrlLoading: (view: WebView, url: String)->Boolean) {

    private var mUploadCallbackAboveL: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri?>? = null
    @SuppressLint("WebViewApiAvailability")
    fun setupWebView(webView: OkWebView) {
        webView.webChromeClient = object : OkWebChromeClient(
        ) {
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
                            activity.startActivity(i)
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
                fileChooserParams: FileChooserParams,
            ): Boolean {
                mUploadCallbackAboveL = filePathCallback
                openImageChooserActivity()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val PERMISSIONS_AT_WEBVIEW = 0
                val permissionCheck = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_AT_WEBVIEW
                    )
                } else {
                    request?.grant(request.resources)
                }
            }
        }


        webView.webViewClient = object : OkWebViewClient(object : WebViewCallBack {

            override fun pageStarted(view: View?, url: String?) {
                activity.loading()
            }

            override fun pageFinished(view: View?, url: String?) {
                activity.hideLoading()
            }

            override fun onError() {
            }
        }) {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?,
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return overrideUrlLoading(view, url)
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError,
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                //handler.proceed()
                if (activity.isFinishing) return
                AlertDialog.Builder(activity)
                    .setMessage(android.R.string.httpErrorUnsupportedScheme).setPositiveButton(
                        "continue"
                    ) { dialog, which -> handler.proceed() }.setNegativeButton(
                        "cancel"
                    ) { dialog, which -> handler.cancel() }.create().show()
            }

        }


        //H5调用系统下载
        webView.setDownloadListener { url, _, _, _, _ ->
            kotlin.runCatching { Uri.parse(url) }.getOrNull()?.let {
                val intent = Intent(Intent.ACTION_VIEW, it)
                activity.startActivity(intent)
            }
        }
    }
    open fun setCookie(url: String) {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val oldCookie = cookieManager.getCookie(url)
            Timber.i("Cookie:oldCookie:$oldCookie")

            cookieManager.setCookie(url, "x-session-token=" + URLEncoder.encode(LoginRepository.token, "utf-8")
            ) //cookies是在HttpClient中获得的cookie
            cookieManager.flush()

            val newCookie = cookieManager.getCookie(url)
            Timber.i("Cookie:newCookie:$newCookie")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun openImageChooserActivity() {
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL?.onReceiveValue(arrayOf(it!!))
                mUploadCallbackAboveL = null
            } else {
                mUploadMessage?.onReceiveValue(it)
                mUploadMessage = null
            }
        }.launch(arrayOf("image/*"))
    }
    fun loadUrl(webView: WebView,url: String) {
        webView.loadUrl(url)
    }
}