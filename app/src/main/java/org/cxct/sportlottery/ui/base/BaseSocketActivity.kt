package org.cxct.sportlottery.ui.base

import android.content.IntentFilter
import android.os.Bundle
import org.cxct.sportlottery.service.SERVICE_SEND_DATA
import org.cxct.sportlottery.ui.home.broadcast.ServiceBroadcastReceiver
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseViewModel>(clazz: KClass<T>) : BaseActivity<T>(clazz) {

    val receiver by lazy {
        ServiceBroadcastReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeBroadCastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()

        removeBroadCastReceiver()
    }

    private fun subscribeBroadCastReceiver() {
        val filter = IntentFilter().apply {
            addAction(SERVICE_SEND_DATA)
        }

        registerReceiver(receiver, filter)
    }

    private fun removeBroadCastReceiver() {
        unregisterReceiver(receiver)
    }
}