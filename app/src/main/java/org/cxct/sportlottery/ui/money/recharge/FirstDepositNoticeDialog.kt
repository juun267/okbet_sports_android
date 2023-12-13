package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogFirstDepositNoticeBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.modify.BindInfoViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.formatHTML
import org.cxct.sportlottery.view.webView.OkWebViewClient
import timber.log.Timber

class FirstDepositNoticeDialog(val content: String): BaseDialog<BindInfoViewModel>(BindInfoViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }
    lateinit var binding : DialogFirstDepositNoticeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DialogFirstDepositNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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