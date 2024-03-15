package org.cxct.sportlottery.ui.login

import android.os.Bundle
import android.os.Parcelable
import android.webkit.JavascriptInterface
import kotlinx.android.parcel.Parcelize
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
class CaptchaDialog : BaseDialog<LoginViewModel,DialogCaptchaBinding>() {
    companion object{
        fun newInstance(callBack: CallBack)= CaptchaDialog().apply {
            arguments = Bundle().apply {
                putParcelable("callBack",callBack)
            }
        }
    }
    init {
        setStyle(R.style.FullScreen)
    }
    private val callBack by lazy { requireArguments().getParcelable<CallBack>("callBack")!! }

    @Parcelize
    class CallBack(val callBack: ((ticket: String, randstr: String) -> Unit)) : Parcelable {
        fun onCall(ticket: String, randstr: String){
            callBack.invoke(ticket,randstr)
        }
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
            if (ret==0) {
                this@CaptchaDialog.activity?.runOnUiThread {
                    callBack.onCall(ticket,randstr)
                }
            }
            dismiss()
        }
    }
}