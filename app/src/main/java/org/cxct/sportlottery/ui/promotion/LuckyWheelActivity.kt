package org.cxct.sportlottery.ui.promotion

import org.cxct.sportlottery.ui.common.WebActivity

class LuckyWheelActivity: WebActivity() {

    override fun onInitView() {
        binding.okWebView.cleanAllCache()
        super.onInitView()
    }
}