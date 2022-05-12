package org.cxct.sportlottery.ui.base

import kotlin.reflect.KClass

abstract class BaseSocketDialog<T : BaseViewModel>(clazz: KClass<T>) : BaseDialog<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    protected fun subscribeChannelHall(gameType: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelHall(gameType, eventId)
    }

    protected fun subscribeChannelEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelEvent(eventId)
    }

    protected fun unSubscribeChannelHall(
        gameType: String?,
        eventId: String?
    ) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHall(gameType, eventId)
    }

    protected fun unSubscribeChannelEvent(eventId: String?) {
        (activity as BaseSocketActivity<*>).unSubscribeChannelEvent(eventId)
    }

    protected fun unSubscribeChannelHallAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHallAll()
    }

    protected fun unSubscribeChannelEventAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelEventAll()
    }
}