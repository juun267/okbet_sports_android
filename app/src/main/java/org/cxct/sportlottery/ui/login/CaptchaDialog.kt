package org.cxct.sportlottery.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogCaptchaBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.LogUtil

/**
 * 顯示棋牌彈窗
 */
class CaptchaDialog(val callback: (ticket: String, randstr: String)-> Unit) : BaseDialog<LoginViewModel>(LoginViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }

    lateinit var binding: DialogCaptchaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogCaptchaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okWebView.apply {
            setBackgroundColor(0);
            background.alpha = 0
            addJavascriptInterface(JsBridge(),"jsBridge")
            loadUrl("file:///android_asset/captcha.html?appid=${sConfigData?.captchaAppId}")
        }

    }
    inner class JsBridge {
        @JavascriptInterface
        fun notify(ret: Int,ticket: String,randstr: String) {
            LogUtil.d("ret=${ret},ticket=${ticket},randstr=${randstr}")
            if (ret==0) {
                this@CaptchaDialog.activity?.runOnUiThread {
                    callback.invoke(ticket,randstr)
                }
            }
            dismiss()
        }
    }
}