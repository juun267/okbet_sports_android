package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.Observer
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseActivity<T>(clazz) {

    val receiver by lazy {
        ServiceBroadcastReceiver()
    }

    lateinit var backService: BackService

    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.e(">>> onServiceConnected")
            val binder = service as BackService.MyBinder //透過Binder調用Service內的方法
            backService = binder.service

            binder.connect(
                viewModel.loginRepository.token,
                viewModel.loginRepository.userId,
                viewModel.loginRepository.platformId
            )

            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.e(">>> onServiceDisconnected")
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver.sysMaintenance.observe(this, Observer {
            startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        })

        viewModel.errorResultToken.observe(this, Observer {
            backService.apply {
                //TODO Dean : 待解除訂閱方法完善後加入解除訂閱私人頻道
            }
        })
    }

    override fun onStart() {
        super.onStart()

        subscribeBroadCastReceiver()
        bindService()
    }

    override fun onStop() {
        super.onStop()

        removeBroadCastReceiver()
        unBindService()
    }

    private fun bindService() {
        if (isServiceBound) return

        val serviceIntent = Intent(this, BackService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
    }

    private fun unBindService() {
        if (!isServiceBound) return

        unbindService(serviceConnection)
        isServiceBound = false
    }

    private fun checkServiceRunning(): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BackService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun subscribeBroadCastReceiver() {
        val filter = IntentFilter().apply {
            addAction(BackService.SERVICE_SEND_DATA)
        }

        registerReceiver(receiver, filter)
    }

    private fun removeBroadCastReceiver() {
        unregisterReceiver(receiver)
    }
}