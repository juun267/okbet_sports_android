package org.cxct.sportlottery.view.webView

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber

/**
 * webViewClient
 * 处理各种通知&请求事件
 */
open class OkWebViewClient(
) : WebViewClient() {

    private val TAG = "OkWebViewClient"
    private var mWebViewCallBack: WebViewCallBack? = null

    constructor(mWebViewCallBack: WebViewCallBack) : this() {
        this.mWebViewCallBack = mWebViewCallBack
    }


    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Timber.d(TAG,"onPageStarted:url = $url")
        mWebViewCallBack?.pageStarted(view,url)

    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Timber.d(TAG,"onPageFinished:url = $url")
        mWebViewCallBack?.pageFinished(view,url)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Timber.d(TAG,"onReceivedError:error=$error")
        mWebViewCallBack?.onError()
    }



}