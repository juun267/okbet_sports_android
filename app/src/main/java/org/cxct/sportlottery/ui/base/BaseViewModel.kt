package org.cxct.sportlottery.ui.base

import android.content.Context
import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.NetworkUtil
import retrofit2.Response
import java.net.SocketTimeoutException


abstract class BaseViewModel(
    val loginRepository: LoginRepository,
    val betInfoRepository: BetInfoRepository,
    val infoCenterRepository: InfoCenterRepository
) : ViewModel() {
    val errorResultToken: LiveData<BaseResult>
        get() = _errorResultToken

    val networkUnavailableMsg: LiveData<String>
        get() = _networkUnavailableMsg

    val networkExceptionTimeout: LiveData<String>
        get() = _networkExceptionTimeout

    val networkExceptionUnknown: LiveData<Exception>
        get() = _networkExceptionUnknown

    private val _errorResultToken = MutableLiveData<BaseResult>()
    private val _networkUnavailableMsg = MutableLiveData<String>()
    private val _networkExceptionTimeout = MutableLiveData<String>()
    private val _networkExceptionUnknown = MutableLiveData<Exception>()

    @Nullable
    suspend fun <T : BaseResult> doNetwork(
        context: Context,
        apiFun: suspend () -> Response<T>
    ): T? {
        return when (NetworkUtil.isAvailable(context)) {
            true -> {
                doApiFun(context, apiFun)
            }
            false -> {
                doNoConnect(context)
            }
        }
    }

    private suspend fun <T : BaseResult> doApiFun(
        context: Context,
        apiFun: suspend () -> Response<T>
    ): T? {
        val apiResult = viewModelScope.async {
            try {
                val response = apiFun()
                when (response.isSuccessful) {
                    true -> return@async response.body()
                    false -> return@async doResponseError(response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return@async doOnException(context, e)
            }
        }

        return apiResult.await()
    }

    private fun <T : BaseResult> doNoConnect(context: Context): T? {
        _networkUnavailableMsg.postValue(context.getString(R.string.message_network_no_connect))
        return null
    }

    private fun <T : BaseResult> doResponseError(response: Response<T>): T? {
        val errorResult = ErrorUtils.parseError(response)
        if (response.code() == HttpError.UNAUTHORIZED.code) {
            errorResult?.let {
                _errorResultToken.postValue(it)
            }
        }
        return errorResult
    }

    fun doLogoutCleanUser() {
        viewModelScope.launch {
            loginRepository.clear()
            betInfoRepository.clear()
            infoCenterRepository.clear()
            loginRepository.logout()
        }
    }

    private fun <T : BaseResult> doOnException(context: Context, exception: Exception): T? {
        when (exception) {
            is SocketTimeoutException -> doOnTimeOutException(context)
            else -> doOnUnknownException(exception)
        }
        return null
    }

    private fun doOnTimeOutException(context: Context) {
        _networkExceptionTimeout.postValue(context.getString(R.string.message_network_timeout))
    }

    private fun doOnUnknownException(exception: Exception) {
        _networkExceptionUnknown.postValue(exception)
    }
}