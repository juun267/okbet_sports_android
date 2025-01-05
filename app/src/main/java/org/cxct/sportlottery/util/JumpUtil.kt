package org.cxct.sportlottery.util

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.game.dropball.DropBallActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.lottery.LotteryActivity
import org.cxct.sportlottery.ui.profileCenter.invite.InviteActivity
import org.cxct.sportlottery.ui.profileCenter.vip.VipBenefitsActivity
import org.cxct.sportlottery.ui.promotion.LuckyWheelActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import timber.log.Timber

object JumpUtil {

    val sportStartPrefix = listOf("mobile/sports/","oksports/","okesports/")
    fun toInternalWeb(
        context: Context,
        href: String,
        title: String?,
        toolbarVisibility: Boolean = true,
        backEvent: Boolean = true,
        bettingStation: BettingStation? = null,
        tag: String? = null
    ) {
        LogUtil.d("href:===>${href}")
        if (href.isNullOrEmpty()){
            return
        }
//                /mobile/games/oklive -> oklive頁
//                /mobile/games/okgame -> okgame頁
//                /mobile/sports/today/ES/SB:CSGO -> esport頁
//                /mobile/sports/inPlay/BK -> sport頁
//                /mobile/personal/activity_v2 ->  优惠活动列表页
        val path = href.getUrlPathExcludeHost()
        LogUtil.d("path=$path")
        when{
              path.contains("sweepstakes") ->{
                  toLottery(context, Constants.getLotteryH5Url(context, LoginRepository.token))
              }
            path.contains("mobile/christmas") ->{
                loginedRun(context, true) {
                    context.startActivity(
                        Intent(context, WebActivity::class.java).apply {
                            putExtra(WebActivity.KEY_URL, Constants.appendParams(href))
                            putExtra(WebActivity.KEY_TITLE, title)
                            putExtra(WebActivity.KEY_TOOLBAR_VISIBILITY, toolbarVisibility)
                            putExtra(WebActivity.KEY_BACK_EVENT, backEvent)
                            putExtra(WebActivity.TAG, WebActivity.TAG_HALLOWEEN)
                        }
                    )
                }
                return
            }
            path == "mobile/user/vipCenter"
                    || path == "vipCenter"->{
                loginedRun(context,true){
                    (context as AppCompatActivity).startActivity(VipBenefitsActivity::class.java)
                }
            }
            path == "mobile/inviteFriends"->{
                loginedRun(context,true){
                    (context as AppCompatActivity).startActivity(InviteActivity::class.java)
                }
            }
            path == "mobile/promo-dropball"->{
                loginedRun(context,true){
                    (context as AppCompatActivity).startActivity(DropBallActivity::class.java)
                }
            }

            path == "mobile/personal/activity_v2"
                    || path == "promo"->{
                PromotionListActivity.startFrom(context, "内嵌H5跳转")
            }
            path == "mobile/games/okgame"
                    || path == "okgames"->{
                (context as? MainTabActivity)?.let {
                    it.jumpToOKGames()
                }
            }
            path== "mobile/games/oklive"
                    || path == "oklive"->{
                (context as? MainTabActivity)?.let {
                    it.jumpToOkLive()
                }
            }
            sportStartPrefix.any { path.startsWith(it)}->{
                sportStartPrefix.firstOrNull { path.startsWith(it) }
                    ?.let { startPrefix->
                        val sportParams=path.substringAfter(startPrefix).split("/")
                        LogUtil.toJson(sportParams)
                        if (startPrefix == sportStartPrefix[0]){
                            val matchType = sportParams.getOrNull(0)
                            val gameType = sportParams.getOrNull(1)
                            val categoryType = sportParams.getOrNull(2)
                            toSport(matchType,gameType,categoryType)
                        }else{
                            val matchType = sportParams.getOrNull(0)
                            val gameType = if(startPrefix == sportStartPrefix[1]) sportParams.getOrNull(1) else GameType.ES.key
                            val categoryType = if(startPrefix == sportStartPrefix[1]) sportParams.getOrNull(2) else sportParams.getOrNull(1)
                            toSport(matchType,gameType,categoryType)
                        }
                    }
            }
            else->{
                context.startActivity(
                    Intent(context, WebActivity::class.java).apply {
                        putExtra(WebActivity.KEY_URL, Constants.appendParams(href))
                        putExtra(WebActivity.KEY_TITLE, title)
                        putExtra(WebActivity.KEY_TOOLBAR_VISIBILITY, toolbarVisibility)
                        putExtra(WebActivity.KEY_BACK_EVENT, backEvent)
                        putExtra(WebActivity.TAG, tag)
                        if (bettingStation != null) {
                            putExtra(WebActivity.BET_STATION, bettingStation)
                        }
                    }
                )
            }
        }
    }

    /**
     * 获取url域名后的路径
     */
    private fun String.getUrlPathExcludeHost(): String = substringAfterLast("://").substringAfter("/").substringBefore("?")

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

    /**
     * gameType: OK_GAMES、OK_LIVE、OK_BINGO、OK_SPORT
     */
    //跳轉第三方遊戲網頁
    fun toThirdGameWeb(context: Context, href: String, firmType: String, okGameBean: OKGameBean?, guestLogin: Boolean = false) {
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
                val intent = Intent(context, ThirdGameActivity::class.java)
                intent.putExtra(WebActivity.KEY_URL, href)
                intent.putExtra(WebActivity.FIRM_TYPE, firmType)
                intent.putExtra(WebActivity.GAME_BEAN, okGameBean)
                intent.putExtra(WebActivity.GUESTLOGIN, guestLogin)
                context.startActivity(intent)
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
    fun toSport(matchType: String?, gameType: String?,categoryType: String?){
        (AppManager.currentActivity() as? MainTabActivity)?.let {
            if (gameType==GameType.ES.key){
                it.jumpToESport(matchType = MatchType.getMatchType(matchType),categoryType)
            }else{
                it.jumpToTheSport(matchType = MatchType.getMatchType(matchType),gameType = GameType.getGameType(gameType))
            }
        }
    }
}