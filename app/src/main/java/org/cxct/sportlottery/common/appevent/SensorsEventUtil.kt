package org.cxct.sportlottery.common.appevent

import android.content.Context
import com.sensorsdata.analytics.android.sdk.SAConfigOptions
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.sensorsdata.analytics.android.sdk.SensorsDataDynamicSuperProperties
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.uploadImg.ImgData
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.LanguageManager
import org.json.JSONObject


object SensorsEventUtil {

    private const val SA_SERVER_URL = "https://data.catokbet.com/sa?project=default";

    // 需要在主线程初始化神策 SDK
    fun initSdk(context: Context) {
        val options = SAConfigOptions(SA_SERVER_URL)
        /**
         * 集成神策 Web JS SDK 的 H5 页面，在嵌入到 App 后，H5 内的事件可以通过 App 进行发送，
         * 事件发送前会添加上 App 采集到的预置属性。该功能默认是关闭状态，如果需要开启，需要在 App 和 H5 端同时进行配置。
         */
        options.enableJavaScriptBridge(true)
        SensorsDataAPI.startWithConfigOptions(context, options)

        val properties = JSONObject()
            .put("platform_type", "Android")
            .put("device_id", Constants.deviceSn)
        SensorsDataAPI.sharedInstance().registerSuperProperties(properties)
        SensorsDataAPI.sharedInstance().registerDynamicSuperProperties(
            object : SensorsDataDynamicSuperProperties {
                override fun getDynamicSuperProperties(): JSONObject {
                    val params = JSONObject()
                        .put("client_language", LanguageManager.getLanguageString())
                    val userInfo = UserInfoRepository.loginedInfo() ?: return params
                    userInfo.levelCode?.let { params.put("vip_level", userInfo.levelCode) }
                    return params
                        .put("member_id", userInfo.userId.toString())
                        .apply { userInfo.verified?.let { put("KYC_status", it) } }

                }
            })
    }

    private fun pushEvent(eventName: String, event: JSONObject?) {
        var properties: JSONObject = event ?: JSONObject()
        properties.put("eventname", eventName)
        SensorsDataAPI.sharedInstance().track(eventName, properties)
    }

    fun getPageName(): String {
        val activity = AppManager.currentActivity()
        return if (activity is BaseActivity<*, *>) {
            activity.pageName()
        } else {
            "未知页面"
        }
    }

    /**
     * 进入登录注册页面
     * "登录页面加载完成后上报（可在此步骤添加调用用户关联的logout步骤，防止出现相同设备登录不同账号发生的自关联情况）"
     * source_channel	来源渠道	STRING	本次进入登录注册页面是来源什么渠道()
     * page_name	    页面名称	STRING	唤出登录注册界面的页面名称
     */
    fun loginPageEvent() {
        val event = JSONObject()
        event.put("source_channel", "Android")
        event.put("page_name", getPageName())
        pushEvent("visitLoginPage", event)
    }

    /**
     * 获取验证码结果,有获取验证码结果时触发
     * veri_code_type	验证码类型	STRING	手机验证码、邮箱验证码
     * is_success	    是否成功	    BOOL	1成功 0失败
     * fail_reason	    失败原因	    STRING	返回什么原因就上报什么原因
     */
    fun getCodeEvent(status: Boolean, isEmailCode: Boolean = false, errorMsg: String? = null) {
//        val event = JSONObject()
//        if (isEmailCode) {
//            event.put("veri_code_type", "邮箱验证码")
//        } else {
//            event.put("veri_code_type", "手机验证码")
//        }
//        event.put("is_success", status)
//        errorMsg?.let { event.put("fail_reason", errorMsg) }
//        pushEvent("loginGetCodeResult", event)
    }

    /**
     * 提交注册
     * "手机/邮箱注册：点击提交注册按钮后上报
     * 第三方注册：点击创建账户中的"创建"按钮后上报(第三方登录后，进入网站统一要求创建账户)
     * 第三方包含：Google/Facebook"
     * reg_channel	    注册方式	STRING	账号、邮箱、手机号、Google...等
     * source_channel	来源渠道	STRING	本次提交注册页面前，用户是来源什么渠道
     */
    fun registerEvent(registerWays: String) {
        val event = JSONObject()
        event.put("reg_channel", registerWays)
        event.put("source_channel", "Android")
        pushEvent("submitRegister", event)
    }

    /**
     * 提交KYC请求
     * 提交KYC请求时上报
     */
    fun submitKYCEvent() {
        pushEvent("submitKYC", null)
    }

    /**
     * 存款页面访问
     * "进入存款页面时上报（页面标题、地址、路径如果SDK可以默认获取可以不用单独采集）"
     * title_type	页面类型	STRING	存款页面
     * title	页面标题	STRING	存款
     * url	页面地址	STRING	所在的页面地址
     * url_path	页面地址路径	STRING	所在的页面地址路径
     * visit_source_type	来源类型	STRING	从什么地方进入该页面，例如：游戏大厅、弹窗引导...
     * visit_source_name	来源名称	STRING	例如：机台名称、来源的页面名称等
     */
    fun depositPageEvent(sourceType: String) {
        val params = JSONObject()
        params.put("title_type", "存款页面")
        params.put("title", "存款")
        params.put("url", "")                         // 所在的页面地址
        params.put("url_path", "")                    // 所在的页面地址路径
        params.put("visit_source_type", sourceType)         // 来源类型
        params.put("visit_source_name", getPageName())         // 例如：机台名称、来源的页面名称等
        pushEvent("depositPageView", params)
    }

//    /**
//     * 选择支付账户
//     * "不同支付账户被选中时上报
//     * （页面标题、地址、路径如果SDK可以默认获取可以不用单独采集）"
//     * payment_account	    支付账户	        STRING	所选的支付账户方式(GCash/GrabPay/Maya……)
//     * title_type	        页面类型	        STRING	存款页面
//     * title	            页面标题	        STRING	存款
//     * url	                页面地址	        STRING	所在的页面地址
//     * url_path	            页面地址路径	    STRING	所在的页面地址路径
//     * visit_source_type	来源类型	        STRING	从什么地方进入该页面，例如：游戏大厅、弹窗引导...
//     * visit_source_name	来源名称	        STRING	例如：机台名称、来源的页面名称等
//     */
//    fun selectPaymentEvent(paymentAccount: String, sourceType: String, sourceName: String) {
//        val params = JSONObject()
//        params.put("payment_account", paymentAccount)
//        params.put("title_type", "存款页面")
//        params.put("title", "存款")
////        params.put("url", "")
////        params.put("url_path", "")
//        params.put("visit_source_type", sourceType)          // 来源类型
//        params.put("visit_source_name", sourceName)          // 例如：机台名称、来源的页面名称等
//        pushEvent("selectPaymentAccount", params)
//
//    }

    /**
     * 点击游戏
     * 点击游戏按钮时上报(游戏图标)
     * cp_name	        游戏供应商(厅方名称)	STRING	提供游戏玩法的供应商名称
     * machine_type	    游戏分类	            STRING	游戏隶属于哪种玩法分类，okgames、sports、oklive、esports
     * machine_name	    游戏名称	            STRING	游戏本身的名称
     * machine_id	    游戏ID	            STRING	游戏的唯一ID
     * click_place	    点击位置	            STRING	通过什么页面或位置点击的，若不需知道点击来源，可删除此属性
     * is_trial_machine	是否试玩游戏	        BOOL	1是0否
     */
    fun gameClickEvent(clickPlace: String, frimName: String, gameType: String, gameName: String, gameId: String) {
        val params = JSONObject()
        params.put("cp_name", frimName)
        params.put("machine_type", gameType)
        params.put("machine_name", gameName)
        params.put("machine_id", gameId)
        params.put("click_place", clickPlace)
//        params.put("is_trial_machine", "")
        pushEvent("clickMachine", params)
    }

    /**
     * 开始加载游戏
     * 开始加载游戏时上报，一些玩法没有加载过程不上报
     * cp_name	            游戏供应商(厅方名称)	STRING	提供游戏玩法的供应商名称
     * machine_type	        游戏分类	            STRING	游戏隶属于哪种玩法分类，okgames、sports、oklive、esports
     * machine_name	        游戏名称	            STRING	游戏本身的名称
     * machine_id	        游戏ID	            STRING	游戏的唯一ID
     * is_trial_machine	    是否试玩游戏	        BOOL	1是0否
     */
    fun startLoadingGame(firmName: String,
                         gameType: String,
                         gameName: String,
                         gameId: String,
                         isTrialPlay: Boolean = false) {
//        val params = JSONObject()
//        params.put("cp_name", firmName)
//        params.put("machine_type", gameType)
//        params.put("machine_name", gameName)
//        params.put("machine_id", gameId)
////        params.put("is_trial_machine", isTrialPlay)
//        pushEvent("startLoadingMachine", params)
    }

    /**
     * 游戏加载结果
     * "产生游戏加载结果时上报计算游戏稳定性指标用"
     * cp_name	            游戏供应商(厅方名称)	STRING	提供游戏玩法的供应商名称
     * machine_type	        游戏分类	            STRING	游戏隶属于哪种玩法分类
     * machine_name	        游戏名称	            STRING	游戏本身的名称
     * machine_id	        游戏ID	            STRING	游戏的唯一ID
     * click_place	        点击位置	            STRING	通过什么页面或位置点击的，若不需知道点击来源，可删除此属性
     * is_trial_machine 	是否试玩游戏	        BOOL	1是0否
     * loading_result	    加载结果	            STRING	完成,失败
     * fail_reason	        失败原因	            STRING	"记录加载失败的原因或Error Code若报的是不可读的Error Code，可通过上传维度字典将Error Code映射为原因）"
     * loading_duration	    加载时长	            NUMBER	加载开始到完成所花费的时长，单位为秒
     */
    fun onGameLoadingResult(firmName: String,
                            gameType: String,
                            gameName: String,
                            gameId: String,
                            loadingDuration: Int,
                            isTrialPlay: Boolean = false,
                            loadingResult: Boolean,
                            failReason: String? = null) {
        val params = JSONObject()
        params.put("cp_name", firmName)
        params.put("machine_type", gameType)
        params.put("machine_name", gameName)
        params.put("machine_id", gameId)
        params.put("is_trial_machine", isTrialPlay)
        params.put("loadingDuration", loadingDuration)
        params.put("loadingResult", loadingResult)
        params.put("failReason", failReason)
        pushEvent("machineLoadingResult", params)
    }

    fun popupWindowClickEventWithImageData(imgData: ImageData) {
        popupWindowClickEvent(
            imgData.id.toStringS("0"),
            imgData.imageText1 ?: "",
            imgData.imageText1 ?: "",
            imgData.appUrl ?:""
        )
    }

    /**
     * 弹窗点击
     * 点击页面内弹时上报
     * popup_id	        弹窗ID	    STRING	弹窗的id
     * popup_type	    弹窗类型	    STRING	弹窗的类型，活动弹窗，促销弹窗
     * popup_name	    弹窗名称	    STRING	弹窗的名称
     * pop_up_page	    弹窗所在页面	STRING	弹窗所在的页面，首页，活动页等
     * hyper_page_type	跳转页面类型	STRING	跳转的页面类型（通过LINK来判断）
     * hyper_page_title	跳转页面标题	STRING	跳转页面的页面标题
     * hyper_page_url	跳转页面url	STRING	跳转页面的地址
     */
    private fun popupWindowClickEvent(popupId: String,
//                              popupType: String,
                              popupName: String,
//                              popupPage: String,
//                              hyperPageType: String,
                              hyperPageTitle: String,
                              hyperPageUrl: String,) {
        val params = JSONObject()
        params.put("popup_id", popupId)
        params.put("popup_type", "活动弹窗")
        params.put("popup_name", popupName)
        params.put("pop_up_page", getPageName())
//        params.put("hyper_page_type", hyperPageType)
        params.put("hyper_page_title", hyperPageTitle)
        params.put("hyper_page_url", hyperPageUrl)
        pushEvent("popupClick", params)
    }

    /**
     * Banner点击
     * 点击Banner时上报
     * banner_page	            banner所在页面	    STRING	首页/优惠列表
     * banner_position_number	banner所在位置序号	NUMBER	由左至右、由上至下从1开始赋予序号
     * hyper_page_type	        跳转页面类型	        STRING	跳转的页面类型（通过LINK来判断）
     * hyper_page_title	        跳转页面标题	        STRING	跳转页面的页面标题
     * hyper_page_url	        跳转页面url	        STRING	跳转页面的地址
     */
    fun bannerClickEvent(bannerPage: String,
                         bannerPositionNumber: Int,
//                         hyperPageType: String,
                         hyperPageTitle: String,
                         hyperPageUrl: String) {
        val params = JSONObject()
        params.put("banner_page", bannerPage)
        params.put("banner_position_number", bannerPositionNumber)
//        params.put("hyper_page_type", hyperPageType)
        params.put("hyper_page_title", hyperPageTitle)
        params.put("hyper_page_url", hyperPageUrl)
        pushEvent("bannerClick", params)
    }

    /**
     * 活动页面浏览
     * 进入活动页面时上报
     * visit_source_type	来源类型	    STRING	从什么地方进入该页面，例如：游戏大厅、弹窗引导...
     * visit_source_name	来源名称	    STRING	例如：机台名称、来源的页面名称等
     * event_type	        活动类型	    STRING	活动的类型，常驻活动，促销活动
     * event_name	        活动名称	    STRING	活动的名称
     * event_stage_name	    活动档位名称	STRING	各个活动档位对应的名称
     * event_stage_id	    活动档位ID	STRING	各个活动档位对应的独立ID
     * page_name	        页面名称	    STRING	活动页面的名称
     */
    fun activityPageVisitEvent(visitSourceType: String,
                       visitSourceName: String,
                       eventType: String,
                       eventName: String,
                       /*eventStageName: String,
                       eventStageId: String*/) {
        val params = JSONObject()
        params.put("visit_source_type", visitSourceType)
        params.put("visit_source_name", visitSourceName)
        params.put("event_type", eventType)
        params.put("event_name", eventName)
//        params.put("event_stage_name", eventStageName)
//        params.put("event_stage_id", eventStageId)
        params.put("page_name", getPageName())
        pushEvent("eventPageView", params)
    }

    private fun activityEventProperties(eventType: String,
                                        eventName: String,
                                        /*eventStageName: String,
                                        eventStageId: String*/): JSONObject {
        return JSONObject()
            .put("event_type", eventType)
            .put("event_name", eventName)
//            .put("event_stage_name", eventStageName)
//            .put("event_stage_id", eventStageId)
    }

//    /**
//     * 报名活动任务
//     * 主动报名活动成功时上报
//     * event_type	    活动类型	    STRING	活动的类型
//     * event_name	    活动名称	    STRING	活动的名称
//     * event_stage_name	活动档位名称	STRING	各个活动档位对应的名称
//     * event_stage_id	活动档位ID	STRING	各个活动档位对应的独立ID
//     */
//    private fun activityRegisterEvent(eventType: String,
//                              eventName: String,
//                              /*eventStageName: String,
//                              eventStageId: String*/) {
//        pushEvent("registerEvent", activityEventProperties(eventType, eventName/*, eventStageName, eventStageId*/))
//    }

    /**
     * 活动签到完成
     * 完成活动签到时上报
     * event_type	    活动类型	    STRING	活动的类型
     * event_name	    活动名称	    STRING	活动的名称
     * event_stage_name	活动档位名称	STRING	各个活动档位对应的名称
     * event_stage_id	活动档位ID	STRING	各个活动档位对应的独立ID
     */
    fun activitySignInEvent(eventType: String,
                            eventName: String,
                            eventStageName: String,
                            eventStageId: String) {
        pushEvent("eventSignIn", activityEventProperties(eventType, eventName/*, eventStageName, eventStageId*/))
    }

    /**
     * 新闻页面浏览
     * 浏览新闻页面时上报
     * news_id	    新闻id	    STRING	新闻的id
     * news_name	新闻标题	    STRING	新闻的标题
     * title_type	页面类型	    STRING	新闻页面
     * title	    页面标题	    STRING	页面的标题
     * url	        页面地址	    STRING	所在的页面地址
     * url_path	    页面地址路径	STRING	所在的页面地址路径
     */
    fun newsPageViewEvent(newsId: String,
                          newsName: String,
//                          titleType: String,
                          /*title: String*/) {
        val params = JSONObject()
        params.put("news_id", newsId)
        params.put("news_name", newsName)
        params.put("title_type", "新闻页面")
        params.put("title", newsName)
//        params.put("url", url)
//        params.put("url_path", url_path)
        pushEvent("newsPageView", params)
    }

    /**
     * 分享点击 点击分享按钮时上报
     * title_type	    页面类型	    STRING	分享所在的页面
     * title	        页面标题	    STRING	页面的标题
     * url	            页面地址	    STRING	所在的页面地址
     * url_path	        页面地址路径	STRING	所在的页面地址路径
     */
    fun shareClickEvent(shareTo: String) {
        val params = JSONObject()
        params.put("title_type", "邀请好友")
        params.put("title", shareTo)
//        params.put("url", url)
//        params.put("url_path", url_path)
        pushEvent("shareClick", params)
    }


    /**
     * event_type	    活动类型	    STRING	活动的类型
     * event_name	    活动名称	    STRING	活动的名称
     * event_stage_name	活动档位名称	STRING	各个活动档位对应的名称
     * event_stage_id	活动档位ID	STRING	各个活动档位对应的独立ID
     */
    fun activitySignInEvent(eventType: String, eventName: String) {
        val params = JSONObject()
        params.put("event_type", eventType)
        params.put("event_name", eventName)
//        params.put("url", url)
//        params.put("url_path", url_path)
        pushEvent("shareClick", params)
    }


}