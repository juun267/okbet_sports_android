package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.service.BackService
import kotlin.reflect.KClass

abstract class BaseSocketDialog<T : BaseViewModel>(clazz: KClass<T>) : BaseDialog<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    val service: BackService
        get() = (activity as BaseSocketActivity<*>).backService

    protected fun registerChannelHall(receiverChannelHall: BaseSocketActivity.ReceiverChannelHall) {
        (activity as BaseSocketActivity<*>).registerChannelHall(receiverChannelHall)
    }

    protected fun registerChannelEvent(receiverChannelEvent: BaseSocketActivity.ReceiverChannelEvent) {
        (activity as BaseSocketActivity<*>).registerChannelEvent(receiverChannelEvent)
    }

    protected fun registerChannelPublic(receiverChannelPublic: BaseSocketActivity.ReceiverChannelPublic) {
        (activity as BaseSocketActivity<*>).registerChannelPublic(receiverChannelPublic)
    }

    protected fun subscribeChannelHall(gameType: String?, cateMenuCode: String?, eventId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChannelHall(gameType, cateMenuCode, eventId)
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

    protected fun unSubscribeChannelHallAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelHallAll()
    }

    protected fun unSubscribeChannelEventAll() {
        (activity as BaseSocketActivity<*>).unSubscribeChannelEventAll()
    }
}