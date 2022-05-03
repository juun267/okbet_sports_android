package org.cxct.sportlottery.ui.base

import android.content.Context
import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.exception.DoNoConnectException
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.index.checktoken.CheckTokenResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.NetworkUtil
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException


abstract class BaseViewModel(
    val loginRepository: LoginRepository,
    val betInfoRepository: BetInfoRepository,
    val infoCenterRepository: InfoCenterRepository
) : ViewModel() {
    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin
    }

    val isKickedOut: LiveData<Event<String?>> by lazy {
        loginRepository.kickedOut
    }

    val errorResultToken: LiveData<BaseResult>
        get() = _errorResultToken

    val networkExceptionUnavailable: LiveData<String>
        get() = _networkExceptionUnavailable

    val networkExceptionTimeout: LiveData<String>
        get() = _networkExceptionTimeout

    val networkExceptionUnknown: LiveData<String>
        get() = _networkExceptionUnknown

    private val _errorResultToken = MutableLiveData<BaseResult>()
    private val _networkExceptionUnavailable = MutableLiveData<String>()
    private val _networkExceptionTimeout = MutableLiveData<String>()
    private val _networkExceptionUnknown = MutableLiveData<String>()

    enum class NetWorkResponseType(val code: Int) {
        REQUEST_TOO_FAST(400)
    }

    //20210526 新增 exceptionHandle 參數，還判斷要不要在 BaseActivity 顯示，exception 錯誤訊息
    @Nullable
    suspend fun <T : BaseResult> doNetwork(
        context: Context,
        exceptionHandle: Boolean = true,
        apiFun: suspend () -> Response<T>
    ): T? {
        return try {
            if (!NetworkUtil.isAvailable(context))
                throw DoNoConnectException()
            doApiFun(apiFun)
        } catch (e: Exception) {
            Timber.e("doNetwork: $e")
            e.printStackTrace()
            if (exceptionHandle)
                doOnException(context, e)
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
        val errorResult = ErrorUtils.parseError(response)
        if (response.code() == HttpError.UNAUTHORIZED.code || response.code() == HttpError.KICK_OUT_USER.code ) {
            errorResult?.let {
                _errorResultToken.postValue(it)
            }
        }
        return errorResult
    }

    private fun doOnException(context: Context, exception: Exception) {
        when (exception) {
            is DoNoConnectException -> {
                _networkExceptionUnavailable.postValue(context.getString(R.string.message_network_no_connect))
            }
            is SocketTimeoutException -> {
                _networkExceptionTimeout.postValue(context.getString(R.string.message_network_timeout))
            }
            else -> {
                _networkExceptionUnknown.postValue(context.getString(R.string.message_network_no_connect))
            }
        }
    }

    fun doLogoutAPI() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }

    fun doLogoutCleanUser(finishFunction: () -> Unit) {
        viewModelScope.launch {
            betInfoRepository.clear()
            infoCenterRepository.clear()
            finishFunction.invoke()
        }
    }

    fun checkIsUserAlive() {
        viewModelScope.launch {
            doNetwork(MultiLanguagesApplication.appContext) {
               loginRepository.checkIsUserAlive()
            }.let{ result ->
                if (result?.success == false && loginRepository.isLogin.value == true){
                    loginRepository._kickedOut.value = Event(result.msg)
                }
            }
        }
    }
}