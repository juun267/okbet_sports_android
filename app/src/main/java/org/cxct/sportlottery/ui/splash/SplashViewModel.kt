package org.cxct.sportlottery.ui.splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.httpFormat
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.playquotacom.PlayQuotaComResult
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import retrofit2.Retrofit
import timber.log.Timber
import kotlin.random.Random

class SplashViewModel(
    private val androidContext: Context,
    private val hostRepository: HostRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val userInfoRepository: UserInfoRepository,
    private val playQuotaComRepository: PlayQuotaComRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val playQuotaCom: LiveData<PlayQuotaComResult>
        get() = playQuotaComRepository._playquotacom

    //當獲取 host 失敗時，就使用下一順位的 serverUrl，重新 request，直到遍歷 ServerUrlList，或成功獲取 host 即停止
    private var mServerUrlIndex = 0
    private var mCheckHostUrlCount = 0 //已經完成 check host 的數量
    private var mIsGetFastHostDown = false //get host 流程結束
    private var mAppUrlList: List<String>? = null

    val configResult: LiveData<ConfigResult?>
        get() = _configResult

    val skipHomePage: LiveData<Boolean>
        get() = _skipHomePage

    private val _configResult = MutableLiveData<ConfigResult?>()

    private val _skipHomePage = MutableLiveData<Boolean>()

    /**
     * 1. 先讀取 local 端儲存的 host 使用 getConfig() 檢查，看可不可用
     * 2. 可用跳轉畫面; 不可用再執行 getHost()
     * 3. getConfig() 後判斷是否維護中，一切成功跳轉畫面
     * 4. 若使使用 local host 進入，跳轉畫面後也要重新 getHost() 一次，再比對有無更新
     *
     * P.S 不管 getHost() 有無成功，其他 web server API 都使用 getBaseUrl() 來獲取 host
     */
    fun checkLocalHost() {
        val hostUrl = hostRepository.hostUrl
        hostRepository.isNeedGetHost = true


        viewModelScope.launch {
            if (hostUrl.isNotEmpty()) {
                Timber.i("==> checkLocalHost: $hostUrl")
                val retrofit = RequestManager.instance.createRetrofit(hostUrl.httpFormat())
                val result = doNetwork(androidContext, exceptionHandle = false) {
                    retrofit.create(IndexService::class.java).getConfig()
                }
                if (result?.success == true) {
                    setConfig(result)
                    gotConfigData = true
                    setBaseUrl(hostUrl, retrofit)
                    result.configData?.let { setRandomSocketUrl(it.wsHost) }
                    getPlayQuotaCom()
                    return@launch
                } else {
                    getHost()
                }
            } else {
                getHost()
            }
            //確定localHost無法獲取config後才去獲取其他域名
//            getHost()
        }
    }

    private fun getHostListUrl(index: Int): String {
        return if (index in Constants.SERVER_URL_LIST.indices) {
            val serverUrl = Constants.SERVER_URL_LIST[index]
            Constants.getHostListUrl(serverUrl)
        } else ""
    }

    fun getHost() {
        Timber.i("==> getHost")
        viewModelScope.launch {
            mServerUrlIndex = 0
            sendGetHostRequest(mServerUrlIndex)
        }
    }

    fun goNextPage() {
        if (sConfigData?.thirdOpen != FLAG_OPEN)
            viewModelScope.launch {
                loginRepository.checkToken()

                if (!userInfoRepository.checkedUserInfo && isLogin.value == true) {
                    userInfoRepository.getUserInfo()
                }
            } else {
            _skipHomePage.postValue(false)
        }
    }

    private suspend fun sendGetHostRequest(index: Int) {
        try {
            val url = getHostListUrl(index)
            Timber.i("==> sendGetHostRequest: $url")
            val response = OneBoSportApi.hostService.getHost(url)
            val result = response.body()
            when {
                response.isSuccessful && result?.success == true && result.rows.isNotEmpty() -> {
                    mAppUrlList = result.rows
                    mCheckHostUrlCount = 0
                    mIsGetFastHostDown = false

                    mAppUrlList?.forEach { hostUrl ->
                        checkHostByGettingConfig(hostUrl)
                    }
                }
                else -> throw Exception(response.errorBody().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (++mServerUrlIndex in Constants.SERVER_URL_LIST.indices)
                sendGetHostRequest(mServerUrlIndex)
            else
                _configResult.postValue(null)
        }
    }

    //20210209 記錄問題：每次都要 create 新的 retrofit，才能init正確的 baseUrl，於最後確定要使用的 baseUrl 去替換相對應的 retrofit 實體
    private fun checkHostByGettingConfig(baseUrl: String) {
        viewModelScope.launch {
            Timber.i("==> checkHostByGettingConfig: $baseUrl")
            val retrofit = RequestManager.instance.createRetrofit(baseUrl.httpFormat())
            val result = doNetwork(androidContext, exceptionHandle = false) {
                val indexService = retrofit.create(IndexService::class.java)
                indexService.getConfig()
            }

            if (mIsGetFastHostDown) //其中一個 check host 成功，其他的 result 都不用執行
                return@launch

            if (result?.success == true) {
                Timber.i("==> Check host success!!! baseUrl = $baseUrl")
                mIsGetFastHostDown = true
                hostRepository.isNeedGetHost = false
                setConfig(result)
                gotConfigData = true
                setBaseUrl(baseUrl, retrofit)
                result.configData?.let { setRandomSocketUrl(it.wsHost) }
                getPlayQuotaCom()
            } else {
                Timber.e("==> Check host fail!!! baseUrl = $baseUrl")
                val listSize = mAppUrlList?.size ?: 0
                if (++mCheckHostUrlCount >= listSize) { //當所有的 check request 都失敗才跳 error
                    setConfig(result)
                    gotConfigData = false
                }
            }
        }
    }

    private fun setConfig(result: ConfigResult?) {
        hostRepository.platformId = result?.configData?.platformId ?: -1
        sConfigData = result?.configData
        _configResult.postValue(result)
    }

    private fun setBaseUrl(baseUrl: String, retrofit: Retrofit) {
        Timber.i("Final choice host: $baseUrl")
        hostRepository.hostUrl = baseUrl
        Constants.setBaseUrl(baseUrl)
        RequestManager.instance.retrofit = retrofit
    }

    private fun setRandomSocketUrl(wsHost: String) {
        val wsList = wsHost.split(',')
        val randomIndex = Random.nextInt(wsList.size)
        Constants.setSocketUrl(wsList[randomIndex])
    }

    fun isNeedGetHost(): Boolean {
        return hostRepository.isNeedGetHost
    }

    private fun getPlayQuotaCom() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                playQuotaComRepository.getPlayQuotaCom()
            }
            result?.let {
                playQuotaComRepository.postPlayQuotaCom(it)
                betInfoRepository.playQuotaComData = it.playQuotaComData
            }
        }
    }

}