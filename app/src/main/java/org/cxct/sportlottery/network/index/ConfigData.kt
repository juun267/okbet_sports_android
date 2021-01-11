package org.cxct.sportlottery.network.index

//TODO review 需多欄位文件上沒寫，不知回傳型態和代表什麼
data class ConfigData(
    val agentMode: String?, //代理模式：ulimit 无限级，fixed 固定级
    val agentUrl: String?, //代理地址
    val agentZxkfUrl: String?, //代理专属客服地址
    val appDownUrl: String?, //电脑版APP下载地址
    val cancelOrder: String?,
    val chatAutoConnect: Any?,
    val chatHost: Any?,
    val chatMinBetMoney: Any?,
    val chatOpen: Any?,
    val customerServiceUrl: String?, //平台在线客服url
    val customerServiceUrl2: String?, //平台在线客服url2
    val defaultSkin: String?, //默认皮肤
    val enableEmail: String?, //是否开启Email(1：开启，0：关闭)
    val enableFacebook: Any?,
    val enableFullName: String?, //是否开启真实姓名检测(1：开启，0：关闭)
    val enableFundPwd: String?, //是否开启取款密码(1：开启，0：关闭)
    val enableInviteCode: Any?,
    val enableModifyBank: String,
    val enablePhone: String?, //是否开启手机号(1：开启，0：关闭)
    val enableQQ: String?, //是否开启QQ(1：开启，0：关闭)
    val enableRegValidCode: String?, //是否开启注册验证码(1：开启，0：关闭)
    val enableSmsValidCode: String,
    val enableTelegram: Any?,
    val enableValidCode: String?, //是否开启登录验证码(1：开启，0：关闭)
    val enableWechat: String?, //是否开启微信检测(1：开启，0：关闭)
    val enableWhatsApp: Any?,
    val enableWithdrawEmail: String?, //提现时Email不能为空
    val enableWithdrawFullName: String?, //提现时真实姓名不能为空
    val enableWithdrawFundPwd: String?, //提现时提现密码不能为空
    val enableWithdrawPhone: String?, //提现时手机号不能为空
    val enableWithdrawQQ: String?, //提现时QQ不能为空
    val enableWithdrawWechat: String?, //提现时微信不能为空
    val enableXinDaiBa: Any?,
    val enableYuEBao: Any?,
    val enableZalo: Any?,
    val exclusiveAgentUrl: String?, //代理专属域名
    val extendJs: String?, //统计代码
    val facebook: Any?,
    val facebookLink: Any?,
    val facebookQR: Any?,
    val flyOpen: Any?,
    val imageList: Any?,
    val lhc: Any?,
    val lotteryLiveUrl: String,
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
    val navigationUrl: String,
    val opStatus: Any?,
    val rankingOpen: Any?,
    val rankingRewardAmount: Any?,
    val rebateRatio: Any?,
    val resServerHost: String?, //静态资源服务器地址
    val serverTime: Long,
    val smsValidTime: String,
    val stopBetTime: String,
    val stopBetTime2: String,
    val telegram: Any?,
    val telegramLink: Any?,
    val telegramQR: Any?,
    val thirdOpen: String,
    val thirdTransferOpen: String,
    val weixinQR: Any?,
    val whatsApp: Any?,
    val whatsAppLink: Any?,
    val whatsAppQR: Any?,
    val withDrawBalanceLimit: String?, //最低提现金额限制
    val zalo: Any?,
    val zaloLink: Any?,
    val zaloQR: Any?
)