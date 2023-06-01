package org.cxct.sportlottery.service

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SysMaintenanceEvent
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.EncryptUtil
import org.cxct.sportlottery.util.SingleLiveEvent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

object ApplicationBroadcastReceiver {

    val sysMaintenance: LiveData<SysMaintenanceEvent?>
        get() = _sysMaintenance

    val sportMaintenance: LiveData<SportMaintenanceEvent?>
        get() = _sportMaintenance

    private val _sysMaintenance = MutableLiveData<SysMaintenanceEvent?>()
    val _sportMaintenance = MutableLiveData<SportMaintenanceEvent?>()




    fun receiveMessage(bundle: Bundle) {
        CoroutineScope(Dispatchers.IO).launch {
            val channelStr = bundle.getString(BackService.CHANNEL_KEY, "") ?: ""
            val messageStr = bundle.getString(BackService.SERVER_MESSAGE_KEY, "") ?: ""
            val decryptMessage = EncryptUtil.uncompress(messageStr)
            try {
                decryptMessage?.let {
                    if (it.isNotEmpty()) {
                        val json = JSONTokener(it).nextValue()
                        if (json is JSONArray) {
                            var jsonArray = JSONArray(it)
                            for (i in 0 until jsonArray.length()) {
                                var jObj = jsonArray.optJSONObject(i)
                                val jObjStr = jObj.toString()
                                handleEvent(jObj, jObjStr, channelStr)
                            }
                        } else if (json is JSONObject) {
                            val jObjStr = json.toString()
                            handleEvent(json, jObjStr, channelStr)
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e("JSONException", "WS格式出問題 $messageStr")
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleEvent(jObj: JSONObject, jObjStr: String, channelStr: String) {
        when (val eventType = jObj.optString("eventType")) {


            //公共频道(这个通道会通知主站平台维护)
            EventType.SYS_MAINTENANCE -> {
                val data = ServiceMessage.getSysMaintenance(jObjStr)
//                _sysMaintenance.postValue(data)
                onSystemStatusChange.postValue(data?.status == 1)
            }
            //体育服务开关
            EventType.SPORT_MAINTAIN_STATUS -> {
                val data = ServiceMessage.getSportMaintenance(jObjStr)
                sConfigData?.sportMaintainStatus="${data?.status}"
                _sportMaintenance.postValue(data)
            }
            else -> {}

        }

    }

    var onSystemStatusChange: SingleLiveEvent<Boolean> = SingleLiveEvent()

}