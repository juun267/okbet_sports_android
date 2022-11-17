package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.Context
import android.text.TextUtils
import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.fromJson

class WorldCupJsInterface(val context: Context) {

    companion object {
        const val name = "worldCupJsInterface"
    }

    @JavascriptInterface
    fun tapAndroidEvent(param: String) {
        if (TextUtils.isEmpty(param)) {
            ToastUtil.showToast(context, R.string.bad_param)
            return
        }
        toSportDetailActivity(param)
    }

    private fun toSportDetailActivity(infoString: String) {

        val matchInfo: MatchInfo? = infoString.fromJson()
        if (matchInfo == null) {
            ToastUtil.showToast(context, R.string.bad_param)
            return
        }

        SportDetailActivity.startActivity(context, matchInfo)
    }

}