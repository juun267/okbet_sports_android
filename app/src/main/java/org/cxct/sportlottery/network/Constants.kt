package org.cxct.sportlottery.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import org.cxct.sportlottery.util.getCurrentOddsTypeName
import org.cxct.sportlottery.util.isMultipleSitePlat
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Constants {
    val SERVICE_H5_URL_LIST = listOf(
        "https://phuat.abaoooiap.com", "https://www.okbet.com", "https://okbet-v1.cxsport.net"
    )
    val SERVER_URL_LIST = listOf("56wwwkvo.com", "66abnmho.com", "pukckq23.com", "tyiksa89.com")
    var currentServerUrl: String? = null  //當前選擇的的 server url (後續 CheckAppUpdate API 會用到)
    var currentFilename: String? = null //當前選擇的apk name
    private var mBaseUrl = ""
        set(value) {
            field = value
            if (!field.isEmpty()) KvUtils.put("host", value)
        }
        get() {
            if (field.isEmpty()) {
                field = KvUtils.decodeString("host")
            }
            return field
        }
    private var mSocketUrl = ""
        set(value) {
            field = value
            if (!field.isNullOrEmpty()) KvUtils.put("socket_host", value)
        }
        get() {
            return if (field.isNullOrEmpty()) KvUtils.decodeString("socket_host") else field
        }


    fun setBaseUrl(baseUrl: String) {
        mBaseUrl = baseUrl
    }

    fun getBaseUrl(): String {
        return mBaseUrl.httpFormat()
    }

    fun getH5BaseUrl(): String {
        return getBaseUrl()
    }

    fun setSocketUrl(baseUrl: String) {
        mSocketUrl = baseUrl
    }

    fun getSocketUrl(): String {
        return mSocketUrl
    }

    fun getInviteCode(): String {
        return getMetaDataDefValue(MultiLanguagesApplication.appContext, "INVITE_CODE", "")
    }

    /**
     * 获取MetaData信息
     *
     * @param name
     * @param def
     * @return
     */
    fun getMetaDataDefValue(context: Context, name: String, def: String): String {
        val value = getMetaDataValue(context, name)
        return value ?: def
    }

    fun getMetaDataValue(context: Context, name: String): String? {
        var value: Any? = null
        val packageManager: PackageManager = context.packageManager
        val applicationInfo: ApplicationInfo
        try {
            applicationInfo = packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            if (applicationInfo.metaData != null) {
                value = applicationInfo.metaData.get(name)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(
                "Could not read the name in the manifest file.", e
            )
        }
        if (value == null) {
            throw RuntimeException(
                "The name '" + name + "' is not defined in the manifest file's meta data."
            )
        }
        return value.toString()
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

    //优惠活动 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getPromotionUrl(token: String?, language: LanguageManager.Language): String? {
        return try {
            "${getH5BaseUrl()}activity/mobile/#/useractilistV2?lang=${language.key}&token=${
                URLEncoder.encode(
                    token, "utf-8"
                )
            }${
                if (isMultipleSitePlat()) {
                    "&platform=onbet"
                } else {
                    "&platform=okbet"
                }
            }"
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    //優惠活動 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getPromotionDetailUrl(
        token: String?,
        id: Int?,
        language: LanguageManager.Language,
    ): String? {
        return try {
            "${getH5BaseUrl()}activity/mobile/#/useractivityV2/${id}?lang=${language.key}&token=${
                URLEncoder.encode(
                    token, "utf-8"
                )
            }${
                if (isMultipleSitePlat()) {
                    "&platform=onbet"
                } else {
                    "&platform=okbet"
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
            LanguageManager.Language.TH -> "th/"
            LanguageManager.Language.PHI -> "ph/"
            else -> "us/"
        }
    }

    /**
     * 英文为空
     */
    fun getLanguageTag1(context: Context): String {
        return when (getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "zh/"
            LanguageManager.Language.VI -> "vi/"
            LanguageManager.Language.TH -> "th/"
            LanguageManager.Language.PHI -> "ph/"
            else -> ""
        }
    }


    //遊戲規則 url: 須傳入當前 user 登入的 token，獲取 encode token 的 URL
    fun getGameRuleUrl(context: Context): String? {

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/?platform=" + context.getString(
            R.string.app_name
        )
    }

    //關於我們
    fun getAboutUsUrl(context: Context): String? {

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/about-us?platform=" + context.getString(
            R.string.app_name
        )
    }

    //博彩责任
    fun getDutyRuleUrl(context: Context): String? {

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/responsibility?platform=" + context.getString(
            R.string.app_name
        )
    }

    //代理加盟
    fun getAffiliateUrl(context: Context): String {
        return "${getH5BaseUrl()}sports-rule/#/${getLanguageTag(context)}v2/agent-h5?platform=${
            context.getString(
                R.string.app_name
            )
        }"
    }

    //隐私权政策
    fun getPrivacyRuleUrl(context: Context): String? {

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/privacy-policy?platform=" + context.getString(
            R.string.app_name
        )
    }

    //规则与条款
    fun getAgreementRuleUrl(context: Context): String? {

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/terms-conditions?platform=" + context.getString(
            R.string.app_name
        )
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

        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/faq?platform=" + context.getString(
            R.string.app_name
        )
    }

    //联系我们
    fun getContactUrl(context: Context): String? {
        isMultipleSitePlat()
        return getH5BaseUrl() + "sports-rule/#/${getLanguageTag(context)}v2/contact-us?platform=" + context.getString(
            R.string.app_name
        ) + "&service=" + URLEncoder.encode(sConfigData?.customerServiceUrl ?: "", "utf-8")
    }

    //https://okbet-v2.cxsport.net/activity/mobile/#/print?uniqNo=B0d7593ed42d8840ec9a56f5530e09773c&addTime=1681790156872
    //打印小票H5地址
    fun getPrintReceipt(
        context: Context, uniqNo: String?, addTime: String?, reMark: String?
    ): String {
        var language = getLanguageTag(context)
        val base = getH5BaseUrl()
        if (language.contains("/")) {
            language = language.substring(0, language.indexOf("/"))
        } else if (language.isEmpty()) {
            language = "zh"
        }
        return "${base}activity/mobile/#/print?lang=${language}&uniqNo=$uniqNo&addTime=$addTime&reMark=$reMark"
    }


    fun getPrintReceiptScan(url: String): String {
        val base = getH5BaseUrl()
        return if (url.startsWith(base) || SERVICE_H5_URL_LIST.any { url.startsWith(it) }) {
            url
        } else {
            ""
        }

    }

    //抽奖活动H5地址
    fun getLotteryH5Url(context: Context, token: String? = ""): String {
        val language = getLanguageTag(context)
        val base = getH5BaseUrl()
        return base + "sports-rule/#/${language}sweepstakes?platform=${context.getString(R.string.app_name)}&d=android&token=${token}"
    }

    /**
     * 给h5地址加上统一参数
     */
    fun appendParams(url: String?): String? {
        if (url.isNullOrEmpty()) {
            return url
        }
        return url + (if (url.contains("?")) "&" else "?") + "mode=${(if (MultiLanguagesApplication.isNightMode) "night" else "day")}&from=android&version=${BuildConfig.VERSION_NAME}lang=${
            getLanguageTag(MultiLanguagesApplication.appContext)
        }&token=${LoginRepository.token}"
    }

    //獲取檢查APP是否有更新版本的URL //輪詢 SERVER_URL_LIST 成功的那組 serverUrl 用來 download .apk
    fun getCheckAppUpdateUrl(serverUrl: String?): String {
        return "https://download." + serverUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/version-Android" + (if (BuildConfig.FLAVOR != "google") "" else "-${BuildConfig.FLAVOR}") + ".json"
    }

    //.apk 下載 url
    fun getAppDownloadUrl(): String {
        return "https://download." + currentServerUrl + "/sportnative/platform/" + BuildConfig.CHANNEL_NAME + "/${currentFilename}.apk"
    }

    fun getHostListUrl(serverUrl: String?): String {
        return "https://${BuildConfig.CHANNEL_NAME}.$serverUrl/api/front/domainconfig/appdomain/${BuildConfig.CHANNEL_NAME}.json"
    }

    //bet
    const val MATCH_BET_INFO = "/api/front/match/bet/info"
    const val MATCH_BET_REMARK_BET = "/api/front/match/bet/reMarkBet"
    const val MATCH_BET_ADD = "/api/front/match/bet/add"
    const val MATCH_BET_LIST = "/api/front/match/bet/list"

    //index
    const val INDEX_LOGIN = "/api/front/index/loginV2"
    const val INDEX_SEND_LOGIN_DEVICE_SMS = "/api/front/index/sendLoginDeviceSms"
    const val INDEX_VALIDATE_LOGIN_DEVICE_SMS = "/api/front/index/validateLoginDeviceSms"
    const val INDEX_LOGOUT = "/api/front/index/logout"
    const val INDEX_CONFIG = "/api/front/index/config.json" //获取配置信息
    const val INDEX_VALIDATE_CODE = "/api/front/index/getvalidatecode" //获取验证码
    const val INDEX_REGISTER = "/api/front/index/register" //注册用户
    const val INDEX_SEND_SMS = "/api/front/index/sendSms" //发送验证码
    const val INDEX_CHECK_TOKEN = "/api/front/index/checktoken" //验证token 是否过期
    const val LOGIN_FOR_GUEST = "/api/front/index/loginforguest" //游客登录
    const val INDEX_PROMOTENOTICE =
        "/api/front/index/promotenotice" //公告API 未登入不帶token 在投注区查询的时候带[1] 在首页的时候带[2,3]
    const val INDEX_CHECK_EXIST_NEW = "/api/front/index/checkExistNew" //检查账号名称是否已存在

    const val INDEX_SENDCODE = "/api/front/index/sendCode" // 将验证码发送到电子邮件或电话

    const val INDEX_VERIFYORRESET = "/api/front/index/verifyOrResetInfo" // 验证或重置手机号或者邮箱

    const val ACTIVITY_IMAGELIST_H5 = "/api/front/index/activityImageListH5"

    const val ACTIVITY_DETAIL_H5 = "/api/front/agentActivity/detailH5/{activityId}"

    const val ACTIVITY_APPLY = "/api/front/agentActivity/apply/{activityId}"


    //登录或注册(新版优化登录注册->使用)
    const val LOGIN_OR_REG = "/api/front/index/loginOrRegV3"

    //登录或注册获取验证码(新版优化登录注册->使用)
    const val LOGIN_OR_REG_SEND_VALIDCODE = "/api/front/index/loginOrRegSendValidCode"

    // facebook登录或注册(新版优化登录注册->使用)
    const val FACEBOOK_LOGIN = "/api/front/index/facebookLogin"

    // google登录或注册(新版优化登录注册->使用)
    const val GOOGLE_LOGIN = "/api/front/index/googleLogin"

    // 发送邮箱验证码(新版优化登录注册->使用)
    const val SEND_EMAIL_CODE = "/api/front/index/sendEmailCode"

    // 校验邮箱验证码
    const val VALIDATE_EMAIL_CODE = "/api/front/index/validateEmailCode"

    //绑定谷歌登录(绑定现有账户)
    const val BIND_GOOGLE = "/api/front/index/bindGoogle"

    //绑定facebook登录(绑定现有账户)
    const val BIND_FACEBOOK = "/api/front/index/bindFacebook"

    //注册平台账号
    const val REG_PLATFORM_USER = "/api/front/index/regPlatformUser"

    //match
    const val MATCH_LIVE_ROUND = "/api/front/liveRound"

    //match result
    const val MATCH_RESULT_LIST = "/api/front/match/result/list"
    const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

    //odds
    const val MATCH_ODDS_LIST = "/api/front/match/odds/simple/list"
    const val MATCH_ODDS_DETAIL = "/api/front/match/odds/detail"
    const val MATCH_INPLAY_ALL = "/api/front/match/odds/mobile/inplay/all"

    //sport
    const val SPORT_LIST = "/api/front/sport/list"
    const val SPORT_MENU = "/api/front/sport/mobile/menu"
    const val SPORT_SEARCH_ENGINE = "/api/front/sport/searchEngine"
    const val SPORT_PUBLICITY_RECOMMEND = "/api/front/sport/recommend"
    const val SPORT_MENU_FILTER = "/api/front/sport/menu/list"
    const val SPORT_COUPON_MENU = "/api/front/sport/coupon/menu"


    const val MYFAVORITE_QUERY = "/api/front/myFavorite/query"
    const val MYFAVORITE_MATCH_QUERY = "/api/front/myFavorite/match/query"
    const val MYFAVORITE_SAVE = "/api/front/myFavorite/save"
    const val MYFAVORITE_QUERY_ALL = "/api/front/myFavorite/queryAll"

    //outright
    const val OUTRIGHT_ODDS_LIST = "/api/front/outright/odds/list"
    const val OUTRIGHT_RESULT_LIST = "/api/front/outright/result/list"
    const val OUTRIGHT_BET_INFO = "/api/front/outright/bet/info"


    //infoCenter
    const val USER_NOTICE_LIST = "/api/front/user/notice/list"
    const val USER_NOTICE_READED = "/api/front/user/notice/readed/{id}"

    //money
    const val RECHARGE_CONFIG_MAP = "/api/front/rechcfg/map"
    const val USER_RECHARGE_ADD = "/api/front/userrech/add"
    const val USER_RECHARGE_ONLINE_PAY = "/api/front/userrech/onlinepay"
    const val USER_RECHARGE_LIST = "/api/front/userrech/list"
    const val USER_BILL_LIST = "/api/front/sportBill/query"
    const val RECH_CHECK_STATUS = "/api/front/userrech/onlinepayAndroidAndIos"//充值前检查参数状态


    //user
    const val USER_INFO = "/api/front/user/info"
    const val USER_MONEY = "/api/front/user/money"
    const val USER_EDIT_NICKNAME = "/api/front/user/editNickName" //更新昵称
    const val USER_EDIT_ICON_URL = "/api/front/user/editIconUrl" //更新头像
    const val USER_UPDATE_PWD = "/api/front/user/updatepwd" //更新密码
    const val USER_UPDATE_FUND_PWD = "/api/front/user/updatefundpwd" //更新资金密码
    const val USER_WITHDRAW_INFO = "/api/front/user/setWdUserInfo" //設置提款資料
    const val USER_BET_LIMIT = "/api/front/user/setPerBetLimit"
    const val USER_FROZE = "/api/front/user/setFroze"
    const val LOCK_MONEY = "/api/front/user/lockMoney"
    const val PASSWORD_VERIFY = "/api/front/user/passwordVerify"
    const val ODDS_CHANGE_OPTION = "/api/front/user/oddsChangeOption" //设置用户赔率变化
    const val USER_BASIC_INFO_SWITCH = "/api/front/index/getUserBasicInfoSwitch" //是否完善用户信息开关
    const val USER_BASIC_INFO_CHECK = "/api/front/user/checkUserBasicInfoV2" //是否已完善信息
    const val USER_SALARY_SOURCE_LIST = "/api/front/user/querySalarySource" //收入来源列表
    const val USER_BASIC_INFO_UPDATE = "/api/front/user/improveBasicInformationV2" //提交用户基础信息
    const val USER_GET_BASIC_INFO = "/api/front/user/queryUserBasicInfoV2" //获取用户基本信息
    const val USER_VERIFY_CONFIG = "/api/front/user/verifyConfig" //获取需要提交信息配置，为1的必填
    const val UPLOAD_REVIEW_PHOTO = "/api/front/user/uploadReviewPhoto" //上传复审资料
    const val SET_USERNAME = "/api/front/user/fullname" // 修改用户名称

    //upload image
    const val UPLOAD_IMG = "/api/upload/image" //上传图片
    const val UPLOAD_VERIFY_PHOTO = "/api/front/user/uploadVerifyPhoto" //上傳實名制文件

    //簡訊碼驗證
    const val GET_TWO_FACTOR_STATUS =
        "/api/front/user/getTwoFactorValidateStatus" //取得双重验证状态(success: true 验证成功, false 需重新验证手机)
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
    const val NON_THIRD_LOGIN = "/api/front/thirdapi/nonLoginTrial/{firmType}" //未登录试玩

    const val QUERY_FIRST_ORDERS = "/api/front/thirdapi/queryFirstOrders"
    const val QUERY_SECOND_ORDERS = "/api/front/thirdapi/querySecondOrders"


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
    const val MESSAGE_LIST2 = "/api/front/message/list/v2"


    const val RED_ENVELOPE_CHECK = "/api/front/redenp/rain/info"    //10s一次

    const val RED_ENVELOPE_PRIZE_BASE = "/api/front/redenp/rain/grab"
    const val RED_ENVELOPE_PRIZE = "${RED_ENVELOPE_PRIZE_BASE}/{redEnpId}"
    const val REDEEM_CODE = "/api/front/redeemCode/redeem/{redeemCode}"//兑换
    const val REDEEM_CODE_HISTORY = "/api/front/redeemCode/history "//兑换历史


    //bettingStation
    const val BETTING_STATION_QUERY = "/api/front/bettingStation/query"
    const val BETTING_STATION_QUERY_INVITE = "/api/front/bettingStation/queryByInvite"
    const val BETTING_STATION_QUERY_BY_BETTING_STATION_ID =
        "/api/front/bettingStation/queryByBettingStationId"
    const val BETTING_STATION_QUERY_UWSTATION = "/api/front/bettingStation/queryUwStation"
    const val AREA_ALL = "/api/front/area/all"
    const val AREA_UNIVERSAL = "/api/front/area/universal"//国家省市城市
    const val WORKS_QUERYALL = "/api/front/works/queryAll"//获取所有工作性质列表
    const val USER_QUERYUSERINFODETAILS = "/api/front/user/queryUserInfoDetails"//完善用户信息详情查询
    const val USER_COMPLETEUSERDETAILS = "/api/front/user/CompleteUserDetails"//完善用户信息详情

    //注销账户
    const val CANCEL_ACCOUNT = "/api/front/user/remove"

    //忘记密码 发送短信
    const val FORGET_PASSWORD_SMS = "/api/front/index/validateForgotPasswordSMS"
    const val RESET_FORGET_PASSWORD = "/api/front/index/resetForgotPassword" //重设密码
    const val RESET_FORGET_PASSWORD_BY_EMAIL =
        "/api/front/index/resetForgotPasswordByEmail" // 通过邮箱验证后重设密码
    const val SEND_SMS_FORGET = "/api/front/index/sendSmsForgotPasswordV2" //找回密码-获取短信验证码
    const val SEND_EMAIL_FORGET = "/api/front/index/sendEmailCode" //找回密码-获取邮箱验证码
    const val FORGET_PASSWORD_VALIDATE_EMAIL = "/api/front/index/validateEmailCode"//找回密码-验证邮箱验证码

    //全局抽奖活动
    const val LOTTERY_GET = "/api/front/lottery/get"

    // 游戏大厅
    const val OKGAMES_HALL = "/api/front/gameEntryGames/getHallOkGames"

    // jackpot奖池总数
    const val OKGAMES_JACKPOT = "/api/front/gameEntryGames/getFirstGameJackpot"

    // 游戏分页列表
    const val OKGAMES_GAME_LIST = "/api/front/gameEntryGames/getPageOkGames"

    // 收藏或取消OKGames
    const val OKGAMES_COLLECT = "/api/front/gameEntryGames/collectOkGames"

    // okgame 最新投注
    const val OKGAMES_RECORD_NEW = "/api/front/index/recordNewOkGamesList"

    // okgame 最新大奖
    const val OKGAMES_RECORD_RESULT = "/api/front/index/recordResultOkGamesList"

    // oklive 最新投注
    const val OKLIVE_RECORD_NEW = "/api/front/index/recordNewOkLiveList"

    // oklive 最新大奖
    const val OKLIVE_RECORD_RESULT = "/api/front/index/recordResultOkLiveList"

    // 首页最新投注
    const val RECORD_NEW = "/api/front/index/recordNewList"

    // 首页最新大奖
    const val RECORD_RESULT = "/api/front/index/recordResultList"

    //首页资讯列表
    const val NEWS_LIST_HOME = "/front/content/getListHome"

    //推荐列表不带Contents
    const val NEWS_LIST_RECOMMEND = "/front/content/getListRecommend"

    //新闻列表不带Contents
    const val NEWS_LIST_PAGE = "/front/content/getPage"

    //资讯详情
    const val NEWS_DETIAL = "/front/content/getOne"

    // 安卓送审版本号
    const val GET_CONFIG_BY_NAME = "/api/agent/game/config/getConfigByName/{name}"

    //chat
    const val ROOM_QUERY_LIST =
        "/api/chat/front/room/queryList" //------------------------------- 查询所有开放的房间
    const val PACKET_LUCKY_BAG =
        "/api/chat/front/packet/luckyBag" //----------------------------- 抢红包
    const val PACKET_GET_UNPACKET =
        "/api/chat/front/packet/{roomId}/getUnPacket" //-------------- 获得未抢红包列表
    const val CHAT_INIT =
        "/api/chat/front/chat/init" //------------------------------------------ 初始化聊天室用户(游客,一般用户)
    const val CHAT_GUEST_INIT =
        "/api/chat/front/chat/guest/init" //------------------------------ 初始化聊天室用户(访客)
    const val CHAT_JOIN_ROOM =
        "/api/chat/front/chat/{roomId}/joinRoom" //------------------------ 用户进入聊天室
    const val CHAT_LEAVE_ROOM =
        "/api/chat/front/chat/{roomId}/leaveRoom" //---------------------- 用户离开聊天室
    const val CHAT_REMOVE_MESSAGE =
        "/api/chat/front/chat/{roomId}/removeMessage/{messageId}" //-- 删除讯息
    const val CHAT_GET_SIGN =
        "/api/front/chat/getNewSign" //---------------------------------------- 获取平台用户信息和签名信息
    const val CHAT_CHECK_TOKEN =
        "/api/chat/front/user/checktoken" //----------------------------- 验证token 是否过期。如果不过期返回token信息，过期返回success: false
    const val CHAT_GET_STICKER="/api/chat/front/emoticons/queryList"
}