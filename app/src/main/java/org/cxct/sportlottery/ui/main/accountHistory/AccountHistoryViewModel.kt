package org.cxct.sportlottery.ui.main.accountHistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListResult
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

    val selectedSportDate: LiveData<Pair<String?, String?>> //<sport, date>
        get() = _selectedSportDate

    val betRecordResult: LiveData<BetSettledListResult>
        get() = _betSettledRecordResult

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg

    val betDetailResult: LiveData<BetSettledDetailListResult>
        get() = _betDetailResult

    private val _loading = MutableLiveData<Boolean>()
    private val _selectedSportDate = MutableLiveData<Pair<String?, String?>>()
    private val _betSettledRecordResult = MutableLiveData<BetSettledListResult>()
    private var mBetSettledListRequest: BetSettledListRequest? = null
    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()
    private val _betDetailResult = MutableLiveData<BetSettledDetailListResult>()

    val emptyFilter = { item: String? ->
        if (item.isNullOrEmpty()) null else item
    }

    fun searchBetRecord(
        gameType: String ?= "",
    ) {

        BetSettledListRequest(
            startTime = startTime,
            endTime = endTime,
            gameType = emptyFilter(gameType),
            page = 1,
            pageSize = PAGE_SIZE
        ).let {
            getBetSettledList(it)
        }
    }

    var isLastPage = false
    private var nowPage = 1

    private var mBetDetailRequest: BetSettledDetailListRequest? = null

    val recordDataList = mutableListOf<Row?>()
    val startTime = TimeUtil.getDefaultTimeStamp().startTime
    val endTime = TimeUtil.getDefaultTimeStamp().endTime

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetSettledListRequest?.let {
                    mBetSettledListRequest = BetSettledListRequest(
                        startTime = startTime,
                        endTime = endTime,
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

    var isDetailLastPage = false
    private var nowDetailPage = 1
    val detailDataList = mutableListOf<org.cxct.sportlottery.network.bet.settledDetailList.Row>()

    fun searchDetail(gameType: String? = null, date: String? = null) {

        val startTime = TimeUtil.dateToTimeStamp(date, TimeUtil.TimeType.START_OF_DAY, TimeUtil.YMD_FORMAT).toString()
        val endTime = TimeUtil.dateToTimeStamp(date, TimeUtil.TimeType.END_OF_DAY, TimeUtil.YMD_FORMAT).toString()

        mBetDetailRequest = BetSettledDetailListRequest(
            gameType = emptyFilter(gameType),
            statDate = date,
            startTime = startTime,
            endTime = endTime,
            page = 1,
            pageSize = PAGE_SIZE)
        mBetDetailRequest?.let { getDetailList(it) }
    }

    fun getDetailNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isDetailLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetDetailRequest?.let {
                    mBetDetailRequest = BetSettledDetailListRequest(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        page = it.page?.plus(1),
                        pageSize = PAGE_SIZE)
                    getDetailList(mBetDetailRequest!!)
                }
            }
        }
    }


    private fun getDetailList(betDetailRequest: BetSettledDetailListRequest) {

        if (betDetailRequest.page == 1) {
            nowDetailPage = 1
            detailDataList.clear()
        }

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetSettledDetailList(betDetailRequest)
            }?.let { result ->
                hideLoading()
                result.rows?.let { detailDataList.addAll(it) }
                isDetailLastPage = (detailDataList.size >= (result.total ?: 0))
                _betDetailResult.value = result
            }
        }

    }

    fun setSelectedSportDate(sport: String? = _selectedSportDate.value?.first, date: String? = _selectedSportDate.value?.second) {
        _selectedSportDate.value = Pair(sport, date)
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }


}
