package org.cxct.sportlottery.network

import android.content.Context
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import org.cxct.sportlottery.util.isMultipleSitePlat
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Constants {
    val SERVER_URL_LIST = listOf("cscdawww.com", "mcziulal.com", "toomzqyq.com", "oiobnbss.com")
    var currentServerUrl: String? = null  //當前選擇的的 server url (後續 CheckAppUpdate API 會用到)
    private var mBaseUrl = ""
    private var mSocketUrl = ""


    fun setBaseUrl(baseUrl: String) {
        mBaseUrl = baseUrl
    }

    fun getBaseUrl(): String {
        return mBaseUrl.httpFormat()
    }

    fun setSocketUrl(baseUrl: String) {
        mSocketUrl = baseUrl
    }

    fun getSocketUrl(): String {
        return mSocketUrl
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
    fun getPromotionUrl(token: String?, language: LanguageManager.Language): String? {
        return try {
            "${getBaseUrl()}/activity/mobile/#/useractilist?lang=${language.key}&token=${
                URLEncoder.encode(
                    token,
                    "utf-8"
                )
            }${
                if (isMultipleSitePlat()) {
                    "&platform=onbet"
                } else {
                    ""
                }
            }"
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 在url拼接语言字符
     */
    fun getLanguageTag(context: Context): String {
        return when (getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> ""
            LanguageManager.Language.VI -> "vi/"
            else -> "us/"
        }
    }

    //遊戲規則 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getGameRuleUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}?platform=" + context.getString(
            R.string.app_name
        )
    }

    //關於我們
    fun getAboutUsUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}about-us?platform=" + context.getString(
            R.string.app_name
        )
    }

    //博彩责任
    fun getDutyRuleUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}responsibility?platform=" + context.getString(
            R.string.app_name
        )
    }

    //代理加盟
    fun getAffiliateUrl(context: Context): String {
        return "${getBaseUrl()}sports-rule/#/${getLanguageTag(context)}affiliate?platform=${
            context.getString(
                R.string.app_name
            )
        }"
    }

    //隐私权政策
    fun getPrivacyRuleUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}privacy-policy?platform=" + context.getString(
            R.string.app_name
        )
    }

    //规则与条款
    fun getAgreementRuleUrl(context: Context): String? {

        val checkCreditQuery = if (sConfigData?.creditSystem == 1) "credit=1" else context.getString(R.string.app_name)

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}terms-conditions?" + checkCreditQuery
    }

    //KYC人工客服審核
    fun getKYVUrl(context: Context): String? {
        return try {
              "https://okbetsports.ladesk.com/scripts/generateWidget.php?v=5.30.5.9&t=1658377154&cwid=6h5p37dx&cwrt=V&cwt=phone_popout&vid=qh2gmm3j339kbfmwswxdikkz8woqc&ud=%7B%7D&pt=Document&ref=file%3A%2F%2F%2FUsers%2Flingzhang%2FDesktop%2Ftest%2Findex.html#startScreen"
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    //常见问题
    fun getFAQsUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}faq?platform=" + context.getString(
            R.string.app_name
        )
    }

    //联系我们
    fun getContactUrl(context: Context): String? {

        return getBaseUrl() + "sports-rule/#/${getLanguageTag(context)}contact-us?platform=" + context.getString(
            R.string.app_name
        ) + "&service=" + URLEncoder.encode(sConfigData?.customerServiceUrl ?: "", "utf-8")
    }

    //web页面增加夜间模式参数
    fun appendMode(url: String?): String? {
        if (url.isNullOrEmpty() || url.contains("mode=")) {
            return url
        }
        return url + (if (url.contains("?")) "&" else "?") + "mode=" + (if (MultiLanguagesApplication.isNightMode) "night" else "day") + "&from=android"
    }

    //獲取檢查APP是否有更新版本的URL //輪詢 SERVER_URL_LIST 成功的那組 serverUrl 用來 download .apk
    fun getCheckAppUpdateUrl(serverUrl: String?): String {
        return "https://download." + serverUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/version-Android.json"
    }

    //.apk 下載 url
    fun getAppDownloadUrl(): String {
        return "https://download." + currentServerUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/OKbet.apk"
    }

    fun getHostListUrl(serverUrl: String?): String {
        return "https://${BuildConfig.CHANNEL_NAME}.$serverUrl/api/front/domainconfig/appdomain/${BuildConfig.CHANNEL_NAME}.json"
    }

    //bet
    const val MATCH_BET_INFO = "/api/front/match/bet/info"
    const val MATCH_BET_ADD = "/api/front/match/bet/add"
    const val MATCH_BET_LIST = "/api/front/match/bet/list"
    const val MATCH_BET_SETTLED_LIST = "/api/front/match/bet/settled/list"
    const val MATCH_BET_SETTLED_DETAIL_LIST = "/api/front/match/bet/settled/detail/list"

    //index
    const val INDEX_LOGIN = "/api/front/index/login"
    const val INDEX_SEND_LOGIN_DEVICE_SMS = "/api/front/index/sendLoginDeviceSms"
    const val INDEX_VALIDATE_LOGIN_DEVICE_SMS = "/api/front/index/validateLoginDeviceSms"
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
    const val MATCH_LIVE_URL = "/api/front/match/live/url"
    const val MATCH_TRACKER_URL = "/api/front/match/tracker/url/{mappingId}"

    //match result
    const val MATCH_RESULT_LIST = "/api/front/match/result/list"
    const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

    //odds
    const val MATCH_ODDS_LIST = "/api/front/match/odds/simple/list"
    const val MATCH_ODDS_DETAIL = "/api/front/match/odds/detail"
    const val MATCH_ODDS_EPS_LIST = "/api/front/match/odds/eps/list"
    const val MATCH_ODDS_QUICK_LIST = "/api/front/match/odds/quick/list"

    //sport
    const val SPORT_LIST = "/api/front/sport/list"
    const val SPORT_MENU = "/api/front/sport/mobile/menu"
    const val SPORT_QUERY = "/api/front/sport/query"
    const val SPORT_COUPON_MENU = "/api/front/sport/coupon/menu"
    const val SPORT_SEARCH_ENGINE = "/api/front/sport/searchEngine"
    const val SPORT_PUBLICITY_RECOMMEND = "/api/front/sport/recommend"
    const val SPORT_MENU_FILTER = "/api/front/sport/menu/list"


    const val MYFAVORITE_QUERY = "/api/front/myFavorite/query"
    const val MYFAVORITE_MATCH_QUERY = "/api/front/myFavorite/match/query"
    const val MYFAVORITE_SAVE = "/api/front/myFavorite/save"
    const val MYFAVORITE_QUERY_ALL = "/api/front/myFavorite/match/queryAll"

    //play category list
    const val PLAYCATE_TYPE_LIST = "/api/front/playcate/type/list"

    //outright
    const val OUTRIGHT_ODDS_LIST = "/api/front/outright/odds/list"
    const val OUTRIGHT_RESULT_LIST = "/api/front/outright/result/list"
    const val OUTRIGHT_LEAGUE_LIST = "/api/front/outright/league/list" // outright/season/list棄用
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
    const val USER_BILL_LIST = "/api/front/sportBill/query"


    //user
    const val USER_INFO = "/api/front/user/info"
    const val USER_MONEY = "/api/front/user/money"
    const val USER_EDIT_NICKNAME = "/api/front/user/editNickName" //更新昵称
    const val USER_EDIT_ICON_URL = "/api/front/user/editIconUrl" //更新头像
    const val USER_UPDATE_PWD = "/api/front/user/updatepwd" //更新密码
    const val USER_UPDATE_FUND_PWD = "/api/front/user/updatefundpwd" //更新资金密码
    const val USER_WITHDRAW_INFO = "/api/front/user/setWdUserInfo" //設置提款資料
    const val USER_CREDIT_CIRCLE_HISTORY = "/api/front/user/credit/circle/history"
    const val USER_BET_LIMIT = "/api/front/user/setPerBetLimit"
    const val USER_FROZE = "/api/front/user/setFroze"
    const val LOCK_MONEY = "/api/front/user/lockMoney"
    const val PASSWORD_VERIFY = "/api/front/user/passwordVerify"

    //upload image
    const val UPLOAD_IMG = "/api/upload/image" //上传图片
    const val UPLOAD_VERIFY_PHOTO = "/api/front/user/uploadVerifyPhoto" //上傳實名制文件

    //簡訊碼驗證
    const val GET_TWO_FACTOR_STATUS = "/api/front/user/getTwoFactorValidateStatus" //取得双重验证状态(success: true 验证成功, false 需重新验证手机)
    const val SEND_TWO_FACTOR = "/api/front/index/sendTwoFactor" //发送双重验证讯息
    const val VALIDATE_TWO_FACTOR = "/api/front/index/validateTwoFactor" //双重验证校验

    //bank
    const val BANK_MY = "/api/front/user/bank/my"
    const val BANK_ADD = "/api/front/user/bank/add"
    const val BANK_DELETE = "/api/front/user/bank/delete"

    //withdraw
    const val WITHDRAW_ADD = "/api/front/userwithdraw/add"
    const val WITHDRAW_LIST = "/api/front/userwithdraw/list"
    const val WITHDRAW_UW_CHECK = "/api/front/userwithdraw/getUwCheck"
    const val PACKET_LIST = "/api/front/user/userPacket/list"

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

    const val MATCH_CATEGORY_RECOMMEND = "/api/front/matchCategory/recommend/query" //查询推薦賽事
    const val MATCH_CATEGORY_SPECIAL_MATCH = "/api/front/matchCategory/special/match/query" //查詢主頁精選賽事
    const val MATCH_CATEGORY_SPECIAL_MENU = "/api/front/matchCategory/special/menu/query" //查詢主頁精選賽事菜单
    const val MATCH_CATEGORY_QUERY = "/api/front/matchCategory/query" //(新)查询参赛表

    //credential
    const val CREDENTIAL_INITIALIZE = "/api/front/realid/initialize"
    const val CREDENTIAL_RESULT = "/api/front/realid/checkresult"

    //timeout
    const val CONNECT_TIMEOUT: Long = 15 * 1000
    const val WRITE_TIMEOUT: Long = 15 * 1000
    const val READ_TIMEOUT: Long = 15 * 1000

    //rule type
    const val COMBO = "combo"

    //news
    const val MESSAGE_LIST = "/api/front/message/list"

    const val RED_ENVELOPE_CHECK = "/api/front/redenp/rain/info"    //10s一次

    const val RED_ENVELOPE_PRIZE_BASE = "/api/front/redenp/rain/grab"
    const val RED_ENVELOPE_PRIZE = "${RED_ENVELOPE_PRIZE_BASE}/{redEnpId}"


    //bettingStation
    const val BETTING_STATION_QUERY = "/api/front/bettingStation/query"
    const val BETTING_STATION_QUERY_INVITE = "/api/front/bettingStation/queryByInvite"
    const val BETTING_STATION_QUERY_BY_BETTING_STATION_ID = "/api/front/bettingStation/queryByBettingStationId"
    const val BETTING_STATION_QUERY_UWSTATION = "/api/front/bettingStation/queryUwStation"
    const val AREA_ALL = "/api/front/area/all"

    //注销账户
    const val CANCEL_ACCOUNT = "/api/front/user/remove"
}