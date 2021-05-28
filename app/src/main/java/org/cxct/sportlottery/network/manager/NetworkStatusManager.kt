package org.cxct.sportlottery.network.manager

import android.app.Application
import org.cxct.sportlottery.util.NetworkUtil


class NetworkStatusManager private constructor(val context: Application) {

    companion object {
        private var instance: NetworkStatusManager? = null
        private var context: Application? = null

        fun init(context: Application) {
            this.context = context
        }

        fun getInstance(): NetworkStatusManager {
            if (context == null) {
                throw RuntimeException("You must initialize this manager before getting instance")
            }
            if (instance == null) {
                instance = NetworkStatusManager(context!!)
            }
            return instance!!
        }
    }

    fun isUnavailable(): Boolean {
        return !NetworkUtil.isAvailable(context)
    }
}