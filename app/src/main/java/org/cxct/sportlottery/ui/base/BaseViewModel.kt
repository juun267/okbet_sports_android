package org.cxct.sportlottery.ui.base

import android.content.Context
import androidx.annotation.Nullable
import androidx.lifecycle.*
import kotlinx.coroutines.async
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.util.NetworkUtil
import retrofit2.Response
import java.lang.Exception


abstract class BaseViewModel : ViewModel() {
    val code: LiveData<Int>
        get() = _code

    val networkException: LiveData<Boolean>
        get() = _networkException

    val networkAvailable: LiveData<Boolean>
        get() = _networkAvailable

    private val _code = MutableLiveData<Int>()
    private val _networkException = MutableLiveData<Boolean>()
    private val _networkAvailable = MutableLiveData<Boolean>()

    @Nullable
    suspend fun <T> doNetwork(context: Context, apiFun: suspend () -> Response<T>): T? {
        return when (NetworkUtil.isAvailable(context)) {
            true -> {
                doApiFun(apiFun)
            }
            false -> {
                doNoConnect()
            }
        }
    }

    private suspend fun <T> doApiFun(apiFun: suspend () -> Response<T>): T? {
        val apiResult = viewModelScope.async {
            try {
                val response = apiFun()

                when (response.isSuccessful) {
                    true -> return@async response.body()
                    false -> return@async doResponseError(response)
                }

            } catch (e: Exception) {
                return@async doUnknownException(e)
            }
        }

        return apiResult.await()
    }

    private fun <T> doNoConnect(): T? {
        _networkAvailable.postValue(false)
        return null
    }

    private fun <T> doResponseError(response: Response<T>): T {
        val errorResult = ErrorUtils.parseError(response)
        _code.postValue((errorResult as BaseResult).code)
        return errorResult
    }

    private fun <T> doUnknownException(exception: Exception): T? {
        _networkException.postValue(true)
        return null
    }
}