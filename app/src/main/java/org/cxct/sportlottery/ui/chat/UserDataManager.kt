package org.cxct.sportlottery.ui.chat

import android.widget.ImageView
import org.cxct.sportlottery.R

/**
 * 獲取 用戶資訊
 *
 * 20200609 紀錄：token 參數使用 getLoginData() 獲取，其他用戶資訊優先使用 getUserInfo()
 */
object UserDataManager {
    private const val TAG = "UserDataManager"

    private var mUserDataOutput: UserDataOutput? = null
    var mTodayRewardRebate: Double = 0.0

    fun setUserDataOutput(value: UserDataOutput?) {
        mUserDataOutput = value
    }

    fun getUserDataOutput(): UserDataOutput {
        return mUserDataOutput ?: UserDataOutput() //若為 null 返回預設的空 data，防止資料使用產生 NullException
    }

    /**
     * 登入資訊
     */
    private var mLoginData: LoginOutput.Data? = null
    fun setLoginData(value: LoginOutput.Data?) {
        mLoginData = value
    }

    fun getLoginData(): LoginOutput.Data {
        val loginData =
            mLoginData ?: LoginOutput.Data() //若為 null 返回預設的空 data，防止資料使用產生 NullException
        mLoginData = loginData
        return loginData
    }

    fun isUnLogin(): Boolean {
        return mLoginData?.userId == null
    }

    /**
     * 使用者資訊
     */
    private var mUserData: UserInfoOutput.Data? = null
    fun setUserInfo(value: UserInfoOutput.Data?) {
        mUserData = value
    }

    fun getUserInfo(): UserInfoOutput.Data {
        val userData = mUserData
            ?: UserInfoOutput.Data() //若為 null 返回預設的空 data，防止資料使用產生 NullException
        mUserData = userData
        return userData
    }

//    fun getRebate(cateId: Long?): Double {
//        return cateId?.let {
//            if (LoginRepository.isLogin.value == true) {
//                return getUserInfo().userRebateList?.firstOrNull { it.cateId == cateId }?.rebate ?: 0.0
//            }
//            else {
//                return AppConfigManager.getAppConfig().guestRebate?.toDouble() ?: 0.0
//            } //用户返点
//        } ?: 0.0
//    }

    /**
     * 獲取用戶餘額
     */
    var money: Double =
        0.0 //TODO simon test review 使用者帳戶餘額紀錄，原來API 登入時回傳參數會有 money，新版沒有，所以自訂一筆帶之後看要如何使用

    /**
     * 設置 本地時間 與 伺服器時間差
     */
    private var mDiffTime: Long = 0
    fun setDiffTime(diffTime: Long) {
        mDiffTime = diffTime
    }

    fun getDiffTime(): Long {
        return mDiffTime
    }


    /**
     * 提現手续费
     */
    private var mWithdrawRate: Double? = null

    fun setWithdrawRate(withdrawRate: Double?) {
        mWithdrawRate = withdrawRate
    }

    fun getWithdrawRate(): Double? {
        return mWithdrawRate
    }

    fun clear() {
        mLoginData = LoginOutput.Data()
        mUserData = UserInfoOutput.Data()
        money = 0.0
        mDiffTime = 0
        mWithdrawRate = null
    }

    /**
     * 選擇預選頭像的編號
     */
    fun getLocaleAvatarIconResNum(iconUrl: String?): String? {
        //後端回傳的iconUrl進行切割取得本地端圖片序號，目前格式為(域名信息###pro-pic_數字)
        return iconUrl?.split("###pro-pic_")?.last()
    }

    fun isAgentRegister(): Boolean {
        if (getUserInfo().agentRegister == null) return true
        return getUserInfo().agentRegister == 1
    }

    /**
     * 讀取頭像,使用者頭像：預選頭像、上傳頭像 兩種
     * @param targetImageView: 寫入的 imageView
     * @param iconUrl: 頭像 url
     */
    // 專案內的預設頭像 R.drawable.chat_avatar 改成 R.drawable.icon_avatar_1 -> 2020/8/10
//     專案內的預設頭像 R.drawable.ic_head 改成 R.drawable.icon_avatar_1 -> 2020/8/10
    fun loadIntoUserAvatarIcon(
        targetImageView: ImageView,
        iconUrl: String?,
        defaultRes: Int = R.drawable.icon_avatar_1,
    ) {
//        if (AppConfigManager.getAppConfig().canUploadIcon == "0") { //TODO Bill 這裡取ConfigOutput.Data()的資料
        val drawableRes = when (getLocaleAvatarIconResNum(iconUrl)) {
            "1" -> R.drawable.icon_avatar_1
            "2" -> R.drawable.icon_avatar_2
            "3" -> R.drawable.icon_avatar_3
            "4" -> R.drawable.icon_avatar_4
            "5" -> R.drawable.icon_avatar_5
            "6" -> R.drawable.icon_avatar_6
            "7" -> R.drawable.icon_avatar_7
            "8" -> R.drawable.icon_avatar_8
            "9" -> R.drawable.icon_avatar_9
            "10" -> R.drawable.icon_avatar_10
            "11" -> R.drawable.icon_avatar_11
            "12" -> R.drawable.icon_avatar_12
            "13" -> R.drawable.icon_avatar_13
            "14" -> R.drawable.icon_avatar_14
            "15" -> R.drawable.icon_avatar_15
            "16" -> R.drawable.icon_avatar_16
            "17" -> R.drawable.icon_avatar_17
            "18" -> R.drawable.icon_avatar_18
            "19" -> R.drawable.icon_avatar_19
            "20" -> R.drawable.icon_avatar_20
            "21" -> R.drawable.icon_avatar_21
            "22" -> R.drawable.icon_avatar_22
            "23" -> R.drawable.icon_avatar_23
            "24" -> R.drawable.icon_avatar_24
            "25" -> R.drawable.icon_avatar_25
            "26" -> R.drawable.icon_avatar_26
            "27" -> R.drawable.icon_avatar_27
            "28" -> R.drawable.icon_avatar_28
            "29" -> R.drawable.icon_avatar_29
            "30" -> R.drawable.icon_avatar_30
            "31" -> R.drawable.icon_avatar_31
            "32" -> R.drawable.icon_avatar_32
            else -> R.drawable.icon_avatar_1
        }

        targetImageView.setImageResource(drawableRes)

//        } else { //使用者上傳頭像
//            Glide.with(targetImageView.context)
//                    .load(iconUrl)
//                    .apply(RequestOptions().placeholder(defaultRes).error(defaultRes))
//                    .into(targetImageView)
//        }
    }

    interface OnMoneyListener {
        fun onBalance(money: Double?)
        fun onError(error: ErrorOutput?)
    }
}