package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.checktoken.CheckTokenResult
import org.cxct.sportlottery.network.index.login.*
import org.cxct.sportlottery.network.index.login_for_guest.LoginForGuestRequest
import org.cxct.sportlottery.network.index.logout.LogoutRequest
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.util.*
import retrofit2.Response

const val NAME_LOGIN = "login"
const val KEY_TOKEN = "token"
const val KEY_ACCOUNT = "account"
const val KEY_PWD = "pwd"
const val KEY_PLATFORM_ID = "platformId"
const val KEY_REMEMBER_PWD = "remember_pwd"
const val KEY_ODDS_TYPE = "oddsType"
const val KEY_IS_CREDIT_ACCOUNT = "is_credit_account"
const val KEY_DISCOUNT = "discount"
const val KEY_USER_ID = "user_id"
const val KEY_USER_LEVEL_ID = "user_Level_Id"
const val KEY_LIVE_USER_INFO = "live_user_info"


object LoginRepository {
    private val sharedPref: SharedPreferences by lazy {
        MultiLanguagesApplication.appContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val isLogin: LiveData<Boolean>
        get() = _isLogin

    val kickedOut: LiveData<Event<String?>>
        get() = _kickedOut

    val transNum: LiveData<Int?> //交易狀況數量
        get() = _transNum

    val _isLogin = MutableLiveData<Boolean>()
    val _kickedOut = MutableLiveData<Event<String?>>()
    private val _transNum = MutableLiveData<Int?>()

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

    var password: String?
        get() {
            return try {
                val securityKey = AesCryptoUtil.encrypt(KEY_PWD)
                val securityValue = sharedPref.getString(securityKey, "") ?: ""
                AesCryptoUtil.decrypt(securityValue)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        set(value) {
            try {
                with(sharedPref.edit()) {
                    val securityKey = AesCryptoUtil.encrypt(KEY_PWD)
                    val securityValue = AesCryptoUtil.encrypt(value ?: "")
                    putString(securityKey, securityValue)
                    commit()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    var isRememberPWD
        get() = sharedPref.getBoolean(KEY_REMEMBER_PWD, false)
        set(value) {
            with(sharedPref.edit()) {
                putBoolean(KEY_REMEMBER_PWD, value)
                commit()
            }
        }

    var isCheckToken = false

    suspend fun register(registerRequest: RegisterRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.register(registerRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {

                //於遊客帳號添加投注項目至注單內後直接註冊正式帳號 因不會走登出流程 所以直接先清出local user info
                clear()

                account = registerRequest.userName //預設存帳號
                setUpLoginData(it.loginData)
            }
        }

        return loginResponse
    }

    suspend fun login(loginRequest: LoginRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.login(loginRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                setUpLoginData(it.loginData)
            }
        }

        return loginResponse
    }

    suspend fun loginOrReg(loginRequest: LoginRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.loginOrReg(loginRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                setUpLoginData(it.loginData)
            }
        }

        return loginResponse
    }

    suspend fun googleLogin(token: String): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.googleLogin(LoginTokenRequest(token))

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                setUpLoginData(it.loginData)
            }
        }

        return loginResponse
    }

    suspend fun facebookLogin(token: String): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.facebookLogin(LoginTokenRequest(token))

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                setUpLoginData(it.loginData)
            }
        }

        return loginResponse
    }

    suspend fun bindGoogle(token: String): Response<AuthBindResult> {
        val loginResponse = OneBoSportApi.indexService.bindGoogle(LoginTokenRequest(token))

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                MultiLanguagesApplication.getInstance()?.userInfo()?.let {
                    it.googleBind = true
                    MultiLanguagesApplication.getInstance()?.saveUserInfo(it)
                }
            }
        }
        return loginResponse
    }

    suspend fun bindFaceBook(token: String): Response<AuthBindResult> {
        val loginResponse = OneBoSportApi.indexService.bindFacebook(LoginTokenRequest(token))

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                MultiLanguagesApplication.getInstance()?.userInfo()?.let {
                    it.facebookBind = true
                    MultiLanguagesApplication.getInstance()?.saveUserInfo(it)
                }
            }
        }
        return loginResponse
    }

    suspend fun setUpLoginData(loginData: LoginData?) {
        isCheckToken = true
        updateLoginData(loginData)
        updateUserInfo(loginData)
    }

    suspend fun sendLoginDeviceSms(token: String): Response<LogoutResult> {
        return OneBoSportApi.indexService.sendLoginDeviceSms(token)
    }

    suspend fun validateLoginDeviceSms(
        token: String,
        validateLoginDeviceSmsRequest: ValidateLoginDeviceSmsRequest,
    ): Response<LogoutResult> {
        return OneBoSportApi.indexService.validateLoginDeviceSms(token,
            validateLoginDeviceSmsRequest)
    }


    suspend fun loginForGuest(): Response<LoginResult> {

        val loginForGuestResponse =
            OneBoSportApi.indexService.loginForGuest(LoginForGuestRequest(deviceSn = getDeviceName()))

        if (loginForGuestResponse.isSuccessful) {
            loginForGuestResponse.body()?.let {
                setUpLoginData(it.loginData)
            }
        }

        return loginForGuestResponse
    }

    fun updateTransNum(transNum: Int) {
        _transNum.postValue(transNum)
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
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    suspend fun checkToken(): Response<CheckTokenResult> {
        val checkTokenResponse = OneBoSportApi.indexService.checkToken()

        if (checkTokenResponse.isSuccessful) {
            checkTokenResponse.body()?.let {
                isCheckToken = true
                _isLogin.value = true
            }
        } else {

            isCheckToken = false
            _isLogin.value = false
            clear()
        }

        return checkTokenResponse
    }

    suspend fun checkIsUserAlive(): Response<CheckTokenResult> {
        return OneBoSportApi.indexService.checkToken()
    }

    suspend fun logoutAPI(): Response<LogoutResult> {
        _isLogin.value = false
        val emptyList = mutableListOf<String>()
        MultiLanguagesApplication.saveSearchHistory(emptyList)
        return OneBoSportApi.indexService.logout(LogoutRequest()).apply {
            clear()
        }
    }

    suspend fun logout() {
        _isLogin.value = false
        val emptyList = mutableListOf<String>()
        MultiLanguagesApplication.saveSearchHistory(emptyList)
        clear()
    }

    private fun updateLoginData(loginData: LoginData?) {
        _isLogin.postValue(loginData != null)

        GameConfigManager.maxBetMoney = loginData?.maxBetMoney ?: 9999999
        GameConfigManager.maxCpBetMoney = loginData?.maxCpBetMoney ?: 9999
        GameConfigManager.maxParlayBetMoney = loginData?.maxParlayBetMoney ?: 9999

        with(sharedPref.edit()) {
            /*putBoolean(KEY_IS_LOGIN, loginData != null)*/
            putString(KEY_TOKEN, loginData?.token)
            putLong(KEY_USER_ID, loginData?.userId ?: -1)
            putLong(KEY_PLATFORM_ID, loginData?.platformId ?: -1)
            putFloat(KEY_DISCOUNT, loginData?.discount ?: 1f)
            loginData?.liveSyncUserInfoVO?.let {
                putString(KEY_LIVE_USER_INFO, it.toJson())
            }
            apply()
        }
    }

    suspend fun clear() {
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_ODDS_TYPE)
            remove(KEY_LIVE_USER_INFO)
            KvUtils.removeKey(KV_STR_SELECT_ODDS_MODE)
            apply()
        }
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

    @WorkerThread
    private suspend fun clearUserInfo() {
        withContext(Dispatchers.IO) {
            MultiLanguagesApplication.getInstance()?.saveUserInfo(null)
            GameConfigManager.maxBetMoney = 9999999
            GameConfigManager.maxCpBetMoney = 9999
            GameConfigManager.maxParlayBetMoney = 9999
        }
    }

    private fun transform(loginData: LoginData): UserInfo =
        UserInfo(
            loginData.userId,
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
            discount = loginData.discount,
            verified = loginData.verified,
        )
}