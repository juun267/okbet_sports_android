package org.cxct.sportlottery.ui.base

import androidx.lifecycle.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import retrofit2.Response
import java.lang.Exception


abstract class BaseViewModel : ViewModel() {
    val code: LiveData<Int>
        get() = _code
    val networkException: LiveData<Boolean>
        get() = _networkException

    private val _code = MutableLiveData<Int>()
    private val _networkException = MutableLiveData<Boolean>()

    suspend fun <T> doNetwork(apiFun: suspend () -> Response<T>): T {
        val apiResult = viewModelScope.async {
            try {
                val response = apiFun()

                if (response.isSuccessful) {
                    return@async response.body()
                } else {
                    val errorResult = ErrorUtils.parseError(response)

                    _networkException.postValue(errorResult == null)

                    errorResult?.let {
                        _code.postValue((it as BaseResult).code)
                        return@async it
                    }
                }
            } catch (e: Exception) {
                _networkException.postValue(true)
            }
        }

        @Suppress("UNCHECKED_CAST")
        return (apiResult as Deferred<T>).await()
    }
}