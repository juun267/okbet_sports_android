package org.cxct.sportlottery.ui.chat

class LoginOutput : BaseOutput() {

    var t: Data? = null

    class Data {
        var uid: Number? = null //用户id（保留，建议用userId）

        var userId: Number? = null //用户id

        var token: String? = null //token值

        var loginDate: Long? = null //登录日期 //時間戳 timestamp 格式

        var loginIp: String? = null //登录ip

        var lastLoginDate: Long? = null //最近一次登录日期 //時間戳 timestamp 格式

        var lastLoginIp: String? = null //最近一次登录ip

        var userName: String? = null //用户名

        var nickName: String? = null //昵称

        var userType: String? = null //用户类型（"ADMIN"：超管；"ZDL"：总代理；"DL"：代理；"HY"：会员；"ZZH"：子账户）

        var platformId: Int? = null //平台id

        var fullName: String? =
            null //真实名称 //20200629 統一使用 UserInfoOutput 裡面的資料，在更新資料時才能確保獲取最新的"真實姓名"

        var testFlag: Int? = null //是否测试用户（0-正常用户，1-游客，2-内部测试）

        var rechLevel: String? = null //充值层级

        var iconUrl: String? = null //头像地址

        var hyType: Int? = null //会员类型

        var userRebateList: MutableList<UserRebate>? = null //用户返点数据

        var isModfiyOdds: String? = null //系统级别是否能让代理会员修改下级会员赔率

        val liveSign: String? = null //直播签名

    }

    class UserRebate {
        var gameCate: String? = null //类别名称

        var cateId: Int? = null //类别id

        var rebate: Number? = null //返点
    }

}