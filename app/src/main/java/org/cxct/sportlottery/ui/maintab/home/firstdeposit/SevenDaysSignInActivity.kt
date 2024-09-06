package org.cxct.sportlottery.ui.maintab.home.firstdeposit

import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.databinding.ActivitySevenDaysSigninBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel

/**
 * 七天签到页面
 */
class SevenDaysSignInActivity: BaseActivity<MainViewModel, ActivitySevenDaysSigninBinding>() {
    override fun pageName() = "七天签到页面"
    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.ivClose.setOnClickListener { finish() }
        binding.okWebView.bindLifecycleOwner(this)
        binding.okWebView.addJavascriptInterface(SignEventBridge(), "sevenCheckInJsInterface")

        val url = Constants.appendParams(Constants.getSevenCheckInUrl(this)) + "&d=android"
        binding.okWebView.loadUrl(url)

    }


    private class SignEventBridge {

        @JavascriptInterface
        fun onSubmitCallback(name: String?, id: String?) {
            if (name != null && id != null) {
                SensorsEventUtil.activitySignInEvent(id, name)
            }
        }
    }


}

