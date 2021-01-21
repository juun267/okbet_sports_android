package org.cxct.sportlottery.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val updatePayPw: Int? = null,

    @ColumnInfo(name = "setted")
    val setted: Int? = null
)
