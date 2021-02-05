package org.cxct.sportlottery.network

import android.content.Context
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Constants {
    val SERVER_URL_LIST = listOf("app66app.com", "app99app.vip", "app66app.vip", "app88app.vip")
    const val BASE_URL = "https://sports.cxct.org"

    //優惠活動 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getPromotionUrl(token: String?): String? {
        return try {
            BASE_URL + "/activity/mobile.html?token=" + URLEncoder.encode(token, "utf-8")
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

    var currentServerUrl: String? = null  //當前選擇的的 server url (後續 CheckAppUpdate API 會用到)

    //獲取檢查APP是否有更新版本的URL //輪詢 SERVER_URL_LIST 成功的那組 serverUrl 用來 download .apk
    fun getCheckAppUpdateUrl(serverUrl: String?): String {
        return "https://download." + serverUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/version-Android.json"
    }

    //.apk 下載 url
    fun getAppDownloadUrl(): String {
        return "https://download." + currentServerUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/cp.apk";
    }

    //bet
    const val MATCH_BET_INFO = "/api/front/match/bet/info"
    const val MATCH_BET_ADD = "/api/front/match/bet/add"
    const val MATCH_BET_LIST = "/api/front/match/bet/list"

    //index
    const val INDEX_LOGIN = "/api/front/index/login"
    const val INDEX_LOGOUT = "/api/front/index/logout"
    const val INDEX_CONFIG = "/api/front/index/config.json" //获取配置信息
    const val INDEX_VALIDATE_CODE = "/api/front/index/getvalidatecode" //获取验证码
    const val INDEX_REGISTER = "api/front/index/register" //注册用户
    const val INDEX_SEND_SMS = "/api/front/index/sendSms" //发送验证码
    const val INDEX_CHECK_EXIST = "/api/front/index/checkexist/{userName}" //检查账号名称是否已存在
    const val INDEX_CHECK_TOKEN = "/api/front/index/checktoken" //验证token 是否过期

    //league
    const val LEAGUE_LIST = "/api/front/match/league/list"

    //match
    const val MATCH_PRELOAD = "/api/front/match/preload"

    //match result
    const val MATCH_RESULT_LIST = "/api/front/match/result/list"
    const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

    //message
    const val MESSAGE_LIST = "/api/front/message/list"

    //odds
    const val MATCH_ODDS_LIST = "/api/front/match/odds/list"
    const val MATCH_ODDS_DETAIL = "/api/front/match/odds/detail"

    //sport
    const val SPORT_MENU = "/api/front/sport/mobile/menu"

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

    //upload image
    const val UPLOAD_IMG = "/api/upload/image" //上传图片

    //bank
    const val BANK_MY = "/api/front/user/bank/my"
    const val BANK_ADD = "/api/front/user/bank/add"
    const val BANK_DELETE = "/api/front/user/bank/delete"

    //withdraw
    const val WITHDRAW_ADD = "/api/front/userwithdraw/add"
    const val WITHDRAW_LIST = "/api/front/userwithdraw/list"

    //third game
    const val GET_ALL_BALANCE = "/api/front/thirdapi/getAllBalance"
    const val THIRD_ALL_TRANSFER_OUT = "/api/front/thirdapi/allTransferOut"

    //timeout
    const val CONNECT_TIMEOUT: Long = 15 * 1000
    const val WRITE_TIMEOUT: Long = 15 * 1000
    const val READ_TIMEOUT: Long = 15 * 1000

}