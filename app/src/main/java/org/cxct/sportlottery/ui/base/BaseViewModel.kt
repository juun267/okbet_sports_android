package org.cxct.sportlottery.ui.base

import android.app.Application
import android.content.Context
import androidx.annotation.Nullable
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.httpFormat
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.money.RedEnvelopeResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.updateDefaultHandicapType
import retrofit2.Response
import timber.log.Timber


abstract class BaseViewModel(
    val androidContext: Application,
    val loginRepository: LoginRepository,
    val betInfoRepository: BetInfoRepository,
    val infoCenterRepository: InfoCenterRepository
) : ViewModel() {

    companion object {
        private val _errorResultToken = MutableLiveData<Event<BaseResult>>()

        fun postErrorResut(result: BaseResult) {
            _errorResultToken.postValue(Event(result))
        }
    }
    val notifyLogin: SingleLiveEvent<Boolean>
        get() = mNotifyLogin
     val mNotifyLogin = SingleLiveEvent<Boolean>()

    val isLogin: LiveData<Boolean>
        get() = loginRepository.isLogin

    val isKickedOut: LiveData<Event<String?>> by lazy {
        loginRepository.kickedOut
    }

    val errorResultIndex: LiveData<String>
        get() = _errorResultIndex

    val errorResultToken: LiveData<Event<BaseResult>>
        get() = _errorResultToken

    val networkExceptionUnavailable: LiveData<String>
        get() = _networkExceptionUnavailable

    val networkExceptionTimeout: LiveData<String>
        get() = _networkExceptionTimeout

    val networkExceptionUnknown: LiveData<String>
        get() = _networkExceptionUnknown

    private val _errorResultIndex = MutableLiveData<String>()
    private val _networkExceptionUnavailable = MutableLiveData<String>()
    private val _networkExceptionTimeout = MutableLiveData<String>()
    private val _networkExceptionUnknown = MutableLiveData<String>()

    enum class NetWorkResponseType(val code: Int) {
        REQUEST_TOO_FAST(400)
    }

    fun <T : BaseResult> doRequest(
        apiFun: suspend () -> Response<T>, callback: (T?) -> Unit
    ) {
        viewModelScope.launch/*(Dispatchers.IO)*/ {
            val result = doNetwork(androidContext,true, apiFun)
            withContext(Dispatchers.Main) {
                callback.invoke(result)
            }
        }
    }

    //20210526 新增 exceptionHandle 參數，還判斷要不要在 BaseActivity 顯示，exception 錯誤訊息
    @Nullable
    suspend fun <T : BaseResult> doNetwork(
        context: Context = androidContext,
        exceptionHandle: Boolean = true,
        apiFun: suspend () -> Response<T>,
    ): T? {
        return try {
            doApiFun(apiFun)
        } catch (e: Exception) {
            Timber.e("doNetwork: $e")
            e.printStackTrace()
            if (exceptionHandle && e !is CancellationException) {
                _networkExceptionUnavailable.postValue(context.getString(R.string.message_network_no_connect))
            }
            null
        }
    }

    private suspend fun <T : BaseResult> doApiFun(apiFun: suspend () -> Response<T>): T? {
        val apiResult = viewModelScope.async {
            val response = apiFun()
            when (response.isSuccessful) {
                true -> return@async response.body()
                false -> return@async doResponseError(response)
            }
        }
        return apiResult.await()
    }

    private fun <T : BaseResult> doResponseError(response: Response<T>): T? {
        /*特殊處理 需採用判斷 response code */
        val url = response.raw().request.url.toString()
        if (response.code() == HttpError.GO_TO_SERVICE_PAGE.code && url.contains(Constants.INDEX_CONFIG)) {
            _errorResultIndex.postValue(response.raw().request.url.host.httpFormat())
            return null
        }

        val errorResult = ErrorUtils.parseError(response)
        val errorList = mutableListOf(
            HttpError.BALANCE_IS_LOW.code,
            HttpError.UNAUTHORIZED.code,
            HttpError.KICK_OUT_USER.code,
            HttpError.MAINTENANCE.code
        )
        errorResult?.let {
            if (errorList.contains(it.code)) {
                _errorResultToken.postValue(Event(it))
            }
        }
        return errorResult
    }

    fun doLogoutAPI() {
        viewModelScope.launch {
            runCatching { loginRepository.logoutAPI() }
        }
    }
    fun doCleanToken() {
        viewModelScope.launch {
            betInfoRepository.clear()
            infoCenterRepository.clear()
            loginRepository.logout()
            BackService.cleanUserChannel()
        }
    }

    fun doLogoutCleanUser(finishFunction: () -> Unit) {
        viewModelScope.launch {
            betInfoRepository.clear()
            infoCenterRepository.clear()
            loginRepository.logout()
            //退出登入後盤口回到預設
            updateDefaultHandicapType()
            finishFunction.invoke()
        }
    }

    fun checkIsUserAlive() {
        viewModelScope.launch {
            doNetwork(MultiLanguagesApplication.appContext) {
                loginRepository.checkIsUserAlive()
            }.let { result ->
                if (result?.success == false && loginRepository.isLogin.value == true) {
                    loginRepository._kickedOut.value = Event(result.msg)
                }
            }
        }
    }

    fun getLoginBoolean(): Boolean {
        return loginRepository.isLogin.value ?: false
    }


    fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(block = block)
    }
}