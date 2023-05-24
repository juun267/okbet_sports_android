package org.cxct.sportlottery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import org.cxct.sportlottery.common.event.NetWorkEvent
import org.cxct.sportlottery.util.EventBusUtil


@Suppress("DEPRECATION")
class NetBroadcastReceiver: BroadcastReceiver() {
    private var lastTime=0L
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val manager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request= NetworkRequest.Builder().build()
            manager.requestNetwork(request,object: ConnectivityManager.NetworkCallback(){
                //网络恢复
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val nowTime=System.currentTimeMillis()
                    if(nowTime-lastTime>1000){
                        lastTime=nowTime
                        EventBusUtil.post(NetWorkEvent(true))
                    }
                }

                //网络断开
               override fun onLost(network: Network) {
                   super.onLost(network)
                    EventBusUtil.post(NetWorkEvent(false))
               }
            })
        }

    }


}