package org.cxct.sportlottery.ui.promotion

import kotlinx.android.synthetic.main.activity_web.*
import org.cxct.sportlottery.ui.common.WebActivity

class LuckyWheelActivity: WebActivity() {
    override fun setCookie() {
        okWebView.cleanAllCache()
        super.setCookie()
    }
}