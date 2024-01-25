package org.cxct.sportlottery.ui.maintenance

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.setupDefaultHandicapType


class MaintenanceViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {


    val configResult: LiveData<ConfigResult?>
        get() = _configResult
    var _configResult = MutableLiveData<ConfigResult?>()

    //获取配置文件
    fun getConfig() {
        val hostUrl = HostRepository.hostUrl

        viewModelScope.launch {
            if (hostUrl.isNotEmpty()) {
                val retrofit = RequestManager.instance.createRetrofit(hostUrl)
                val result = doNetwork(androidContext) {
                    retrofit.create(IndexService::class.java).getConfig()
                }
                if (result?.success == true) {
                    setConfig(result)
                    return@launch
                }
            }
        }
    }

    private fun setConfig(result: ConfigResult?) {
        sConfigData = result?.configData
        setupDefaultHandicapType()
        ConfigRepository.config.postValue(result)
        _configResult.postValue(result)
    }

}