package org.cxct.sportlottery.ui.login

import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.databinding.DialogCaptchaBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil

/**
 * 顯示棋牌彈窗
 */
class CaptchaDialog : BaseDialog<LoginViewModel,DialogCaptchaBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView() {
        binding.okWebView.apply {
            setBackgroundColor(0)
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
            if (ret !=0) {
                dismiss()
                return
            }

            post {
                val fragment = parentFragment
                val act = activity
                if (fragment is VerifyCallback) {
                    fragment.onVerifySucceed(ticket,randstr, tag)
                } else if (act is VerifyCallback) {
                    act.onVerifySucceed(ticket,randstr, tag)
                }
                dismiss()
            }
        }
    }
}