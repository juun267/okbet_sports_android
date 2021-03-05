package org.cxct.sportlottery.ui.maintenance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.splash.HostRepository
import timber.log.Timber


class MaintenanceViewModel(
    private val androidContext: Context,
    private val hostRepository: HostRepository
) : BaseViewModel() {


    val configResult: LiveData<ConfigResult?>
        get() = _configResult
    private val _configResult = MutableLiveData<ConfigResult?>()

    fun getConfig() {
        val hostUrl = hostRepository.hostUrl
        hostRepository.isNeedGetHost = true

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
        _configResult.postValue(result)
    }

}