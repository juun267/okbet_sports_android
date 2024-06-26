package org.cxct.sportlottery.network.index.login

import android.os.Parcelable
import android.provider.Settings
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.CaptchaRequest
import org.cxct.sportlottery.repository.LOGIN_SRC

@KeepMembers
@Parcelize
data class LoginRequest(
    val account: String,
    val password: String? = null, //MD5 加密
    val loginSrc: Long = LOGIN_SRC, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    val deviceSn: String = Constants.deviceSn, //设备号（手机app登录必传）
    val appVersion: String = org.cxct.sportlottery.BuildConfig.VERSION_NAME,
    val loginEnvInfo: String = Settings.Secure.getString(MultiLanguagesApplication.getInstance().contentResolver, Settings.Secure.ANDROID_ID), //登入环境信息
    val securityCode: String? = null, //短信或者邮件验证码
    val inviteCode: String? = null
): Parcelable,CaptchaRequest{
    override var validCodeIdentity: String? = null
    override var validCode: String? = null
    override var ticket: String? = null//腾讯云滑块验证码参数
    override var randstr: String? = null

}
