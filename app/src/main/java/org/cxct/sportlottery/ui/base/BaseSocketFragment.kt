package org.cxct.sportlottery.ui.base

import kotlin.reflect.KClass

abstract class BaseSocketFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFragment<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    protected fun subscribeSportChannelHall(gameTypeCode: String ?= null) {
        (activity as BaseSocketActivity<*>).subscribeSportChannelHall(gameTypeCode)
    }

    protected fun subscribeChannelHall(gameType: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelHall(gameType, eventId)
    }

    protected fun subscribeChannelEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelEvent(eventId)
    }

    protected fun unSubscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHall(gameType, cateMenuCode, eventId)
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
}