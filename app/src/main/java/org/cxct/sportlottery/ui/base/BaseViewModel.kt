package org.cxct.sportlottery.ui.base

import android.content.Context
import androidx.annotation.Nullable
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.exception.DoNoConnectException
import org.cxct.sportlottery.common.extentions.clean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.httpFormat
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.money.RedEnvelopeResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.NetworkUtil
import org.cxct.sportlottery.util.updateDefaultHandicapType
import retrofit2.Response
import timber.log.Timber


abstract class BaseViewModel(
    val loginRepository: LoginRepository,
    val betInfoRepository: BetInfoRepository,
    val infoCenterRepository: InfoCenterRepository
) : ViewModel() {

    private lateinit var liveSet: HashMap<Class<*>, MutableLiveData<*>>

    @Synchronized
    private fun <T> getLiveData(clazz: Class<T>): MutableLiveData<T> {

        var liveData: MutableLiveData<T>? = null
        if (!::liveSet.isInitialized) {
            liveSet = HashMap()
            liveData = MutableLiveData<T>()
            liveSet.put(clazz, liveData)
            return liveData
        }

        liveData = liveSet.get(clazz) as MutableLiveData<T>?
        if (liveData == null) {
            liveData = MutableLiveData<T>()
            liveSet.put(clazz, liveData)
            return liveData
        }

        return liveData!!
    }

    fun <T> oberserve(lifecycleOwner: LifecycleOwner, clazz: Class<T>, oberver: Observer<T>) {
        getLiveData(clazz).observe(lifecycleOwner, oberver)
    }

    protected fun post(data: Any) {
        getLiveData(data::class.java).postValue(data as Nothing)
    }

    override fun onCleared() {
        if (::liveSet.isInitialized) {
            liveSet.values.forEach { it.clean() }
        }
    }

    private val _rainResult = MutableLiveData<Event<RedEnvelopeResult>>()
    val rainResult: LiveData<Event<RedEnvelopeResult>>
        get() = _rainResult

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin
    }

    val isKickedOut: LiveData<Event<String?>> by lazy {
        loginRepository.kickedOut
    }

    val errorResultIndex: LiveData<String>
        get() = _errorResultIndex

    val errorResultToken: LiveData<BaseResult>
        get() = _errorResultToken

    val networkExceptionUnavailable: LiveData<String>
        get() = _networkExceptionUnavailable

    val networkExceptionTimeout: LiveData<String>
        get() = _networkExceptionTimeout

    val networkExceptionUnknown: LiveData<String>
        get() = _networkExceptionUnknown

    private val _errorResultIndex = MutableLiveData<String>()
    private val _errorResultToken = MutableLiveData<BaseResult>()
    private val _networkExceptionUnavailable = MutableLiveData<String>()
    private val _networkExceptionTimeout = MutableLiveData<String>()
    private val _networkExceptionUnknown = MutableLiveData<String>()

    enum class NetWorkResponseType(val code: Int) {
        REQUEST_TOO_FAST(400)
    }

    fun <T : BaseResult> doRequest(
        context: Context, apiFun: suspend () -> Response<T>, callback: (T?) -> Unit
    ) {
        viewModelScope.launch/*(Dispatchers.IO)*/ {
            val result = doNetwork(context, true, apiFun)
            withContext(Dispatchers.Main) {
                callback.invoke(result)
            }
        }
    }

    //20210526 新增 exceptionHandle 參數，還判斷要不要在 BaseActivity 顯示，exception 錯誤訊息
    @Nullable
    suspend fun <T : BaseResult> doNetwork(
        context: Context, exceptionHandle: Boolean = true, apiFun: suspend () -> Response<T>
    ): T? {
        return try {
            if (!NetworkUtil.isAvailable(context)) throw DoNoConnectException()
            doApiFun(apiFun)
        } catch (e: Exception) {
            Timber.e("doNetwork: $e")
            e.printStackTrace()
            if (exceptionHandle) doOnException(context, e)
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
        if (errorResult?.code == HttpError.UNAUTHORIZED.code || errorResult?.code == HttpError.KICK_OUT_USER.code || errorResult?.code == HttpError.MAINTENANCE.code) {
            errorResult.let {
                _errorResultToken.postValue(it)
            }
        }
        return errorResult
    }

    private fun doOnException(context: Context, exception: Exception) {
        when (exception) {
            is kotlinx.coroutines.CancellationException -> {
                // 取消线程不执行业务
            }
            else -> {
                _networkExceptionUnavailable.postValue(context.getString(R.string.message_network_no_connect))
            }
        }
    }

    fun doLogoutAPI() {
        viewModelScope.launch {
            runCatching { loginRepository.logoutAPI() }
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

    fun getRain() {
        viewModelScope.launch {
            doNetwork(MultiLanguagesApplication.appContext) {
                OneBoSportApi.moneyService.getRainInfo()
            }?.let { result ->
                _rainResult.postValue(Event(result))
            }
        }
    }

    fun getLoginBoolean(): Boolean {
        return loginRepository.isLogin.value ?: false
    }




    val netCoroutine:CoroutineScope=viewModelScope
    fun onNet(block:suspend (coroutine:CoroutineScope)->Unit){
        viewModelScope.async (Dispatchers.IO) {
            block(this)
        }
    }


    fun onMain(block:suspend (coroutine:CoroutineScope)->Unit){
        viewModelScope.launch(Dispatchers.Main) {
            block(this)
        }
    }
}