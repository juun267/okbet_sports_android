package org.cxct.sportlottery.ui.main.accountHistory

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
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

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg


    private val _loading = MutableLiveData<Boolean>()
    private val _betRecordResult = MutableLiveData<BetListResult>()

    private var mBetListRequest: BetListRequest? = null

    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()

    fun searchBetRecord(
        isChampionChecked: Boolean? = false,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        status: String? = null,
    ) {
        val statusFilter = { item: String? ->
            if (item.isNullOrEmpty()) listOf(1, 2, 3, 4, 5, 6, 7) else item.toList().map {
                Character.getNumericValue(it)

            }
        }
        val championOnly = if (isChampionChecked == true) 1 else 0
        mBetListRequest = BetListRequest(
            championOnly = championOnly,
            statusList = statusFilter(status),
            startTime = startTime,
            endTime = endTime,
            page = 1,
            pageSize = PAGE_SIZE
        )
        mBetListRequest?.let { getBetList(it) }
    }

    var isLastPage = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetListRequest?.let {
                    mBetListRequest = BetListRequest(
                        championOnly = it.championOnly,
                        statusList = it.statusList,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        page = it.page?.plus(1),
                        pageSize = PAGE_SIZE
                    )
                    getBetList(mBetListRequest!!)
                }
            }
        }

    }

    private fun getBetList(betListRequest: BetListRequest) {

        if (betListRequest.page == 1) {
            nowPage = 1
            recordDataList.clear()
        }

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }?.let { result ->
                hideLoading()
                result.rows?.let { recordDataList.addAll(it) }
                isLastPage = (recordDataList.size >= (result.total ?: 0))
                _betRecordResult.value = result
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
