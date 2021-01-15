package org.cxct.sportlottery.ui.splash

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel

class SplashViewModel(
    private val androidContext: Context,
) : BaseViewModel() {

    val configResult: LiveData<ConfigResult?>
        get() = _configResult
    private val _configResult = MutableLiveData<ConfigResult?>()

    fun getConfig() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.getConfig()
            }
            sConfigData = result?.configData
            _configResult.postValue(result)
        }
    }

}