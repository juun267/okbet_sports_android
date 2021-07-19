package org.cxct.sportlottery.network

import android.content.Context
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Constants {
    val SERVER_URL_LIST = listOf("app66app.com", "app99app.vip", "app66app.vip", "app88app.vip")
    var currentServerUrl: String? = null  //當前選擇的的 server url (後續 CheckAppUpdate API 會用到)
    private var mBaseUrl = ""

    fun setBaseUrl(baseUrl: String) {
        mBaseUrl = baseUrl
    }

    fun getBaseUrl(): String {
        return mBaseUrl.httpFormat()
    }

    //20210401 記錄問題：retrofit.setBaseUrl() format http://[isNotEmpty]/ or https://[isNotEmpty]/，否則會直接 exception
    fun String.httpFormat(): String {
        val regex = "^http[s]?://.+".toRegex()
        return this.let {
            if (it.isEmpty()) "https://default/" else it
        }.let {
            if (it.contains(regex)) it else "https://$it"
        }.let {
            if (it.endsWith("/")) it else "$it/"
        }
    }

    //優惠活動 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getPromotionUrl(token: String?): String? {
        return try {
            "${getBaseUrl()}/activity/mobile/#/useractilist?token=${
                URLEncoder.encode(
                    token,
                    "utf-8"
                )
            }"
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    //遊戲規則 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getGameRuleUrl(context: Context): String? {

        return try {
            when (getSelectLanguage(context)) {
                LanguageManager.Language.ZH -> "https://sportsrule.cxct.org/"
                else -> "https://sportsrule.cxct.org/us"
            }

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    //獲取檢查APP是否有更新版本的URL //輪詢 SERVER_URL_LIST 成功的那組 serverUrl 用來 download .apk
    fun getCheckAppUpdateUrl(serverUrl: String?): String {
        return "https://download." + serverUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/version-Android.json"
    }

    //.apk 下載 url
    fun getAppDownloadUrl(): String {
        return "https://download." + currentServerUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/cp.apk"
    }

    fun getHostListUrl(serverUrl: String?): String {
        return "https://${BuildConfig.CHANNEL_NAME}.$serverUrl/api/front/domainconfig/appdomain/${BuildConfig.CHANNEL_NAME}.json"
    }

    fun getGameRuleUrl(context: Context, type: String): String? {
        return try {
            when (getSelectLanguage(context)) {

                LanguageManager.Language.ZH -> "https://sportsrule.cxct.org/".plus("?type=$type")

                else -> "https://sportsrule.cxct.org/us".plus("?type=$type")
            }

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    //bet
    const val MATCH_BET_INFO = "/api/front/match/bet/info"
    const val MATCH_BET_ADD = "/api/front/match/bet/add"
    const val MATCH_BET_LIST = "/api/front/match/bet/list"
    const val MATCH_BET_SETTLED_LIST = "/api/front/match/bet/settled/list"
    const val MATCH_BET_SETTLED_DETAIL_LIST = "/api/front/match/bet/settled/detail/list"

    //index
    const val INDEX_LOGIN = "/api/front/index/login"
    const val INDEX_LOGOUT = "/api/front/index/logout"
    const val INDEX_CONFIG = "/api/front/index/config.json" //获取配置信息
    const val INDEX_VALIDATE_CODE = "/api/front/index/getvalidatecode" //获取验证码
    const val INDEX_REGISTER = "/api/front/index/register" //注册用户
    const val INDEX_SEND_SMS = "/api/front/index/sendSms" //发送验证码
    const val INDEX_CHECK_EXIST = "/api/front/index/checkexist/{userName}" //检查账号名称是否已存在
    const val INDEX_CHECK_TOKEN = "/api/front/index/checktoken" //验证token 是否过期
    const val LOGIN_FOR_GUEST = "/api/front/index/loginforguest" //游客登录
    const val INDEX_PROMOTENOTICE =
        "/api/front/index/promotenotice" //公告API 未登入不帶token 在投注区查询的时候带[1] 在首页的时候带[2,3]

    //parlay limit
    const val PLAYQUOTACOM_LIST = "/api/front/playQuotaCom/list" //获取所有体育玩法限额

    //league
    const val LEAGUE_LIST = "/api/front/match/league/list"

    //match
    const val MATCH_PRELOAD = "/api/front/match/preload"

    //match result
    const val MATCH_RESULT_LIST = "/api/front/match/result/list"
    const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

    //odds
    const val MATCH_ODDS_LIST = "/api/front/match/odds/simple/list"
    const val MATCH_ODDS_DETAIL = "/api/front/match/odds/detail"
    const val MATCH_ODDS_EPS_LIST = "/api/front/match/odds/eps/list"

    //sport
    const val SPORT_MENU = "/api/front/sport/mobile/menu"
    const val SPORT_QUERY = "/api/front/sport/query"

    const val MYFAVORITE_QUERY = "/api/front/myFavorite/query"
    const val MYFAVORITE_MATCH_QUERY = "/api/front/myFavorite/match/query"
    const val MYFAVORITE_SAVE = "/api/front/myFavorite/save"

    //play category list
    const val PLAYCATE_TYPE_LIST = "/api/front/playcate/type/list"

    //outright
    const val OUTRIGHT_ODDS_LIST = "/api/front/outright/odds/list"
    const val OUTRIGHT_RESULT_LIST = "/api/front/outright/result/list"
    const val OUTRIGHT_SEASON_LIST = "/api/front/outright/season/list"
    const val OUTRIGHT_BET_ADD = "/api/front/outright/bet/add"
    const val OUTRIGHT_BET_INFO = "/api/front/outright/bet/info"


    //infoCenter
    const val USER_NOTICE_LIST = "/api/front/user/notice/list"
    const val USER_NOTICE_READED = "/api/front/user/notice/readed/{id}"

    //vip / member level
    const val USER_LEVEL_GROWTH = "/api/front/user/level/getLevelGrowth"
    const val THIRD_REBATES = "/api/front/playcom/thirdRebates"

    //money
    const val RECHARGE_CONFIG_MAP = "/api/front/rechcfg/map"
    const val USER_RECHARGE_ADD = "/api/front/userrech/add"
    const val USER_RECHARGE_ONLINE_PAY = "/api/front/userrech/onlinepay"
    const val USER_RECHARGE_LIST = "/api/front/userrech/list"

    //user
    const val USER_INFO = "/api/front/user/info"
    const val USER_MONEY = "/api/front/user/money"
    const val USER_EDIT_NICKNAME = "/api/front/user/editNickName" //更新昵称
    const val USER_EDIT_ICON_URL = "/api/front/user/editIconUrl" //更新头像
    const val USER_UPDATE_PWD = "/api/front/user/updatepwd" //更新密码
    const val USER_UPDATE_FUND_PWD = "/api/front/user/updatefundpwd" //更新资金密码
    const val USER_WITHDRAW_INFO = "/api/front/user/setWdUserInfo" //設置提款資料

    //upload image
    const val UPLOAD_IMG = "/api/upload/image" //上传图片

    //bank
    const val BANK_MY = "/api/front/user/bank/my"
    const val BANK_ADD = "/api/front/user/bank/add"
    const val BANK_DELETE = "/api/front/user/bank/delete"

    //withdraw
    const val WITHDRAW_ADD = "/api/front/userwithdraw/add"
    const val WITHDRAW_LIST = "/api/front/userwithdraw/list"

    //feedback
    const val FEEDBACK_QUERYLIST = "/api/front/feedback/querylist"
    const val FEEDBACK_SAVE = "/api/front/feedback/save"
    const val FEEDBACK_REPLY = "/api/front/feedback/reply"
    const val FEEDBACK_QUERYDETAIL = "/api/front/feedback/querydetail/{id}"

    //third game
    const val THIRD_GAMES = "/api/front/index/thirdgames"
    const val THIRD_GET_ALL_BALANCE = "/api/front/thirdapi/getAllBalance"
    const val THIRD_ALL_TRANSFER_OUT = "/api/front/thirdapi/allTransferOut"
    const val THIRD_TRANSFER = "/api/front/thirdapi/{outPlat}/{inPlat}/transfer?=amount"
    const val THIRD_QUERY_TRANSFERS = "/api/front/thirdapi/queryTransfers"
    const val THIRD_AUTO_TRANSFER =
        "/api/front/thirdapi/{inPlat}/autoTransfer" //自动转入、转出（先将第三方都转至彩票，再将彩票的余额转至第三方）
    const val THIRD_LOGIN = "/api/front/thirdapi/{firmType}/login" //登录

    const val QUERY_FIRST_ORDERS = "/api/front/thirdapi/queryFirstOrders"
    const val QUERY_SECOND_ORDERS = "/api/front/thirdapi/querySecondOrders"

    const val MATCH_CATEGORY_SPECIAL_MATCH = "/api/front/matchCategory/special/match/query" //查詢主頁精選賽事
    const val MATCH_CATEGORY_SPECIAL_MENU = "/api/front/matchCategory/special/menu/query" //查詢主頁精選賽事菜单

    //timeout
    const val CONNECT_TIMEOUT: Long = 15 * 1000
    const val WRITE_TIMEOUT: Long = 15 * 1000
    const val READ_TIMEOUT: Long = 15 * 1000

    //rule type
    const val COMBO = "combo"
}