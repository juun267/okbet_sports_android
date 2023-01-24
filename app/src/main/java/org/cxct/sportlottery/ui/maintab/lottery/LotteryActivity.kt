package org.cxct.sportlottery.ui.maintab.lottery

import android.os.Bundle
import android.webkit.JavascriptInterface
import org.cxct.sportlottery.ui.common.WebActivity

/**
 * Create by Simon Chang
 */
open class LotteryActivity : WebActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun init() {
        super.init()
        getWebView().addJavascriptInterface(LotteryJsInterface(this), LotteryJsInterface.name)
    }

    class LotteryJsInterface(val activity: LotteryActivity) {
        companion object {
            const val name = "LotteryJsInterface"
        }

        @JavascriptInterface
        fun backClick(infoString: String) {
            activity.onBackPressed()
        }

    }
}