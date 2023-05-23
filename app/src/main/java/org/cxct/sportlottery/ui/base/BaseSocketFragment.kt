package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.ui.chat.LiveMsgEntity
import kotlin.reflect.KClass

abstract class BaseSocketFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFragment<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }


    protected fun subscribeSportChannelHall() {
        (activity as BaseSocketActivity<*>).subscribeSportChannelHall()
    }

    protected fun subscribeChannelHall(gameType: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelHall(gameType, eventId)
    }

    fun subscribeChannel2HotMatch(gameType: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelHall(gameType, eventId)
    }

    protected fun subscribeChannelEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelEvent(eventId)
    }

    protected fun unSubscribeChannelHall(
        gameType: String?,
        eventId: String?,
    ) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHall(gameType, eventId)
    }

    protected fun unSubscribeChannelEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelEvent(eventId)
    }

    protected fun unsubscribeHallChannel(eventId: String?) {
        (activity as BaseSocketActivity<*>).unsubscribeHallChannel(eventId)
    }

    protected fun unSubscribeChannelHall(eventId: String?) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHall(eventId)
    }

    protected fun unSubscribeChannelHallAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHallAll()
    }

    fun unSubscribeChannel2HotMatch(){
        (activity as BaseSocketActivity<*>).unSubscribeChannelHallAll()
    }

    protected fun unSubscribeChannelHallSport() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHallSport()
    }

    protected fun unSubscribeChannelEventAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelEventAll()
    }

    protected fun betListPageSubscribeEvent() {
        (activity as BaseSocketActivity<*>).betListPageSubscribeEvent()
    }

    protected fun betListPageUnSubScribeEvent() {
        (activity as BaseSocketActivity<*>).betListPageUnSubScribeEvent()
    }

    protected fun fastBetPageSubscribeHallEvent(gameType: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).fastBetPageSubscribeHallEvent(gameType, eventId)
    }

    protected fun fastBetPageSubscribeEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).fastBetPageSubscribeEvent(eventId)
    }

    protected fun fastBetPageUnSubscribeEvent() {
        (activity as BaseSocketActivity<*>).fastBetPageUnSubscribeEvent()
    }

    protected fun getBetListPageVisible(): Boolean {
        return when (val thisActivity = activity) {
            is BaseBottomNavActivity<*> -> thisActivity.getBetListPageVisible()
            else -> false
        }
    }
}