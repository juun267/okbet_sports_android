package org.cxct.sportlottery.network.index.config

data class ConfigData(
    val platformId: Long?,
    val agentMode: String?, //代理模式：ulimit 无限级，fixed 固定级
    val agentUrl: String?, //代理地址
    val agentZxkfUrl: String?, //代理专属客服地址
    val appDownUrl: String?, //电脑版APP下载地址
    val cancelOrder: String?, //是否开启撤单功能(1：开启，0：关闭)
    val chatAutoConnect: String?, //是否自动打开聊天室（0：不自动打开；1：自动打开）
    val chatHost: String?, //聊天室地址
    val chatMinBetMoney: String?, //聊天室推送的最低下注金额 备注：排行榜关闭时才生效;字段命名和旧平台保持一致
    val chatOpen: String?, //聊天室开关
    val customerServiceUrl: String?, //平台在线客服url
    val customerServiceUrl2: String?, //平台在线客服url2
    val defaultSkin: String?, //默认皮肤
    val enableEmail: String?, //是否开启Email(1：开启，0：关闭)
    val enableFacebook: String?, //是否开启Facebook(1：开启，0：关闭)
    val enableFullName: String?, //是否开启真实姓名检测(1：开启，0：关闭)
    val enableFundPwd: String?, //是否开启取款密码(1：开启，0：关闭)
    val enableInviteCode: String?, //是否开启邀请码注册(1：开启，0：关闭)
    val enableModifyBank: String?, //是否允许编辑银行卡， 0 禁止 1允许
    val enablePhone: String?, //是否开启手机号(1：开启，0：关闭)
    val enableQQ: String?, //是否开启QQ(1：开启，0：关闭)
    val enableRegValidCode: String?, //是否开启注册验证码(1：开启，0：关闭)
    val enableSmsValidCode: String?, //是否开启短信验证码(1：开启，0：关闭)
    val enableTelegram: String?, //是否开启Telegram(1：开启，0：关闭)
    val enableValidCode: String?, //是否开启登录验证码(1：开启，0：关闭)
    val enableWechat: String?, //是否开启微信检测(1：开启，0：关闭)
    val enableAddress: String?, //是否开启address(1：开启，0：关闭)
    val enableWhatsApp: String?, //是否开启WhatsApp(1：开启，0：关闭)
    val enableWithdrawEmail: String?, //提现时Email不能为空
    val enableWithdrawFullName: String?, //提现时真实姓名不能为空
    val enableWithdrawFundPwd: String?, //提现时提现密码不能为空
    val enableWithdrawPhone: String?, //提现时手机号不能为空
    val enableWithdrawQQ: String?, //提现时QQ不能为空
    val enableWithdrawWechat: String?, //提现时微信不能为空
    val enableXinDaiBa: String?, //是否开启信贷吧（1-开启，0-关闭）
    val enableYuEBao: String?, //是否开启余额宝（1-开启，0-关闭）
    val enableZalo: String?, //是否开启Zalo(1：开启，0：关闭)
    val exclusiveAgentUrl: String?, //代理专属域名
    val extendJs: String?, //统计代码
    val facebook: String?,
    val facebookLink: String?,
    val facebookQR: String?,
    val flyOpen: String?, //飞单是否开启 0:关闭,1-开启,默认0为关闭
    val imageList: List<ImageData>?, //图片列表
    val lhc: Lhc?, //六合彩
    val lotteryLiveUrl: String?, //开奖直播地址
    val mainAgentQQ: String?, //推广页面代理QQ
    val mainCustomerQQ: String?, //推广页面客服QQ
    val mainEmail: String?, //推广页面代理Email
    val mainPhone: String?, //推广页面联系电话
    val mainQQUrl: String?, //推广面QQ连接
    val mainWxUrl: String?, //推广面微信连接
    val maintainInfo: String?, //维护页面描述
    val maintainStatus: String?, //系统维护开关 (1：开启，0：关闭)
    val minRechMoney: String?, //最低充值金额限制
    val mobileAppDownUrl: String?, //手机版APP下载地址
    val navigationUrl: String?, //navigationUrl导航地址
    val opStatus: Long?, //运营状态 0：非直营，1：直营，2：外部接入
    val rankingOpen: String?, //排行榜开关
    val rankingRewardAmount: String?, //聊天室的最高打赏金额
    val rebateRatio: String?, //返点比率
    val resServerHost: String?, //静态资源服务器地址
    val serverTime: Long?, //服务器时间
    val smsValidTime: String?, //短信验证码有效时间（分钟）
    val stopBetTime: String?, //所有游戏的早上开盘时间
    val stopBetTime2: String?, //所有游戏的次日封盘时间
    val telegram: String?,
    val telegramLink: String?,
    val telegramQR: String?,
    val thirdOpen: String?, //第三方游戏开关
    val thirdTransferOpen: String?, //第三方自动转账开关
    val weixinQR: String?,
    val whatsApp: String?,
    val whatsAppLink: String?,
    val whatsAppQR: String?,
    val withDrawBalanceLimit: String?, //最低提现金额限制
    val zalo: String?,
    val zaloLink: String?,
    val zaloQR: String?,
    val sportAnimation: String?,//体育动画接口 20210812確認暫時無用處
    val liveUrl: String?,
    val analysisUrl: String?,
    val referUrl: String?,
    val enableMinRemainingBalance: String?,//是否启用账户首次提现最小剩余额度(1：开启，0：关闭)
    val minRemainingBalance: String?,//账户首次提现最小剩余额度
    val presetBetAmount: List<Int>?, //前台预设下注金额配置
    val systemCurrencySign: String?,
    val systemCurrency: String? = "PHP",
    val customerFloating: String? = "0", //在线客服悬浮按钮开关 (1：开启，0：关闭 默认是null:关闭)
    val realNameWithdrawVerified: String? = null ,//提现实名制验证开关 (1：开启，0：关闭， 默认是null:关闭)
    val perBetMaxAmount: String?,
    val perBetMinAmount: String?,
    val selfRestraintVerified: String?
)

enum class VerifySwitchType(val value: String) {
    OPEN("1"), CLOSE("0")
}


