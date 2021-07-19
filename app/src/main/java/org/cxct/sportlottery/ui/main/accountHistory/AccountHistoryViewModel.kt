package org.cxct.sportlottery.ui.main.accountHistory

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.settledList.BetSettledListRequest
import org.cxct.sportlottery.network.bet.settledList.BetSettledListResult
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil


class AccountHistoryViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val sportMenuRepository: SportMenuRepository,
    private val thirdGameRepository: ThirdGameRepository,
) : BaseNoticeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val betRecordResult: LiveData<BetSettledListResult>
        get() = _betSettledRecordResult

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg


    private val _loading = MutableLiveData<Boolean>()
    private val _betSettledRecordResult = MutableLiveData<BetSettledListResult>()

    private var mBetSettledListRequest: BetSettledListRequest? = null

    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()

    val emptyFilter = { item: String? ->
        if (item.isNullOrEmpty()) null else item
    }

    fun searchBetRecord(
        gameType: String = "",
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
    ) {
        mBetSettledListRequest = BetSettledListRequest(
            gameType = emptyFilter(gameType),
            startTime = startTime,
            endTime = endTime,
            page = 1,
            pageSize = PAGE_SIZE
        )
        mBetSettledListRequest?.let { getBetSettledList(it) }
    }

    var isLastPage = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetSettledListRequest?.let {
                    mBetSettledListRequest = BetSettledListRequest(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        page = it.page?.plus(1),
                        pageSize = PAGE_SIZE
                    )
                    getBetSettledList(mBetSettledListRequest!!)
                }
            }
        }

    }

    private fun getBetSettledList(betSettledListRequest: BetSettledListRequest) {

        if (betSettledListRequest.page == 1) {
            nowPage = 1
            recordDataList.clear()
        }

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetSettledList(betSettledListRequest)
            }?.let { result ->
                hideLoading()
                result.rows?.let { recordDataList.addAll(it) }
                isLastPage = (recordDataList.size >= (result.total ?: 0))
                _betSettledRecordResult.value = result
            }
        }

    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }


    //獲取系統公告
    fun getAnnouncement() {
        if (isLogin.value == true) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    val typeList = arrayOf(1)
                    OneBoSportApi.messageService.getPromoteNotice(typeList)
                }?.let { result -> _messageListResult.postValue(result) }
            }
        } else {
            _messageListResult.value = null
        }
    }



    fun getSettlementNotification(event: OrderSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code -> {
                    _settlementNotificationMsg.value = Event(it)
                }
            }
        }
    }
}
