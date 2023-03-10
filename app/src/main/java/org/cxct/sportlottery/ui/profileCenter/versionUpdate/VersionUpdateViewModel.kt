package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.archit.calendardaterangepicker.manager.SPUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.exception.DoNoConnectException
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.NetworkUtil
import timber.log.Timber

class VersionUpdateViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

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
        viewModelScope.launch(Dispatchers.IO) {
            mServerUrlIndex = 0
            check {
                compareVersion(it)
            }
        }
    }

    fun checkAppMinVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            mServerUrlIndex = 0
            check {
                compareMinVersion(it)
            }
        }
    }

    private fun check(compareFun: (CheckAppVersionResult) -> Unit) {
        var appVersionChecked = false //是否已完成版本檢查

        //Map<伺服器網址, 是否已檢查過>
        val serverUrlStatusMap: MutableMap<String, Boolean> =
            Constants.SERVER_URL_LIST.associateWith { false }.toMutableMap()

        serverUrlStatusMap.toMap().forEach { (serverUrl, status) ->
            val url = Constants.getCheckAppUpdateUrl(serverUrl)
            viewModelScope.launch {
                try {
                    if (!NetworkUtil.isAvailable(androidContext))
                        throw DoNoConnectException()
                    val response = OneBoSportApi.appUpdateService.checkAppVersion(url)
                    val result = response.body()

                    serverUrlStatusMap[serverUrl] = true //標記該伺服器已檢查過
                    //当前版本是否处于控制
                    var isVersonControl =
                        result?.controlVersion?.split(",")?.contains(BuildConfig.VERSION_NAME)
                    SPUtil.saveMarketSwitch(isVersonControl == true)
                    //已有獲取的最新版本資訊
                    if (appVersionChecked) {
                        return@launch
                    } else {
                        when {
                            response.isSuccessful && result != null -> {
                                appVersionChecked = true
                                Constants.currentServerUrl = serverUrl //紀錄成功獲取檢查版本的 serverUrl
                                Constants.currentFilename = result.fileName //記錄成功獲取版本的apk name
                                compareFun(result)
                            }
                            else -> {
                                //如果每一個伺服器網址都已檢查過
                                if (serverUrlStatusMap.all { it.value }) {
                                    _appVersionState.postValue(
                                        AppVersionState(
                                            false,
                                            BuildConfig.VERSION_CODE.toString(),
                                            BuildConfig.VERSION_NAME
                                        )
                                    )

                                    val version =
                                        "${BuildConfig.VERSION_CODE}_${BuildConfig.VERSION_NAME}"
                                    _appMinVersionState.postValue(
                                        AppMinVersionState(
                                            isShowUpdateDialog = false,
                                            isForceUpdate = false,
                                            version = version,
                                            checkAppVersionResult = result
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e("checkAppVersion exception e: ${e.message}")
                    return@launch
                }
            }
        }
    }

    private fun compareVersion(result: CheckAppVersionResult) {
        val isNewVersionCode = judgeNewVersion(result)
        val versionList = result.version?.split("_")
        val androidVersionCode = versionList?.get(0) ?: ""
        val androidVersionName = versionList?.get(1) ?: ""
        _appVersionState.postValue(
            AppVersionState(
                isNewVersionCode,
                androidVersionCode,
                androidVersionName
            )
        )
    }

    private fun compareMinVersion(result: CheckAppVersionResult) {
        val after72Hours = (System.currentTimeMillis() - lastShowUpdateTime) > (24 * 60 * 60 * 1000)

        //當 check = 1 時，才比較全部版號，否則比較只比較大版號
        val isNewVersionCode = if (result.check == FLAG_OPEN) judgeNewVersion(result) else judgeBigCodeVersion(result)
        val isShowUpdateDialog = after72Hours && isNewVersionCode
        val isForceUpdate = judgeForceUpdate(result)
        _appMinVersionState.postValue(
            AppMinVersionState(
                isShowUpdateDialog,
                isForceUpdate,
                result.version ?: "",
                result
            )
        )
    }

    private fun judgeForceUpdate(result: CheckAppVersionResult): Boolean {
        val minVersionList: ArrayList<String> = arrayListOf()
        val miniVersion = result.miniVersion?.split("_")
        val minVersionCode = miniVersion?.get(0) ?: ""
        val minVersionName = miniVersion?.get(1) ?: ""
        minVersionList.add(minVersionCode.ifBlank { "0" })
        minVersionName.split(".").forEach {
            minVersionList.add(it.ifBlank { "0" })
        }

        val localVersionList: ArrayList<String> = arrayListOf()
        localVersionList.add(BuildConfig.VERSION_CODE.toString())
        BuildConfig.VERSION_NAME.split(".").forEach {
            localVersionList.add(it.ifBlank { "0" })
        }

        if (minVersionList.size <= localVersionList.size) {
            for (i in minVersionList.indices) {
                val minVersion = minVersionList[i].toLongOrNull() ?: 0
                val localVersion = localVersionList[i].toLongOrNull() ?: 0
                if (minVersion > localVersion) {
                    return true
                } else if (minVersion < localVersion) {
                    break
                }
            }
        }

        return false
    }

    private fun judgeNewVersion(result: CheckAppVersionResult): Boolean {
        val versionList: ArrayList<String> = arrayListOf()
        val version = result.version?.split("_")
        val androidVersionCode = version?.get(0) ?: ""
        val androidVersionName = version?.get(1) ?: ""
        versionList.add(androidVersionCode.ifBlank { "0" })
        androidVersionName.split(".").forEach {
            versionList.add(it.ifBlank { "0" })
        }

        val localVersionList: ArrayList<String> = arrayListOf()
        localVersionList.add(BuildConfig.VERSION_CODE.toString())
        BuildConfig.VERSION_NAME.split(".").forEach {
            localVersionList.add(it.ifBlank { "0" })
        }

        if (versionList.size <= localVersionList.size) {
            for (i in versionList.indices) {
                val version = versionList[i].toLongOrNull() ?: 0
                val localVersion = localVersionList[i].toLongOrNull() ?: 0
                if (version > localVersion) {
                    return true
                } else if (version < localVersion) {
                    break
                }
            }
        }

        return false
    }

    private fun judgeBigCodeVersion(result: CheckAppVersionResult): Boolean {
        val androidBigCode = result.version?.split("_")?.firstOrNull()?.toInt() ?: 0
        val localBigCode = BuildConfig.VERSION_CODE
        return androidBigCode > localBigCode
    }

}