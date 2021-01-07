package org.cxct.sportlottery.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.greenrobot.eventbus.EventBus

open class BaseService : Service() {
//    private lateinit var eventBus: EventBus

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
/*
    override fun onCreate() {
        super.onCreate()
        //訂閱監聽 event bus
        eventBus = EventBus.getDefault()
        eventBus.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //一定要記得取消註冊釋放資源

        eventBus.unregister(this)
    }
    */
}
