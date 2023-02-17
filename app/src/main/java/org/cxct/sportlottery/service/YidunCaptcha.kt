//package org.cxct.sportlottery.service
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.LifecycleOwner
//import com.netease.nis.captcha.Captcha
//import com.netease.nis.captcha.Captcha.CloseType
//import com.netease.nis.captcha.CaptchaConfiguration
//import com.netease.nis.captcha.CaptchaListener
//import org.cxct.sportlottery.util.LanguageManager
//import org.cxct.sportlottery.util.LanguageManager.Language
//
//// 网易图形验证码
//object YidunCaptcha {
//
//    const val captchaId = "业务id"
//
//    private fun getLangType(context: Context): CaptchaConfiguration.LangType {
//        return when(LanguageManager.getSelectLanguage(context)) {
//            Language.ZH, Language.ZHT -> CaptchaConfiguration.LangType.LANG_ZH_CN
//            Language.VI -> CaptchaConfiguration.LangType.LANG_VI
//            Language.TH -> CaptchaConfiguration.LangType.LANG_TH
//            else -> CaptchaConfiguration.LangType.LANG_EN
//        }
//    }
//
//    fun validateAction(context: Context, lifecycleOwner: LifecycleOwner, onSuccess: () -> Unit, onFailed: (() -> Unit)? = null) {
//
//        val langType = getLangType(context)
//        val config = CaptchaConfiguration.Builder()
//            .captchaId(captchaId)
//            .languageType(langType)
//            .mode(CaptchaConfiguration.ModeType.MODE_CAPTCHA)
//            .listener(object : CaptchaListener {
//                override fun onReady() {}
//                override fun onValidate(result: String, validate: String, msg: String) {}
//                override fun onError(code: Int, msg: String) {
//                    Log.e("For Test", "======>>> YidunCaptcha onError ${code}  $msg")
//                }
//                override fun onClose(closeType: CloseType) {
//                    Log.e("For Test", "======>>> YidunCaptcha onClose ${closeType.name}")
//                    if (closeType == CloseType.VERIFY_SUCCESS_CLOSE) {
//                        onSuccess.invoke()
//                    } else {
//                        onFailed?.invoke()
//                    }
//                }
//        }).build(context)
//
//        val captcha = Captcha.getInstance().init(config)
//        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                if (event == Lifecycle.Event.ON_DESTROY) {
//                    kotlin.runCatching { captcha.destroy() }
//                }
//            }
//        })
//        captcha.validate()
//    }
//}