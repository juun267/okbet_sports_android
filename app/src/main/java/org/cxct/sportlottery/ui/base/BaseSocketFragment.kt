package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import kotlin.reflect.KClass

abstract class BaseSocketFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFragment<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiver.dataSourceChange.observe(viewLifecycleOwner) {
            dataSourceChangeEven?.let {
                showErrorPromptDialog(
                    title = getString(R.string.prompt),
                    message = getString(R.string.message_source_change),
                    hasCancel = false
                ) { it.invoke() }
            }
        }
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

    protected fun getBetListPageVisible(): Boolean {
        return when (activity) {
            is MainTabActivity-> (activity as MainTabActivity).getBetListPageVisible()
            is SportDetailActivity -> (activity as SportDetailActivity).getBetListPageVisible()
            else -> false
        }
    }
    private var dataSourceChangeEven: (() -> Unit)? = null

    /**
     * 设置有新赛事数据监听回调。
     *  重点!!!
     *  页面加载完成后再调用该方法就行设置回调，
     *  不然由于LiveData粘性事件的原因，在页面初始化的时候就有可能弹窗
     */
    fun setDataSourceChangeEvent(dataSourceChangeEven: () -> Unit) {
        this.dataSourceChangeEven = dataSourceChangeEven
    }
}