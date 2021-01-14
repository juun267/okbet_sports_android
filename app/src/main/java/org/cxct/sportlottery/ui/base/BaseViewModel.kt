package org.cxct.sportlottery.ui.base

import androidx.annotation.Nullable
import androidx.lifecycle.*
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

    @Nullable
    suspend fun <T> doNetwork(apiFun: suspend () -> Response<T>): T? {
        val apiResult = viewModelScope.async {
            try {
                val response = apiFun()

                if (response.isSuccessful) {
                    return@async response.body()
                } else {
                    val errorResult = ErrorUtils.parseError(response)

                    _networkException.postValue(errorResult == null)

                    _code.postValue((errorResult as BaseResult).code)
                    return@async errorResult
                }
            } catch (e: Exception) {
                e.printStackTrace()

                _networkException.postValue(true)
                return@async null
            }
        }

        return apiResult.await()
    }
}