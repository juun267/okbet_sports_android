package org.cxct.sportlottery.view.webView

import android.view.View

interface WebViewCallBack {
    fun pageStarted(view: View?, url: String?)
    fun pageFinished(view:View?,url: String?)
    fun onError()
    fun updateTitle(title: String?){}
}