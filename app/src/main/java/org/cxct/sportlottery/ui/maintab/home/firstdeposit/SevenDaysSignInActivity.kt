package org.cxct.sportlottery.ui.maintab.home.firstdeposit

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySevenDaysSigninBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel

/**
 * 七天签到页面
 */
class SevenDaysSignInActivity: BaseActivity<MainViewModel, ActivitySevenDaysSigninBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.ivClose.setOnClickListener { finish() }
        binding.okWebView.bindLifecycleOwner(this)
        Constants.appendParams(Constants.getSevenCheckInUrl(this))?.let { binding.okWebView.loadUrl(it) }
    }
}