package org.cxct.sportlottery.ui.main.accountHistory

import android.app.Application
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
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil


class AccountHistoryViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val selectedSport: LiveData<Event<String?>>
        get() = _selectedSport

    val selectedDate: LiveData<Event<String?>>
        get() = _selectedDate

    val betRecordResult: LiveData<BetSettledListResult>
        get() = _betSettledRecordResult

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val betDetailResult: LiveData<BetSettledDetailListResult>
        get() = _betDetailResult

    private val _loading = MutableLiveData<Boolean>()
    private val _selectedSport = MutableLiveData<Event<String?>>()
    private val _selectedDate = MutableLiveData<Event<String?>>()
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
    //20220506 先還原, 等之後後端若有通知在看此處如何修正
    /*private val accountHistoryTimeRangeParams = TimeUtil.getAccountHistoryTimeRangeParams()
    val startTime = accountHistoryTimeRangeParams.startTime
    val endTime = accountHistoryTimeRangeParams.endTime*/

    //20220621 按照默认时区来请求数据
    val startTime = TimeUtil.getDefaultTimeStamp(7).startTime
    val endTime = TimeUtil.getDefaultTimeStamp(7).endTime


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

                //列出包含今日往前共八天的日期
                val showDateList = mutableListOf<String>()
                for (i in 0..7) {
                    showDateList.add(TimeUtil.getMinusDate(i, TimeUtil.YMD_FORMAT))
                }
                //過濾資料僅顯示八日
                val filteredSettledList = result.rows?.filter { showDateList.contains(it.statDate) }?.toMutableList()
                filteredSettledList?.sortBy { it.statDate }
                filteredSettledList?.let { row ->
                    recordDataList.addAll(row)
                }

                //TODO 目前沒有分頁需求, 此處是配合後端先暫時過濾資料僅顯示八日, 後續有需要使用到分頁時需review
                //將被過濾掉的資料數量加回去
                val notInShowDateSize = result.rows?.filter { !showDateList.contains(it.statDate) }?.size ?: 0
                isLastPage = (recordDataList.size + notInShowDateSize >= (result.total ?: 0))

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

    var isDetailLastPage = false
    private var nowDetailPage = 1
    val detailDataList = mutableListOf<org.cxct.sportlottery.network.bet.settledDetailList.Row>()

    fun searchDetail(gameType: String? = selectedSport.value?.peekContent(), date: String? = selectedDate.value?.peekContent()) {

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
                        statDate = selectedDate.value?.peekContent(),
                        page = it.page,
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
                if (result.success) {
                    mBetDetailRequest?.page = mBetDetailRequest?.page?.plus(1)
                    result.rows?.let { detailDataList.addAll(it) }
                    isDetailLastPage = (detailDataList.size >= (result.total ?: 0))
                    _betDetailResult.value = result
                }
            }
        }

    }

    fun setSelectedDate(date: String?) {
        _selectedDate.value = Event(date)
    }

    fun setSelectedSport(sport: String?) {
        _selectedSport.value = Event(sport)
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }


}
