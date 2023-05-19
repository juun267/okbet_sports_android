package org.cxct.sportlottery.ui.chat

class UserInfoOutput : BaseOutput() {

    var t: Data? = null

    class Data {
        var userId: Int? = null //主键

        var userName: String? = null //用户名

        var fullName: String? = null //全名，银行卡上的真实名称

        var roleId: Int? = null //角色id

        var userType: String? = null //用户类型（"ADMIN"：超管；"ZDL"：总代理；"DL"：代理；"HY"：会员；"ZZH"：子账户）

        var superId: Int? = null //上级代理id

        var superUserName: String? = null //上级用户名

        var platformId: Int? = null //平台id

        var userLevelId: Int? = null //用户等级id

        var email: String? = null //邮箱

        var qq: String? = null //QQ

        var phone: String? = null //电话

        var nickName: String? = null //昵称

        var wechat: String? = null //微信

        var updatePw: Int? = null //是否需要更新密码: 0 不用，1 需要

        var updatePayPw: Int? = null //是否需要更新资金密码: 0 不用，1 需要

        var iconUrl: String? = null //头像地址

        var loginIp: String? = null //当前登录IP

        var loginTime: Long? = null //当前登录时间 //時間戳 timestamp 格式

        var lastLoginIp: String? = null //上次登录IP

        var lastLoginTime: Long? = null //上次登录时间 //時間戳 timestamp 格式

        var testFlag: Int? = null //测试试玩账号类型：0-普通账号，2-内部测试账号，1-游客

        var setted: Int? = null //是否设置过昵称 0单标未设置过 1代表设置过

        var hyType: Int? = null //会员类型

        var userRebateList: MutableList<UserRebate>? = null //用户返点数据

        var userAgentRebateList: MutableList<UserRebate>? = null //全民代理赔率

        var isModfiyOdds: String? = null //系统级别是否能让代理会员修改下级会员赔率

        var agentRegister: Int? = 1
    }

    class UserRebate {
        var gameCate: String? = null //类别名称

        var cateId: Long? = null //类别id

        var rebate: Double? = null //返点
    }

}