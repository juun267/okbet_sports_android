package org.cxct.sportlottery.util

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.lottery.LotteryActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import timber.log.Timber

object JumpUtil {

    fun toInternalWeb(
        context: Context,
        href: String?,
        title: String?,
        toolbarVisibility: Boolean = true,
        backEvent: Boolean = true,
        bettingStation: BettingStation? = null
    ) {
        LogUtil.d("href:===>${href}")
        when{
            //是否世界杯主题活动页面
            href?.isNotEmpty() == true &&href?.contains("personal/BasketballWorldCupLottery")->{
                if (LoginRepository.isLogined()){
                   (AppManager.currentActivity() as MainTabActivity).jumpToWorldCupGame()
                }else{
                    AppManager.currentActivity().startLogin()
                }
            }
            else->{
                context.startActivity(
                    Intent(context, WebActivity::class.java).apply {
                        putExtra(WebActivity.KEY_URL, Constants.appendParams(href))
                        putExtra(WebActivity.KEY_TITLE, title)
                        putExtra(WebActivity.KEY_TOOLBAR_VISIBILITY, toolbarVisibility)
                        putExtra(WebActivity.KEY_BACK_EVENT, backEvent)
                        if (bettingStation != null) {
                            putExtra(WebActivity.BET_STATION, bettingStation)
                        }
                    }
                )
            }
        }
    }

    /**
     * 開啟外部瀏覽器
     *
     * @param context:
     * @param dnbUrl:  要跳轉的 url
     */
    fun toExternalWeb(context: Context, dnbUrl: String?) {
        try {
            Timber.d("跳转URL:${dnbUrl}")
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(dnbUrl))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    //跳轉第三方遊戲網頁
    fun toThirdGameWeb(context: Context, href: String, thirdGameCategoryCode: String) {

//        if ("CQ9"== thirdGameCategoryCode) {  // CQ9 有兼容问题特殊处理，用外部浏览器打开
//            runWithCatch {
//                val i = Intent(Intent.ACTION_VIEW, Uri.parse(href))
//                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                context.startActivity(i)
//            }
//            return
//        }


        try {
            Timber.i("跳转到链接:$href")
            if (URLUtil.isValidUrl(href)) {
                context.startActivity(
                    Intent(context, ThirdGameActivity::class.java).putExtra(
                        WebActivity.KEY_URL,
                        href
                    )
                        .putExtra(WebActivity.GAME_CATEGORY_CODE, thirdGameCategoryCode)
                )
            } else {
                throw Exception(href) //20191022 記錄問題：當網址無效時，代表他回傳的 url 是錯誤訊息
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    fun openEmail(context: Context, extraEmail: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ClipDescription.MIMETYPE_TEXT_PLAIN
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(extraEmail))
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    fun toLottery(
        context: Context,
        href: String?,
    ) {
        href?.let { LogUtil.d(href) }
        context.startActivity(
            Intent(context, LotteryActivity::class.java).apply {
                putExtra(WebActivity.KEY_URL, href)
                putExtra(WebActivity.KEY_TITLE, "")
                putExtra(WebActivity.KEY_TOOLBAR_VISIBILITY, false)
                putExtra(WebActivity.KEY_BACK_EVENT, true)
            }
        )
    }
}