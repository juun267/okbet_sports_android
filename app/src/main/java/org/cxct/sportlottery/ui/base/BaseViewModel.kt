package org.cxct.sportlottery.ui.base

import androidx.lifecycle.*
import kotlinx.coroutines.launch
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

    fun <T> doNetwork(apiFun: suspend () -> Response<T>): LiveData<T> {
        val result = MutableLiveData<T>()

        viewModelScope.launch {
            try {
                val response = apiFun()

                if (response.isSuccessful) {
                    result.postValue(response.body())
                } else {
                    val errorResult = ErrorUtils.parseError(response)

                    _networkException.postValue(errorResult == null)

                    errorResult?.let {
                        _code.postValue((it as BaseResult).code)
                        result.postValue(it)
                    }
                }
            } catch (e: Exception) {
                _networkException.postValue(true)
            }
        }
        return result
    }
}