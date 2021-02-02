package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class VersionUpdateViewModel : BaseViewModel() {

    private val _appVersionState = MutableLiveData<AppVersionState>()

    val appVersionState: LiveData<AppVersionState>
        get() = _appVersionState

    //獲取 版本更新 API url，當 call API 失敗時，就使用下一順位的 serverUrl，重新 request，直到遍歷 ServerUrlList，或成功獲取 checkAppUpdate() 即停止
    private var mServerUrlIndex = 0
    private fun getNextCheckAppUpdateUrl(index: Int): String {
        return if (index in Constants.SERVER_URL_LIST.indices) {
            val serverUrl = Constants.SERVER_URL_LIST[index]
            Constants.currentServerUrl = serverUrl //紀錄當前選擇的 serverUrl
            Constants.getCheckAppUpdateUrl(serverUrl)
        } else ""
    }


    fun checkAppVersion() {
        viewModelScope.launch {
            mServerUrlIndex = 0
            check()
        }

    }

    private suspend fun check() {
        try {
            val url = getNextCheckAppUpdateUrl(mServerUrlIndex)
            val response = OneBoSportApi.appUpdateService.checkAppVersion(url)
            val result = response.body()
            when {
                response.isSuccessful && result != null -> compareVersion(result)
                else -> throw Exception(response.errorBody().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (++mServerUrlIndex in Constants.SERVER_URL_LIST.indices)
                check()
            else
                _appVersionState.postValue(AppVersionState(false, BuildConfig.VERSION_CODE.toString(), BuildConfig.VERSION_NAME))
        }
    }

    private fun compareVersion(result: CheckAppVersionResult) {
        var isNewVersionCode = false

        val versionList: ArrayList<Int> = arrayListOf()
        val androidVersionCode = result.version?.split("_")?.get(0) ?: ""
        val androidVersionName = result.version?.split("_")?.get(1) ?: ""
        versionList.add(if (androidVersionCode.isBlank()) 0 else androidVersionCode.toInt())
        androidVersionName.split(".").forEach {
            versionList.add(if (it.isBlank()) 0 else it.toInt())
        }

        val localVersionList: ArrayList<Int> = arrayListOf()
        localVersionList.add(BuildConfig.VERSION_CODE)
        BuildConfig.VERSION_NAME.split(".").forEach {
            localVersionList.add(if (it.isBlank()) 0 else it.toInt())
        }

        if (versionList.size == localVersionList.size) {
            for (i in versionList.indices) {
                if (versionList[i] > localVersionList[i]) {
                    isNewVersionCode = true
                    break
                } else if (versionList[i] < localVersionList[i]) {
                    break
                }
            }
        }

        _appVersionState.postValue(AppVersionState(isNewVersionCode, androidVersionCode, androidVersionName))
    }
}