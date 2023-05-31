package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.toJson
import retrofit2.Response

object UserInfoRepository {

    val sharedPref: SharedPreferences by lazy {
        MultiLanguagesApplication.appContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    // 拉取userInfo时间戳的记录
    var lastRequestUserInfoTime = 0L

    val userInfo: LiveData<UserInfo?>
        get() = MultiLanguagesApplication.mInstance.userInfo

    suspend fun getUserInfo(): Response<UserInfoResult> {
        lastRequestUserInfoTime = System.currentTimeMillis()
        val userInfoResponse = OneBoSportApi.userService.getUserInfo()

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let {
                if (it.success)
                    updateUserInfo(it.userInfoData)
            }
        }
        return userInfoResponse
    }

    @WorkerThread
    suspend fun updateUserInfo(userInfoData: UserInfoData?) {
        if (userInfoData == null) {
            lastRequestUserInfoTime = 0
            return
        }

        val userInfo = transform(userInfoData)
//            OLD_DISCOUNT = it.discount ?: 1f
            //userInfoDao.upsert(userInfo)
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)

        GameConfigManager.maxBetMoney = userInfoData.maxBetMoney ?: 9999999
        GameConfigManager.maxCpBetMoney = userInfoData.maxCpBetMoney ?: 9999
        GameConfigManager.maxParlayBetMoney = userInfoData.maxParlayBetMoney ?: 9999

        with(sharedPref.edit()){
            putInt(KEY_USER_LEVEL_ID, userInfoData.userLevelId)
            userInfoData?.liveSyncUserInfoVO?.let {
                putString(KEY_LIVE_USER_INFO, it.toJson())
            }
            apply()
        }
            
    }

    suspend fun getDiscount(userId: Long): Float {
//        return withContext(Dispatchers.IO) {
//            userInfoDao.getDiscount(userId) ?: 1.0F
//        }
        return MultiLanguagesApplication.getInstance()?.userInfo()?.discount?:1.0F
    }

    suspend fun updatePayPwFlag(userId: Long) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updatePayPw(userId, 0)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.updatePayPw = 0
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

//    suspend fun updateIconUrl(userId: Long, iconUrl: String) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateIconUrl(userId, iconUrl)
//        }
//    }

    suspend fun updateNickname(userId: Long, nickname: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.nickName = nickname
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateFullName(userId: Long, fullName: String){
//        withContext(Dispatchers.IO){
//            userInfoDao.updateFullName(userId, fullName)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.fullName = fullName
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateQQ(userId: Long, qq: String){
//        withContext(Dispatchers.IO){
//            userInfoDao.updateQQ(userId, qq)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.qq = qq
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateEmail(userId: Long, email: String){
//        withContext(Dispatchers.IO){
//            userInfoDao.updateEmail(userId, email)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.email = email
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updatePhone(userId: Long, phone: String){
//        withContext(Dispatchers.IO){
//            userInfoDao.updatePhone(userId, phone)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.phone = phone
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateWeChat(userId: Long, wechat: String){
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.wechat = wechat
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    //是否设置过昵称 0单标未设置过 1代表设置过
    suspend fun updateSetted(userId: Long, setted: Int) {
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.setted = setted
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

//    suspend fun updateMaxBetMoney(userId: Long, maxBetMoney: Int) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateMaxBetMoney(userId, maxBetMoney)
//        }
//    }
//
//    suspend fun updateMaxParlayBetMoney(userId: Long, maxParlayBetMoney: Int) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateMaxParlayBetMoney(userId, maxParlayBetMoney)
//        }
//    }
//
//    suspend fun updateMaxCpBetMoney(userId: Long, maxCpBetMoney: Int) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateMaxCpBetMoney(userId, maxCpBetMoney)
//        }
//    }

    suspend fun updateDiscount(userId: Long, discount: Float) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateDiscount(userId, discount)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.discount = discount
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateVerified(userId: Long, verified: Int) {
//        withContext(Dispatchers.IO) {
//            userInfoDao.updateVerified(userId, verified)
//        }
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.verified = verified
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }


    fun updateOddsChangeOption(option:Int){
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.oddsChangeOption = option
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    private fun transform(userInfoData: UserInfoData) =
        UserInfo(
            userInfoData.userId,
            fullName = userInfoData.fullName,
            iconUrl = userInfoData.iconUrl,
            lastLoginIp = userInfoData.lastLoginIp,
            loginIp = userInfoData.loginIp,
            nickName = userInfoData.nickName,
            platformId = userInfoData.platformId,
            testFlag = userInfoData.testFlag,
            userName = userInfoData.userName,
            userType = userInfoData.userType,
            email = userInfoData.email,
            qq = userInfoData.qq,
            phone = userInfoData.phone,
            wechat = userInfoData.wechat,
            updatePayPw = userInfoData.updatePayPw,
            setted = userInfoData.setted,
            userRebateList = userInfoData.userRebateList,
            maxBetMoney = userInfoData.maxBetMoney,
            maxCpBetMoney = userInfoData.maxCpBetMoney,
            maxParlayBetMoney = userInfoData.maxParlayBetMoney,
            discount = userInfoData.discount,
            verified = userInfoData.verified,
            perBetLimit = userInfoData.perBetLimit,
            uwEnableTime = userInfoData.uwEnableTime,
            maxPayout = userInfoData.maxPayout,
            firstRechTime = userInfoData.firstRechTime,
            oddsChangeOption = userInfoData.oddsChangeOption,
            googleBind = userInfoData.googleBind,
            facebookBind = userInfoData.facebookBind,
            passwordSet = userInfoData.passwordSet,
            vipType = userInfoData.vipType,
        )

}