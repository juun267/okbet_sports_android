package org.cxct.sportlottery.network.index.config

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.repository.StaticData

@JsonClass(generateAdapter = true)
@Keep
data class ConfigData(
    val platformId: Long?,
//    val agentMode: String?, //代理模式：ulimit 无限级，fixed 固定级
//    val agentUrl: String?, //代理地址
//    val agentZxkfUrl: String?, //代理专属客服地址
//    val appDownUrl: String?, //电脑版APP下载地址
//    val cancelOrder: String?, //是否开启撤单功能(1：开启，0：关闭)
//    val chatAutoConnect: String?, //是否自动打开聊天室（0：不自动打开；1：自动打开）
    val chatHost: String?, //聊天室地址
//    val chatMinBetMoney: String?, //聊天室推送的最低下注金额 备注：排行榜关闭时才生效;字段命名和旧平台保持一致
    val chatOpen: String?, //聊天室开关
    val customerServiceUrl: String?, //平台在线客服url
    val customerServiceUrl2: String?, //平台在线客服url2
//    val defaultSkin: String?, //默认皮肤
//    val enableEmail: String?, //是否开启Email(1：开启，0：关闭)
//    val enableFacebook: String?, //是否开启Facebook(1：开启，0：关闭)
//    val enableFullName: String?, //是否开启真实姓名检测(1：开启，0：关闭)
//    val enableFundPwd: String?, //是否开启取款密码(1：开启，0：关闭)
//    val enableInviteCode: String?, //是否开启邀请码注册(1：开启，0：关闭)
    val enableModifyBank: String?, //是否允许编辑银行卡， 0 禁止 1允许
//    val enablePhone: String?, //是否开启手机号(1：开启，0：关闭)
//    val enableQQ: String?, //是否开启QQ(1：开启，0：关闭)
//    val enableRegValidCode: String?, //是否开启注册验证码(1：开启，0：关闭)
//    val enableSmsValidCode: String?, //是否开启短信验证码(1：开启，0：关闭)
//    val enableTelegram: String?, //是否开启Telegram(1：开启，0：关闭)
//    val enableValidCode: String?, //是否开启登录验证码(1：开启，0：关闭)
//    val enableWechat: String?, //是否开启微信检测(1：开启，0：关闭)
//    val enableAddress: String?, //是否开启address(1：开启，0：关闭)
//    val enableSafeQuestion: String?, //是否开启safeQuestion(1：开启，0：关闭)
//    val enableWhatsApp: String?, //是否开启WhatsApp(1：开启，0：关闭)
    val enableWithdrawEmail: String?, //提现时Email不能为空
    val enableWithdrawFullName: String?, //提现时真实姓名不能为空
//    val enableWithdrawFundPwd: String?, //提现时提现密码不能为空
    val enableWithdrawPhone: String?, //提现时手机号不能为空
    val enableWithdrawQQ: String?, //提现时QQ不能为空
    val enableWithdrawWechat: String?, //提现时微信不能为空
//    val enableXinDaiBa: String?, //是否开启信贷吧（1-开启，0-关闭）
//    val enableYuEBao: String?, //是否开启余额宝（1-开启，0-关闭）
//    val enableZalo: String?, //是否开启Zalo(1：开启，0：关闭)
//    val exclusiveAgentUrl: String?, //代理专属域名
//    val extendJs: String?, //统计代码
//    val facebook: String?,
//    val facebookLink: String?,
//    val facebookQR: String?,
//    val flyOpen: String?, //飞单是否开启 0:关闭,1-开启,默认0为关闭
    val imageList: List<ImageData>?, //图片列表
//    val lhc: Lhc?, //六合彩
//    val lotteryLiveUrl: String?, //开奖直播地址
//    val mainAgentQQ: String?, //推广页面代理QQ
//    val mainCustomerQQ: String?, //推广页面客服QQ
//    val mainEmail: String?, //推广页面代理Email
//    val mainPhone: String?, //推广页面联系电话
//    val mainQQUrl: String?, //推广面QQ连接
//    val mainWxUrl: String?, //推广面微信连接
    val maintainInfo: String?, //维护页面描述
    val maintainStatus: String?, //系统维护开关 (1：开启，0：关闭)
    var sportMaintainStatus: String?, //体育服务维护开关 (1：开启，0：关闭)
    val minRechMoney: String?, //最低充值金额限制
    val mobileAppDownUrl: String?, //手机版APP下载地址
//    val navigationUrl: String?, //navigationUrl导航地址
//    val opStatus: Long?, //运营状态 0：非直营，1：直营，2：外部接入
//    val rankingOpen: String?, //排行榜开关
//    val rankingRewardAmount: String?, //聊天室的最高打赏金额
//    val rebateRatio: String?, //返点比率
    val resServerHost: String?, //静态资源服务器地址
    val serverTime: Long?, //服务器时间
//    val smsValidTime: String?, //短信验证码有效时间（分钟）
//    val stopBetTime: String?, //所有游戏的早上开盘时间
//    val stopBetTime2: String?, //所有游戏的次日封盘时间
//    val telegram: String?,
//    val telegramLink: String?,
//    val telegramQR: String?,
    val thirdOpen: String?, //第三方游戏开关
    val thirdTransferOpen: String?, //第三方自动转账开关
    val thirdTransferUnit: Double?, //第三方額度
//    val weixinQR: String?,
//    val whatsApp: String?,
//    val whatsAppLink: String?,
//    val whatsAppQR: String?,
//    val withDrawBalanceLimit: String?, //最低提现金额限制
//    val zalo: String?,
//    val zaloLink: String?,
//    val zaloQR: String?,
    val sportAnimation: String?,//体育动画接口 20210812確認暫時無用處
    val sportStream: String?,//体育视频域名
    val liveUrl: String?,
//    val analysisUrl: String?,
//    val referUrl: String?,
    val enableMinRemainingBalance: String?,//是否启用账户首次提现最小剩余额度(1：开启，0：关闭)
    val minRemainingBalance: String?,//账户首次提现最小剩余额度
    val presetBetAmount: List<Int>? = listOf(0, 0, 0, 0), //前台预设下注金额配置
    val systemCurrencySign: String?,
    val systemCurrency: String? = "PHP",
//    val systemUSDTCurrency: String? = "USDT",
    val realNameRechargeVerified: String? = null,//充值实名制验证开关 (1：开启，0：关闭， 默认是null:关闭)
    val realNameWithdrawVerified: String? = null,//提现实名制验证开关 (1：开启，0：关闭， 默认是null:关闭)
    val perBetMaxAmount: String?,
    val perBetMinAmount: String?,
    val selfRestraintVerified: String?,
//    val customerFloating: String? = "1",//在线客服悬浮按钮开关 (1：开启，0：关闭 默认是null:关闭)
//    val enableKYCVerify: String? = "1", //KYC认证开关(1：开启，0：关闭)
    val supportLanguage: String = "",
    val wsHost: String = "",
    val creditSystem: Int? = null,
    val firstRechLessAmountLimit: String?,//首存金额限制
    var idUploadNumber: String?,//KYC認證個數
//    var customerServicveVideoUrl: String?,
//    val enableBirthday: String?, //是否开启Birthday(1：开启，0：关闭)
//    val enableSalarySource: String?, //是否开启SalarySource(1：开启，0：关闭)
//    val enableIdentityNumber: String?, //是否开启IdentityNumber(1：开启，0：关闭)
//    val enableBettingStation: String?, //是否开启BettingStation(1：开启，0：关闭)
//    var salarySource: List<SalarySource>?, //薪资来源列表
    var identityTypeList: List<IdentityType>?, //身分证件列表
//    var safeQuestionList: List<SafeQuestion>?, //安全问题选项列表
//    val enableNationCurrency: String?, //国家与币种是否需填(1: 是, 0: 否, 默认是null:关闭)
    val handicapShow: String?, //前端展示的盘口（EU,HK,MY,ID）後端沒配置或為空的狀況下，要顯示預設的四個盤口
//    val nationCurrencyList: List<NationCurrency>?, //国家币种列表
//    val liveChatHost: String?,
//    var liveChatOpen: Int?,
//    var liveCount: Int?,//直播总数
    val enableLockBalance: String?,//个人中心押金后台配置0或者null隐藏1显示
//    val uwEnableTime: String?,//锁定额度限制解锁时间
    var minFrozeDay: Int = 0,//最小限制天数
    val enableEmailReg: String?, //是否开启邮箱注册
    val frontEntranceStatus: String?,// 代码加盟入口状态(1：开启，0：关闭)
    var androidCarouselStatus: String?,//是否开启安卓轮播状态(1：开启，0：关闭)
    var carouselInterval: String?,//自动轮播的间隔秒数
    var cmsUrl: String?,//新闻域名
    var reviewedVersionUrl: String?,//送审中的版本号
    var facebookLinkConfig:String?,//fb连接配置
    var instagramLinkConfig:String?,//ins连接配置
    var youtubeLinkConfig:String?,//youtube连接配置
    var twitterLinkConfig:String?,//twitter连接配置
    var tiktokLinkConfig:String?,//tiktok连接配置
    var telegramLinkConfig : String?,//telegram连接配置
    var viberLinkConfig : String?,//viber连接配置
    var whatsappLinkConfig : String?,//whatapp连接配置
    var selectedDepositAmountSettingList: List<Int>? = null, //存款快捷金额
    var auditFailureRestrictsWithdrawalsSwitch:Int?,//提款额度大于打码量的时候直接拒绝提款
    var noLoginWitchVideoOrAnimation: Int? ,//未登陆观看视频/动画 0-关闭 1-启用
    var homeGamesList:List<HomeGameBean>?,  //首页场馆排序
    var jackpotSwitch:Int=0,  //okgame 奖池 0-关闭 1-开启
    val isNeedOTPBank: Int = 0, // 添加删除编辑 银行卡时 是否需要验证码 0-否 1-是
    val customerServiceEmailAddress: String?, // 客服邮箱
    val firstPhoneGiveMoney: Int = 0,// 注册绑定手机送金额
    val enableRetrieveWithdrawPassword: String?, // 是否开启找回提款密码(0-关｜1-开)
    val identityTabTypeList: List<IdentityType>?,
    val idScanHost: String?,
    val captchaType: Int=0,//1的时候需要使用滑动图形验证码
    val captchaAppId: String?,
    val bkFinalScoreNewGameplaySwitch:Int=0,//篮球末尾比分新玩法 0-关，1-开
    val registerTermsDefaultCheckedSwitch: Int=1,//注册条款是否默认为勾选 1 是 0 否
    val kycsupplierSelectionValue:Int=1,//KYC调用前需要判断此处值 1-华为｜2-腾讯
    val sbSportSwitch:Int=0,//沙巴体育开关 0-关，1-开
    val servicePhoneNationNumber: String?=null, //客服专线国家码
    val servicePhoneNumber: String?=null, //客服专线
    val vipSwitch: Int=0,//是否开启用户VIP功能 1-开启 2-关闭
    val ageVerificationChecked:Int=1,//是否默认勾选21岁 0-关闭 1-打开
    val gameUserDepositURL: String? = null,//三方游戏内充值跳转链接
    val inviteUserStatus: Int=0,//是否开邀请好友活动 1-开启 0-关闭
    val glifeMemberRechargeAndWithdrawal: Int = 0, // glife用户可以在平台直接充值
    val mayaMemberRechargeAndWithdrawal:Int = 0,// maya用户可以在平台直接充值
) {
    var hasGetTwoFactorResult: Boolean? = false //判斷是不是已經成功發送過簡訊認證碼 (關掉彈窗要重新設置為false)
}

