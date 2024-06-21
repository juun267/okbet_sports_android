package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.*
import org.cxct.sportlottery.network.index.login_for_guest.LoginForGuestRequest
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.user.UserSwitchResult
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.network.user.info.UserBasicInfoRequest
import org.cxct.sportlottery.util.*
import retrofit2.Response
import retrofit2.http.Body

// 原则上键定义在什么地方就只能在什么地方，对外暴露的只有读取和修改所存储数据的方法

const val NAME_LOGIN = "login"
const val KEY_TOKEN = "token"
const val KEY_ACCOUNT = "account"
const val KEY_PLATFORM_ID = "platformId"
const val KEY_ODDS_TYPE = "oddsType"
const val KEY_USER_ID = "user_id"
const val KEY_USER_LEVEL_ID = "user_Level_Id"


object LoginRepository {
    private val sharedPref: SharedPreferences by lazy {
        MultiLanguagesApplication.appContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val isLogin: LiveData<Boolean> by lazy {
        val mutableLiveData = MutableLiveData<Boolean>()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            MultiLanguagesApplication.mInstance.userInfo.observeForever { mutableLiveData.value = it != null }
        } else {
            post {
                MultiLanguagesApplication.mInstance.userInfo.observeForever { mutableLiveData.value = it != null }
            }
        }
        mutableLiveData
    }

    val kickedOut: LiveData<Event<String?>>
        get() = _kickedOut


    val _kickedOut = MutableLiveData<Event<String?>>()

    private val mUserMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = mUserMoney

    fun isLogined() = isLogin.value == true

    var platformId
        get() = sharedPref.getLong(KEY_PLATFORM_ID, -1)
        set(value) {
            with(sharedPref.edit()) {
                putLong(KEY_PLATFORM_ID, value)
                apply()
            }
        }

    var token
        get() = sharedPref.getString(KEY_TOKEN, "")
        set(value) {
            with(sharedPref.edit()) {
                putString(KEY_TOKEN, value)
                apply()
            }
        }

    var userId
        get() = sharedPref.getLong(KEY_USER_ID, -1)
        set(value) {
            with(sharedPref.edit()) {
                putLong(KEY_USER_ID, value)
                apply()
            }
        }

    var account
        get() = sharedPref.getString(KEY_ACCOUNT, "")
        set(value) {
            with(sharedPref.edit()) {
                putString(KEY_ACCOUNT, value)
                apply()
            }
        }

    fun updateMoney(money: Double?) {
        mUserMoney.postValue(money)
    }

    fun userMoney(): Double {
        return if (isLogined()) mUserMoney.value ?: 0.0 else 0.0
    }

    private var lastMoneyTime = 0L

    /**
     *  获取平台余额，并转出三方游戏余额
     *  allTransferOut：是否要转出检查
     */
    suspend fun getMoneyAndTransferOut(allTransferOut: Boolean = true) {
        if (!isLogined()) {
            mUserMoney.postValue(0.0)
            return
        }

        val time = System.currentTimeMillis()
        if (time - lastMoneyTime < 100) {
            return
        }

        lastMoneyTime = time
        withContext(Dispatchers.IO) {

            if (allTransferOut && isThirdTransferOpen()) { //如果三方游戏额度自动转换开启
                kotlin.runCatching { OneBoSportApi.thirdGameService.allTransferOut() }
            }

            val result = kotlin.runCatching { OneBoSportApi.userService.getMoney() }
            val userMoneyResult = result.getOrNull()
            if (result.isSuccess && userMoneyResult?.isSuccessful == true) {
                mUserMoney.postValue(userMoneyResult?.body()?.money)
            }
        }
    }

    fun allTransferOut(callback: ((Boolean) -> Unit)? = null) {
        if (!isLogined()) {
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val respnose = kotlin.runCatching { OneBoSportApi.thirdGameService.allTransferOut() }.getOrNull() ?: return@launch
            callback?.let {
                withContext(Dispatchers.Main) {
                    it.invoke(respnose.body()?.success == true)
                }
            }
        }
    }


    /**
     * 获取用户完善个人信息开关
     */
    suspend fun getUserInfoSwitch(): Response<UserSwitchResult> {
        return OneBoSportApi.indexService.getUserInfoSwitch()
    }

    /**
     * 是否已完善个人信息
     */
    suspend fun getUserInfoCheck(): Response<UserSwitchResult> {
        return OneBoSportApi.indexService.getUserInfoCheck()
    }

    suspend fun register(registerRequest: RegisterRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.register(registerRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                //於遊客帳號添加投注項目至注單內後直接註冊正式帳號 因不會走登出流程 所以直接先清出local user info
                clear()
                account = registerRequest.userName //預設存帳號
            }
        }

        return loginResponse
    }

    suspend fun login(@Body params: LoginRequest): Response<LoginResult> {
        return OneBoSportApi.indexService.login(params)
    }

    /**
     * 提交用户基本信息
     */
    suspend fun commitUserBasicInfo(infoRequest: UserBasicInfoRequest): Response<NetResult> {
        return OneBoSportApi.indexService.commitUserBasicInfo(infoRequest)
    }

    suspend fun loginOrReg(loginRequest: LoginRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.loginOrReg(loginRequest)
        return loginResponse
    }

    suspend fun googleLogin(token: String, inviteCode: String?): Response<LoginResult> {

        return OneBoSportApi.indexService.googleLogin(LoginTokenRequest(token,
            inviteCode = inviteCode))
    }

    suspend fun facebookLogin(token: String, inviteCode: String?): Response<LoginResult> {
        return OneBoSportApi.indexService.facebookLogin(LoginTokenRequest(token,
            inviteCode = inviteCode))
    }

    suspend fun bindGoogle(token: String): Response<AuthBindResult> {
        return OneBoSportApi.indexService.bindGoogle(LoginTokenRequest(token))
    }

    suspend fun bindFaceBook(token: String): Response<AuthBindResult> {
        return OneBoSportApi.indexService.bindFacebook(LoginTokenRequest(token))
    }

    suspend fun regPlatformUser(token: String, loginRequest: LoginRequest): Response<LoginResult> {
        return OneBoSportApi.indexService.regPlatformUser(token, loginRequest)
    }
    suspend fun setUpLoginData(loginData: LoginData?) {
        updateLoginData(loginData)
        updateUserInfo(loginData)
    }

    suspend fun sendLoginDeviceSms(token: String): Response<NetResult> {
        return OneBoSportApi.indexService.sendLoginDeviceSms(token)
    }

    suspend fun validateLoginDeviceSms(
        token: String,
        validateLoginDeviceSmsRequest: ValidateLoginDeviceSmsRequest,
    ): Response<NetResult> {
        return OneBoSportApi.indexService.validateLoginDeviceSms(token,
            validateLoginDeviceSmsRequest)
    }


    suspend fun loginForGuest(): Response<LoginResult> {
        return OneBoSportApi.indexService.loginForGuest(LoginForGuestRequest(deviceSn = getDeviceName()))
    }

    private fun getDeviceName(): String {
        val manufacturer: String = Build.MANUFACTURER
        val model: String = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s.isNullOrEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    suspend fun checkIsUserAlive(): Response<NetResult> {
        return OneBoSportApi.indexService.checkToken()
    }

    fun logoutAPI() {
        token?.let {
            GlobalScope.launch(Dispatchers.IO) { runWithCatch { OneBoSportApi.indexService.logout(it) } }
        }
    }

     fun hasToken():Boolean{
        return !sharedPref.getString(KEY_TOKEN, null).isNullOrEmpty()
    }

    private fun updateLoginData(loginData: LoginData?) {
        GameConfigManager.maxBetMoney = loginData?.maxBetMoney ?: 9999999
        GameConfigManager.maxCpBetMoney = loginData?.maxCpBetMoney ?: 9999
        GameConfigManager.maxParlayBetMoney = loginData?.maxParlayBetMoney ?: 9999

        with(sharedPref.edit()) {
            /*putBoolean(KEY_IS_LOGIN, loginData != null)*/
            putString(KEY_TOKEN, loginData?.token)
            putLong(KEY_USER_ID, loginData?.userId ?: -1)
            putLong(KEY_PLATFORM_ID, loginData?.platformId ?: -1)
            apply()
        }
    }

    fun clear() {
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_ODDS_TYPE)
            apply()
        }
        KvUtils.removeKey(KEY_USER_LEVEL_ID)
//        clearRecentPlayGame()
        clearUserInfo()
    }

    @WorkerThread
    private suspend fun updateUserInfo(loginData: LoginData?) {
        loginData?.let {
            val userInfo = transform(it)
            MultiLanguagesApplication.getInstance()?.saveUserInfo(userInfo)
//            withContext(Dispatchers.IO) {
//                userInfoDao.upsert(userInfo)
//            }

        }
    }

    private  fun clearUserInfo() {
        MultiLanguagesApplication.getInstance().saveUserInfo(null)
        GameConfigManager.maxBetMoney = 9999999
        GameConfigManager.maxCpBetMoney = 9999
        GameConfigManager.maxParlayBetMoney = 9999
    }

    private fun transform(loginData: LoginData): UserInfo =
        UserInfo(
            loginData.userId ?: 0,
            fullName = loginData.fullName,
            iconUrl = loginData.iconUrl,
            lastLoginIp = loginData.lastLoginIp,
            loginIp = loginData.loginIp,
            nickName = loginData.nickName,
            platformId = loginData.platformId,
            testFlag = loginData.testFlag,
            userName = loginData.userName,
            userType = loginData.userType,
            userRebateList = loginData.userRebateList,
            verified = loginData.verified,
            vipType = loginData.vipType,
            discountByGameTypeList = loginData.discountByGameTypeList,
        )
}