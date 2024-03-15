package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import org.cxct.sportlottery.databinding.DialogFirstDepositNoticeBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.modify.BindInfoViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.formatHTML
import org.cxct.sportlottery.view.webView.OkWebViewClient
import timber.log.Timber

class FirstDepositNoticeDialog: BaseDialog<BindInfoViewModel,DialogFirstDepositNoticeBinding>() {

    companion object{
        fun newInstance(content: String)= FirstDepositNoticeDialog().apply {
            arguments = Bundle().apply { putString("content",content) }
        }
    }
    init {
        marginHorizontal = 18.dp
    }

    private val content by lazy { requireArguments().getString("content")!! }

    override fun onInitView() {
        initView()
    }
    private fun initView()=binding.run {
        btnClose.setOnClickListener { dismiss() }
        wvContent.webViewClient = object : OkWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                Timber.d("shouldOverrideUrlLoading request:${request?.url} path:${request?.url?.path}")
                return true
            }
        }
        wvContent.loadData(content.formatHTML(), "text/html", null)
    }

}