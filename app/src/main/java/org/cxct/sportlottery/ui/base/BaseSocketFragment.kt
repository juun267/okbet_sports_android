package org.cxct.sportlottery.ui.base

import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver

abstract class BaseSocketFragment<VM : BaseViewModel, VB: ViewBinding>: BaseFragment<VM, VB>() {


    val receiver = ServiceBroadcastReceiver

    protected fun subscribeSportChannelHall() {
        BackService.subscribeSportChannelHall()
    }

    protected fun subscribeChannelHall(gameType: String?, eventId: String?) {
        BackService.subscribeHallChannel(gameType, eventId)
    }

    fun subscribeChannel2HotMatch(gameType: String?, eventId: String?) {
        BackService.subscribeHallChannel(gameType, eventId)
    }

    protected fun subscribeChannelEvent(eventId: String?) {
        BackService.subscribeEventChannel(eventId, null)
    }

    protected fun unSubscribeChannelHall(
        gameType: String?,
        eventId: String?
    ) {
        BackService.unsubscribeHallChannel(gameType, eventId)
    }

    protected fun unSubscribeChannelEvent(eventId: String?) {
        BackService.unsubscribeEventChannel(eventId)
    }

    protected fun unsubscribeHallChannel(eventId: String?) {
        BackService.unsubscribeHallChannel(eventId)
    }

    protected fun unSubscribeChannelHall(eventId: String?) {
        BackService.unsubscribeHallChannel(eventId)
    }

    protected fun unSubscribeChannelHallAll() {
        BackService.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannel2HotMatch(){
        BackService.unsubscribeAllHallChannel()
    }

    protected fun unSubscribeChannelHallSport() {
        BackService.unsubscribeSportHallChannel()
    }

    protected fun unSubscribeChannelEventAll() {
        BackService.unsubscribeAllEventChannel()
    }

    protected fun betListPageSubscribeEvent() {
        BackService.betListPageSubscribeEvent()
    }

    protected fun betListPageUnSubScribeEvent() {
        BackService.betListPageUnSubScribeEvent()
    }


}