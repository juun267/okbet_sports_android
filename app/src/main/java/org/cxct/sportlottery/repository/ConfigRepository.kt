package org.cxct.sportlottery.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.index.config.ConfigResult

object ConfigRepository {

    val config = MutableLiveData<ConfigResult?>()

    fun onNewConfig(lifecycleOwner: LifecycleOwner, block: (ConfigResult?) -> Unit) {
        config.observe(lifecycleOwner) { block.invoke(it) }
    }
}