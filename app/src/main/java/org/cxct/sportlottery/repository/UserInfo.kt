package org.cxct.sportlottery.repository

//TODO Dean : 串接完/api/user/info後將此處替換, 接續review 提款頁面資料
//Test For Withdraw by Dean

class UserRebateList {
    var gameCate: String? = null
    var cateId = 0
    var rebate = 0
}

class UserInfo {
    var userId = 0
    var userName: String? = null
    var fullName: String? = null
    var roleId = 0
    var userType: String? = null
    var superId = 0
    var superUserName: String? = null
    var platformId = 0
    var userLevelId = 0
    var email: String? = null
    var qq: String? = null
    var phone: String? = null
    var nickName: String? = null
    var wechat: String? = null
    var updatePw = 0
    var updatePayPw = 0
    var iconUrl: String? = null
    var loginIp: String? = null
    var loginTime: String? = null
    var lastLoginIp: String? = null
    var lastLoginTime: String? = null
    var testFlag = 0
    var setted = 0
    var userRebateList: List<UserRebateList>? = listOf(UserRebateList())
    var bankName: String? = null
    var subAddress: String? = null
    var cardNo: String? = null
    var zalo: String? = null
    var facebook: String? = null
    var whatsapp: String? = null
    var telegram: String? = null
    var remark: String? = null
    var hideFlag = 0
}