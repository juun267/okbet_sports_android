package org.cxct.sportlottery.ui.splash

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.httpFormat
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.ConfigResource
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.setupDefaultHandicapType
import retrofit2.Retrofit
import timber.log.Timber
import kotlin.random.Random

class SplashViewModel(
    androidContext: Application
) : BaseViewModel(androidContext) {

    //當獲取 host 失敗時，就使用下一順位的 serverUrl，重新 request，直到遍歷 ServerUrlList，或成功獲取 host 即停止
    private var mCheckHostUrlCount = 0 //已經完成 check host 的數量
    private var mIsGetFastHostDown = false //get host 流程結束
    private var mAppUrlList: List<String> = listOf()
    private var isCheckNewHost = false

    val configResult = SingleLiveEvent<ConfigResult?>()

    val skipHomePage: LiveData<Boolean>
        get() = _skipHomePage

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
        val hostUrl = HostRepository.hostUrl

        viewModelScope.launch {
            if (hostUrl.isNotEmpty()) {
                Timber.i("==> checkLocalHost: $hostUrl")

                val retrofit =
                    RequestManager.instance.createRetrofit(hostUrl.httpFormat())

//                Timber.d("userInfo:${MultiLanguagesApplication.mInstance.userInfo.value}")
//                Timber.d("userInfo isLogin:${LoginRepository.isLogined()}")
//                Timber.d("userInfo isLoginValue:${LoginRepository.isLogin.value == true}")

                if (LoginRepository.hasToken()) {
                    val checkHostResult = doNetwork(androidContext, exceptionHandle = false) {
                        retrofit.create(IndexService::class.java).checkToken()
                    }
                    if (checkHostResult?.success == false) {
                        Timber.i("==> check token fail : do getHost")
                        LoginRepository.clear()
                        getHost()
                        return@launch
                    }
                }

                val result = doNetwork(androidContext, exceptionHandle = false) {
                    retrofit.create(IndexService::class.java).getConfig()
                } ?: return@launch

                if (result.success) {
                    setBaseUrl(hostUrl)
                    setConfig(result)
                    gotConfigData = true
                    result.configData?.let { setRandomSocketUrl(it.wsHost) }
                    isCheckNewHost = true
                    getHost()
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

    fun getConfig() {
        val hostUrl = HostRepository.hostUrl
        viewModelScope.launch {
            val retrofit =
                RequestManager.instance.createRetrofit(hostUrl.httpFormat())
            val result = doNetwork(androidContext, exceptionHandle = false) {
                retrofit.create(IndexService::class.java).getConfig()
            } ?: return@launch

            if (result.success) {
                setBaseUrl(hostUrl)
                setConfig(result)
                gotConfigData = true
                result.configData?.let { setRandomSocketUrl(it.wsHost) }
                return@launch
            } else {
            }
        }
    }

    fun getHost() = viewModelScope.launch {
        Timber.i("==> getHost")
        Constants.SERVER_URL_LIST.forEach {
            try {
                var url = Constants.getHostListUrl(it)
                Timber.i("==> sendGetHostRequest: $url")
                val response = OneBoSportApi.hostService.getHost(url)
                val result = response.body()
                if (response.isSuccessful && result?.success == true && result.rows.isNotEmpty()) {
                    mAppUrlList = result.rows
                    mCheckHostUrlCount = 0
                    mIsGetFastHostDown = false

                    mAppUrlList.forEach { hostUrl ->
                        checkHostByGettingConfig(hostUrl)
                    }
                    return@launch
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (!isCheckNewHost) {
            configResult.postValue(null)
        }
    }

    fun goNextPage() = viewModelScope.launch {
            if (!UserInfoRepository.checkedUserInfo && LoginRepository.isLogined()) {
                runWithCatch { UserInfoRepository.getUserInfo() }
            }
             _skipHomePage.postValue(true)
        }


    //20210209 記錄問題：每次都要 create 新的 retrofit，才能init正確的 baseUrl，於最後確定要使用的 baseUrl 去替換相對應的 retrofit 實體
    private fun checkHostByGettingConfig(baseUrl: String) {
        viewModelScope.launch {
            Timber.i("==> checkHostByGettingConfig: $baseUrl")

            if (mIsGetFastHostDown) //其中一個 check host 成功，其他的 result 都不用執行
                return@launch

            val retrofit = RequestManager.instance.createRetrofit(baseUrl.httpFormat())

            if (LoginRepository.hasToken()) {
                val checkHostResult = doNetwork(androidContext, exceptionHandle = false) {
                    retrofit.create(IndexService::class.java).checkToken()
                }
                if (checkHostResult?.success == false) {
                    Timber.i("==> check token fail : do getHost")
                    LoginRepository.clear()
                    getHost()
                    return@launch
                }
            }

            val result = doNetwork(androidContext, exceptionHandle = false) {
                retrofit.create(IndexService::class.java).getConfig()
            }

            if (result?.success == true) {
                Timber.i("==> Check host success!!! baseUrl = $baseUrl")
                mIsGetFastHostDown = true
                setStoreBaseUrl(baseUrl)
                if (!isCheckNewHost) {
                    setBaseUrl(baseUrl)
                    setConfig(result)
                    gotConfigData = true
                    result.configData?.let { setRandomSocketUrl(it.wsHost) }
                }
            } else {
                Timber.e("==> Check host fail!!! baseUrl = $baseUrl")
                val listSize = mAppUrlList.size
                if (++mCheckHostUrlCount >= listSize) { //當所有的 check request 都失敗才跳 error
                    if (!isCheckNewHost) {
                        setConfig(result)
                        gotConfigData = false
                    }
                }
            }
        }
    }

    private fun setConfig(result: ConfigResult?) {
        val configData = result?.configData
        HostRepository.platformId = configData?.platformId ?: -1
        sConfigData = configData
        configData?.let { ConfigResource.preloadResource(it) }
        setupDefaultHandicapType()
        configResult.postValue(result)
        ConfigRepository.config.postValue(result)
    }

    private fun setBaseUrl(baseUrl: String) {
        Timber.i("Final choice host: $baseUrl")
        Constants.setBaseUrl(baseUrl)
        RetrofitHolder.changeHost(baseUrl)
    }

    private fun setStoreBaseUrl(baseUrl: String) {
        Timber.i("Final choice store host: $baseUrl")
        HostRepository.hostUrl = baseUrl
    }

    private fun setRandomSocketUrl(wsHost: String) {
        val wsList = wsHost.split(',')
        val randomIndex = Random.nextInt(wsList.size)
        Constants.setSocketUrl(wsList[randomIndex])
    }

}