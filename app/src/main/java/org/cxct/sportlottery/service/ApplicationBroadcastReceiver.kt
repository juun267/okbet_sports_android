package org.cxct.sportlottery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.UserDiscountChangeEvent
import org.cxct.sportlottery.network.service.close_play_cate.ClosePlayCateEvent
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_lock.MatchOddsLockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SysMaintenanceEvent
import org.cxct.sportlottery.network.service.user_level_config_change.UserLevelConfigListEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.PlayRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.EncryptUtil
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.sortOddsMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber

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
                mSystemStatusListener?.onSystemStatusChange(data?.status)
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

    var mSystemStatusListener:SystemStatusListener?=null

    interface SystemStatusListener{
        fun onSystemStatusChange(status:Int?)
    }
}