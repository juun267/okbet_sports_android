package org.cxct.sportlottery.ui.base

import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.ui.chat.LiveMsgEntity

open class BindingSocketFragment<VM : BaseViewModel, VB: ViewBinding>: BindingFragment<VM, VB>() {


    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    val applicationReceiver by lazy {
        (activity as BaseSocketActivity<*>).applicationReceiver
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
        eventId: String?
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

    protected fun subscribeChatRoom(roomId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChatRoom(roomId)
    }

    protected fun unSubscribeChatRoom(roomId: String?) {
        (activity as BaseSocketActivity<*>).unSubscribeChatRoom(roomId)
    }

    protected fun subscribeChatUser(userId: String?) {
        (activity as BaseSocketActivity<*>).subscribeChatUser(userId)
    }

    protected fun unSubscribeChatUser(userId: String?) {
        (activity as BaseSocketActivity<*>).unSubscribeChatUser(userId)
    }

    protected fun chatSendMessage(liveMsgEntity: LiveMsgEntity?) {
        (activity as BaseSocketActivity<*>).chatSendMessage(liveMsgEntity)
    }

}