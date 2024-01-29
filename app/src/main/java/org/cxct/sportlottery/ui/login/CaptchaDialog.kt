package org.cxct.sportlottery.ui.login

import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogCaptchaBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil

/**
 * 顯示棋牌彈窗
 */
class CaptchaDialog(val callback: (ticket: String, randstr: String)-> Unit) : BaseDialog<LoginViewModel,DialogCaptchaBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView() {
        binding.okWebView.apply {
            setBackgroundColor(0);
            background.alpha = 0
            addJavascriptInterface(JsBridge(),"jsBridge")
            val lang = when(LanguageManager.getSelectLanguage(context)){
                LanguageManager.Language.ZH->"zh-cn"
                LanguageManager.Language.ZHT->"zh-tw"
                LanguageManager.Language.VI->"vi"
                else->"en"
            }
            loadUrl("file:///android_asset/captcha.html?appid=${sConfigData?.captchaAppId}&lang=${lang}")
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