package org.cxct.sportlottery.ui.base

import android.accounts.NetworkErrorException
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

    val networkExceptionUnknown: LiveData<String>
        get() = _networkExceptionUnknown

    private val _errorResultToken = MutableLiveData<BaseResult>()
    private val _networkUnavailableMsg = MutableLiveData<String>()
    private val _networkExceptionTimeout = MutableLiveData<String>()
    private val _networkExceptionUnknown = MutableLiveData<String>()

    //20210526 新增 exceptionHandle 參數，還判斷要不要在 BaseActivity 顯示，exception 錯誤訊息
    @Nullable
    suspend fun <T : BaseResult> doNetwork(
        context: Context,
        exceptionHandle: Boolean = true,
        apiFun: suspend () -> Response<T>
    ): T? {
        return try {
            if (!NetworkUtil.isAvailable(context))
                throw NetworkErrorException()
            doApiFun(apiFun)
        } catch (e: Exception) {
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
        if (response.code() == HttpError.UNAUTHORIZED.code) {
            errorResult?.let {
                _errorResultToken.postValue(it)
            }
        }
        return errorResult
    }

    private fun doOnException(context: Context, exception: Exception){
        when (exception) {
            is NetworkErrorException -> doNoConnect(context)
            is SocketTimeoutException -> doOnTimeOutException(context)
            else -> doOnUnknownException(context)
        }
    }

    private fun doNoConnect(context: Context) {
        _networkUnavailableMsg.postValue(context.getString(R.string.message_network_no_connect))
    }

    private fun doOnTimeOutException(context: Context) {
        _networkExceptionTimeout.postValue(context.getString(R.string.message_network_timeout))
    }

    private fun doOnUnknownException(context: Context) {
        _networkExceptionUnknown.postValue(context.getString(R.string.message_network_no_connect))
    }


    fun doLogoutCleanUser(finishFunction: () -> Unit) {
        viewModelScope.launch {
            betInfoRepository.clear()
            infoCenterRepository.clear()
            loginRepository.logout()
            finishFunction.invoke()
        }
    }
}