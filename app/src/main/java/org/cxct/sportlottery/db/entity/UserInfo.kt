package org.cxct.sportlottery.db.entity

//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
import org.cxct.sportlottery.network.common.UserRebate

//@Entity(tableName = "user_info_table")
data class UserInfo(
//    @PrimaryKey
//    @ColumnInfo(name = "user_id")
    var userId: Long,

//    @ColumnInfo(name = "full_name")
    var fullName: String? = null,


//    @ColumnInfo(name = "icon_url")
    var iconUrl: String? = null,

//    @ColumnInfo(name = "last_login_ip")
    var lastLoginIp: String? = null,

//    @ColumnInfo(name = "login_ip")
    var loginIp: String? = null,

//    @ColumnInfo(name = "nick_name")
    var nickName: String? = null,

//    @ColumnInfo(name = "platform_id")
    var platformId: Long? = null,

//    @ColumnInfo(name = "test_flag")
    var testFlag: Long? = null,

//    @ColumnInfo(name = "user_name")
    var userName: String? = null,

//    @ColumnInfo(name = "user_type")
    var userType: String? = null,

//    @ColumnInfo(name = "email")
    var email: String? = null,

//    @ColumnInfo(name = "qq")
    var qq: String? = null,

//    @ColumnInfo(name = "phone")
    var phone: String? = null,

//    @ColumnInfo(name = "wechat")
    var wechat: String? = null,

//    @ColumnInfo(name = "update_pay_pw")
    var updatePayPw: Int? = 1,

//    @ColumnInfo(name = "setted")
    var setted: Int? = null, //是否设置过昵称 0单标未设置过 1代表设置过

//    @ColumnInfo(name = "user_rebate_list")
    var userRebateList: List<UserRebate>? = null,

    /*@ColumnInfo(name = "maxBetMoney")*///会员对应vip层级的单注最大下注额
    var maxBetMoney: Long? = null,

    /*@ColumnInfo(name = "maxParlayBetMoney")*///会员对应vip层级的串关最大下注额
    var maxParlayBetMoney: Long? = null,

    /*@ColumnInfo(name = "maxCpBetMoney")*///会员对应vip层级的单注冠军最大下注额
    var maxCpBetMoney: Long? = null,

//    @ColumnInfo(name = "discount")
    var discount: Float? = null,

//    @ColumnInfo(name = "verified")
    var verified: Int? = null, // 是否通过实名验证,0:未通过 1:已通过 2:验证中 3:验证失败

//    @ColumnInfo(name = "perBetLimit")
    val perBetLimit: Int? = null,

//    @ColumnInfo(name = "oddsChangeOption")
    var oddsChangeOption:Int? = null ,

    val uwEnableTime: Long? = 0,

    val maxPayout: Double? = 0.0,

    val firstRechTime: String? = null, //首充时间

    val currencySign: String? = null, //幣種

    var facebookBind: Boolean = false,

    var googleBind: Boolean = false,

    var passwordSet: Boolean = true,//true 密码为空
)
