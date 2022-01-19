package org.cxct.sportlottery.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import org.cxct.sportlottery.network.common.UserRebate

@Entity(tableName = "user_info_table")
data class UserInfo(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "full_name")
    val fullName: String? = null,

    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,

    @ColumnInfo(name = "last_login_ip")
    val lastLoginIp: String? = null,

    @ColumnInfo(name = "login_ip")
    val loginIp: String? = null,

    @ColumnInfo(name = "nick_name")
    val nickName: String? = null,

    @ColumnInfo(name = "platform_id")
    val platformId: Long? = null,

    @ColumnInfo(name = "test_flag")
    val testFlag: Long? = null,

    @ColumnInfo(name = "user_name")
    val userName: String? = null,

    @ColumnInfo(name = "user_type")
    val userType: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "qq")
    val qq: String? = null,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "wechat")
    val wechat: String? = null,

    @ColumnInfo(name = "update_pay_pw")
    val updatePayPw: Int? = 1,

    @ColumnInfo(name = "setted")
    val setted: Int? = null, //是否设置过昵称 0单标未设置过 1代表设置过

    @ColumnInfo(name = "user_rebate_list")
    val userRebateList: List<UserRebate>? = null,

    @ColumnInfo(name = "credit_account")
    val creditAccount: Int? = null,

    @ColumnInfo(name = "credit_status")
    val creditStatus: Int? = null,

    @ColumnInfo(name = "maxBetMoney")//会员对应vip层级的单注最大下注额
    val maxBetMoney: Int? = null,

    @ColumnInfo(name = "maxParlayBetMoney")//会员对应vip层级的串关最大下注额
    val maxParlayBetMoney: Int? = null,

    @ColumnInfo(name = "maxCpBetMoney")//会员对应vip层级的单注冠军最大下注额
    val maxCpBetMoney: Int? = null,

    @ColumnInfo(name = "discount")
    val discount: Float? = null,

    @ColumnInfo(name = "verified")
    val verified: Int? // 是否通过实名验证,0:未通过 1:已通过 2:验证中 3:验证失败
)
