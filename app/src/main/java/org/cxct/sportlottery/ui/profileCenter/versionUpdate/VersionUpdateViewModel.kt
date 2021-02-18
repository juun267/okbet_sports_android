package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.ui.base.BaseViewModel

class VersionUpdateViewModel(
    private val androidContext: Context
) : BaseViewModel() {

    companion object {
        const val KEY_LAST_SHOW_UPDATE_TIME = "key-last-show-update-time"
    }

    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    //存取最後一次提醒 APP Update 的時間，若下次提醒間隔不到 72 小時則不提醒
    var lastShowUpdateTime: Long
        get() = sharedPref.getLong(KEY_LAST_SHOW_UPDATE_TIME, 0)
        set(value) {
            with(sharedPref.edit()) {
                putLong(KEY_LAST_SHOW_UPDATE_TIME, value)
                commit()
            }
        }

    private val _appVersionState = MutableLiveData<AppVersionState>()
    val appVersionState: LiveData<AppVersionState>
        get() = _appVersionState


    private val _appMinVersionState = MutableLiveData<AppMinVersionState>()
    val appMinVersionState: LiveData<AppMinVersionState>
        get() = _appMinVersionState


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
            check {
                compareVersion(it)
            }
        }
    }

    fun checkAppMinVersion() {
        viewModelScope.launch {
            mServerUrlIndex = 0
            check {
                compareMinVersion(it)
            }
        }
    }

    private suspend fun check(compareFun: (CheckAppVersionResult) -> Unit) {
        try {
            val url = getNextCheckAppUpdateUrl(mServerUrlIndex)
            val response = OneBoSportApi.appUpdateService.checkAppVersion(url)
            val result = response.body()
            when {
                response.isSuccessful && result != null -> compareFun(result)
                else -> throw Exception(response.errorBody().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (++mServerUrlIndex in Constants.SERVER_URL_LIST.indices)
                check(compareFun)
            else
                _appVersionState.postValue(AppVersionState(false, BuildConfig.VERSION_CODE.toString(), BuildConfig.VERSION_NAME))
        }
    }

    private fun compareVersion(result: CheckAppVersionResult) {
        val isNewVersionCode = judgeNewVersion(result)
        val versionList = result.version?.split("_")
        val androidVersionCode = versionList?.get(0) ?: ""
        val androidVersionName = versionList?.get(1) ?: ""
        _appVersionState.postValue(AppVersionState(isNewVersionCode, androidVersionCode, androidVersionName))
    }

    private fun compareMinVersion(result: CheckAppVersionResult) {
        val after72Hours = System.currentTimeMillis() - lastShowUpdateTime > 24 * 60 * 60 * 1000
        val isShowUpdateDialog = after72Hours && result.check == FLAG_OPEN && judgeNewVersion(result)
        val isForceUpdate = judgeForceUpdate(result)
        _appMinVersionState.postValue(AppMinVersionState(isShowUpdateDialog, isForceUpdate, result.version?: ""))
    }

    private fun judgeForceUpdate(result: CheckAppVersionResult): Boolean {
        val minVersionList: ArrayList<Int> = arrayListOf()
        val minVersionCode = result.miniVersion?.split("_")?.get(0) ?: ""
        val minVersionName = result.miniVersion?.split("_")?.get(1) ?: ""
        minVersionList.add(if (minVersionCode.isBlank()) 0 else minVersionCode.toInt())
        minVersionName.split(".").forEach {
            minVersionList.add(if (it.isBlank()) 0 else it.toInt())
        }

        val localVersionList: ArrayList<Int> = arrayListOf()
        localVersionList.add(BuildConfig.VERSION_CODE)
        BuildConfig.VERSION_NAME.split(".").forEach {
            localVersionList.add(if (it.isBlank()) 0 else it.toInt())
        }

        if (minVersionList.size == localVersionList.size) {
            for (i in minVersionList.indices) {
                if (minVersionList[i] > localVersionList[i]) {
                    return true
                } else if (minVersionList[i] < localVersionList[i]) {
                    break
                }
            }
        }

        return false
    }

    private fun judgeNewVersion(result: CheckAppVersionResult): Boolean {
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
                    return true
                } else if (versionList[i] < localVersionList[i]) {
                    break
                }
            }
        }

        return false
    }
}