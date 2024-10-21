package org.cxct.sportlottery.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.chat.api.SignService
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.KvUtils
import retrofit2.Response


object UserInfoRepository {

    private val signService by lazy {
        RetrofitHolder.createSignApiService(SignService::class.java)
    }

    var checkedUserInfo = false //紀錄checkToken後是否獲取過UserInfo

    val userInfo: LiveData<UserInfo?>
        get() = MultiLanguagesApplication.mInstance.userInfo

    fun loginedInfo() = userInfo.value

    fun userId() = userInfo.value?.userId ?: -1

    fun userName() = userInfo.value?.userName ?: -1

    fun nickName() = userInfo.value?.nickName ?: ""

    fun isGlifeAccount(): Boolean {
        return userInfo.value?.isGlifeAccount() == true
    }
    fun isMayaAccount(): Boolean {
        return userInfo.value?.isMayaAccount() == true
    }

    fun getPhoneNo() = userInfo.value?.phone

    suspend fun getUserInfo(): Response<UserInfoResult> {
        val userInfoResponse = OneBoSportApi.userService.getUserInfo()

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let {
                if (it.success)
                    updateUserInfo(it.userInfoData)
            }
        }
        return userInfoResponse
    }

    fun loadUserInfo() {
        if (LoginRepository.isLogined()) {
            GlobalScope.launch { runWithCatch { getUserInfo() } }
        }
    }

    @WorkerThread
    fun updateUserInfo(userInfoData: UserInfoData?) {
        if (userInfoData == null) {
            return
        }
        val userInfo = transform(userInfoData)
//            OLD_DISCOUNT = it.discount ?: 1f
        //userInfoDao.upsert(userInfo)
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)

        GameConfigManager.maxBetMoney = userInfoData.maxBetMoney ?: 9999999
        GameConfigManager.maxCpBetMoney = userInfoData.maxCpBetMoney ?: 9999
        GameConfigManager.maxParlayBetMoney = userInfoData.maxParlayBetMoney ?: 9999
        KvUtils.put(KEY_USER_LEVEL_ID, userInfoData.userLevelId)
    }

    fun getUserLevelId(): Int {
        return KvUtils.decodeInt(KEY_USER_LEVEL_ID, -1)
    }

    fun updatePayPwFlag(userId: Long) {
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

    suspend fun updatePlaceOfBirth( str: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.placeOfBirth = str
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateaddress( str: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.address = str
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updatePermanentAddress( str: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.permanentAddress = str
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateZipCode( str: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.zipCode = str
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updatepermanentZipCode( str: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.permanentZipCode = str
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateNickname(userId: Long, nickname: String) {

        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.nickName = nickname
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    fun updateFullName(userId: Long, fullName: String) {
//        withContext(Dispatchers.IO){
//            userInfoDao.updateFullName(userId, fullName)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.fullName = fullName
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateQQ(userId: Long, qq: String) {
//        withContext(Dispatchers.IO){
//            userInfoDao.updateQQ(userId, qq)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.qq = qq
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateEmail(userId: Long, email: String) {
//        withContext(Dispatchers.IO){
//            userInfoDao.updateEmail(userId, email)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.email = email
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updatePhone(userId: Long, phone: String) {
//        withContext(Dispatchers.IO){
//            userInfoDao.updatePhone(userId, phone)
//        }
        var userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        userInfo?.phone = phone
        MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
    }

    suspend fun updateWeChat(userId: Long, wechat: String) {
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

    suspend fun updateDiscount(discountByGameTypeList: List<FrontWsEvent.DiscountByGameTypeVO>?) {
        val userInfo = MultiLanguagesApplication.mInstance.userInfo()
        userInfo?.updateDiscountByGameTypeList(discountByGameTypeList)
        MultiLanguagesApplication.mInstance.saveUserInfo(userInfo)
    }

    fun updateOddsChangeOption(option: Int) {
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
            placeOfBirth = userInfoData.placeOfBirth,
            address = userInfoData.address,
            permanentAddress = userInfoData.permanentAddress,
            zipCode = userInfoData.zipCode,
            permanentZipCode = userInfoData.permanentZipCode,
            firstName = userInfoData.firstName,
            middleName = userInfoData.middleName,
            lastName = userInfoData.lastName,
            birthday = userInfoData.birthday,
            discountByGameTypeList = userInfoData.discountByGameTypeList,
            rejectRemark = userInfoData.rejectRemark,
            levelCode = userInfoData.levelCode,
            regSource = userInfoData.regSource,
            safeQuestionType = userInfoData.safeQuestionType,
            fullVerified = userInfoData.fullVerified,
        )

    suspend fun getSign(constraintType:Int,dataStatisticsRange:Int): ApiResult<JsonElement> {
        val params = JsonObject()
        params.addProperty("constraintType", constraintType)
        params.addProperty("dataStatisticsRange", dataStatisticsRange)
        return safeApi {
            signService.getSign(params)
        }
    }
}