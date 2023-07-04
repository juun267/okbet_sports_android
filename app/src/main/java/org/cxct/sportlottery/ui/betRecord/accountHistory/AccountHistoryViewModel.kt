package org.cxct.sportlottery.ui.betRecord.accountHistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListResult
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.bet.settledList.BetSettledListRequest
import org.cxct.sportlottery.network.bet.settledList.BetSettledListResult
import org.cxct.sportlottery.network.bet.settledList.RemarkBetResult
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.betRecord.BetListData
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.SingleLiveEvent
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
        const val PAGE_SIZE = 20
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

//    val sportCodeList: LiveData<List<StatusSheetData>>
//        get() = _sportCodeSpinnerList

    private val _loading = MutableLiveData<Boolean>()
    private val _selectedSport = MutableLiveData<Event<String?>>()
    private val _selectedDate = MutableLiveData<Event<String?>>()
    private val _betSettledRecordResult = MutableLiveData<BetSettledListResult>()
    private var mBetSettledListRequest: BetSettledListRequest? = null
    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()
    private val _betDetailResult = MutableLiveData<BetSettledDetailListResult>()
    val remarkBetLiveData: MutableLiveData<RemarkBetResult> = MutableLiveData()

    //    private val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單
    var tabPosition = 0 //當前tabPosition (for 新版UI)

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

    fun reMarkBet(remarkBetRequest: RemarkBetRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.reMarkBet(remarkBetRequest)
            }?.let { remarkBetResult ->
                if (remarkBetResult.success) {
                    remarkBetLiveData.postValue(remarkBetResult)
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
            pageSize = PAGE_SIZE
        )
        mBetDetailRequest?.let { getDetailList(it) }
    }

    fun getDetailNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int, date: String) {
        if (_loading.value != true && !isDetailLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetDetailRequest?.let {
                    mBetDetailRequest = BetSettledDetailListRequest(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        statDate = date,
                        page = it.page,
                        pageSize = PAGE_SIZE
                    )
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

    /**
     * 獲取當前可用球種清單
     */
    fun getSportList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportList(type = 1)
            }?.let { sportListResponse ->
                if (sportListResponse.success) {
                    val sportCodeList = mutableListOf<StatusSheetData>()
                    //第一項為全部球種
                    sportCodeList.add(StatusSheetData("", LocalUtils.getString(R.string.all_sport)))
                    //根據api回傳的球類添加進當前啟用球種篩選清單
                    sportListResponse.rows.sortedBy { it.sortNum }.map {
                        if (it.state == 1) { //僅添加狀態為啟用的資料
                            sportCodeList.add(
                                StatusSheetData(
                                    it.code,
                                    GameType.getGameTypeString(
                                        LocalUtils.getLocalizedContext(),
                                        it.code
                                    )
                                )
                            )
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _sportCodeSpinnerList.value = sportCodeList
                    }
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

    //region 未結算
    var gameTypeCode = ""

    //整理後未結算投注紀錄資料格式
    val betListData: LiveData<BetListData>
        get() = _betListData
    private val _betListData = MutableLiveData<BetListData>()

    val responseFailed: LiveData<Boolean>
        get() = _responseFailed
    private val _responseFailed = MutableLiveData<Boolean>()

    private var betListRequesting = false
    private var requestCount = 0
    private val requestMaxCount = 2

    //獲取交易狀況資料(未結算)
    fun getBetList(firstPage: Boolean = false, gameType: String? = gameTypeCode) {
        if (betListRequesting || (betListData.value?.isLastPage == true && !firstPage))
            return
        betListRequesting = true

        val page = if (firstPage) 1 else betListData.value?.page?.plus(1) ?: 1
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(0,1), //全部注單，(0:待成立, 1:未結算)
            page = page,
            gameType = gameType,
            pageSize = PAGE_SIZE
        )

        loading()
        viewModelScope.launch {
            val resultData=doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }

            if(resultData==null){
                _responseFailed.postValue(true)
                return@launch
            }
            resultData.let { result ->
                betListRequesting = false
                if (result.success) {
                    requestCount = 0
                    val rowList =
                        if (page == 1) mutableListOf()
                        else betListData.value?.row?.toMutableList() ?: mutableListOf()
                    result.rows?.let { rowList.addAll(it.apply { }) }

                    _betListData.value =
                        BetListData(
                            rowList,
                            MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK,
                            result.other?.totalAmount ?: 0.0,
                            page,
                            (rowList.size >= (result.total ?: 0))
                        )
                    loginRepository.updateTransNum(result.total ?: 0)
                } else {
                    if (result.code == NetWorkResponseType.REQUEST_TOO_FAST.code && requestCount < requestMaxCount) {
                        requestCount += 1
                        _responseFailed.postValue(true)
                    }
                }

                hideLoading()
            }
        }
    }
    //endregion 未結算



    val unsettledDataEvent=SingleLiveEvent<List<org.cxct.sportlottery.network.bet.list.Row>>()
    var pageIndex=1
    private val pageSize=20
    fun getUnsettledList() {
        if (betListRequesting ){
            _responseFailed.postValue(true)
            return
        }
        betListRequesting = true
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(0,1), //全部注單，(0:待成立, 1:未結算)
            page = pageIndex,
            gameType = "",
            pageSize = pageSize
        )

        viewModelScope.launch {
            val resultData=doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }
            betListRequesting = false
            if(resultData==null){
                _responseFailed.postValue(true)
                return@launch
            }


            resultData.let { result ->
                if (result.success) {
                    pageIndex++
                    if(result.rows.isNullOrEmpty()){
                        unsettledDataEvent.postValue(arrayListOf())
                    }else{

                        unsettledDataEvent.postValue(result.rows!!)
                        loginRepository.updateTransNum(result.total ?: 0)
                    }

                } else {
                    if (result.code == NetWorkResponseType.REQUEST_TOO_FAST.code && requestCount < requestMaxCount) {
                        unsettledDataEvent.postValue(arrayListOf())
                    }
                }
            }
        }
    }




    val settledData: LiveData<List<org.cxct.sportlottery.network.bet.list.Row>>
        get() = _settledData
    private val _settledData = MutableLiveData<List<org.cxct.sportlottery.network.bet.list.Row>>()



    var pageSettledIndex=1
    //已结单数据 开始时间
    var settledStartTime:Long?=0L
    //已结单数据 结束时间
    var settledEndTime:Long?=0L
    //总盈亏
    var totalReward:Double=0.0
    //总投注额
    var totalBet:Double=0.0
    //有效投注额
    var totalEfficient:Double=0.0
    fun getSettledList() {
        if (betListRequesting ){
            _responseFailed.postValue(true)
            return
        }
        betListRequesting = true

        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(2,3,4,5,6,7), //234567 结算注单
            page = pageSettledIndex,
            gameType = "",
            pageSize = pageSize,
            startTime = settledStartTime.toString(),
            endTime = settledEndTime.toString()
        )
        if(pageSize==1){
            totalReward=0.0
            totalBet=0.0
            totalEfficient=0.0
        }
        loading()
        viewModelScope.launch {
            val resultData=doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }
            delay(800)
            hideLoading()
            betListRequesting = false
            if(resultData==null){
                _responseFailed.postValue(true)
                return@launch
            }
            pageSettledIndex++
            resultData.let { result ->
                if (result.success) {
                    result.rows?.let {
                        _settledData.postValue(it)
                        loginRepository.updateTransNum(result.total ?: 0)
                    }
                    result.other?.totalAmount?.let {
                        totalBet=it
                    }

                    result.other?.win?.let {
                        totalReward=it
                    }

                    result.other?.valueBetAmount?.let {
                        totalEfficient=it
                    }

                } else {
                    if (result.code == NetWorkResponseType.REQUEST_TOO_FAST.code && requestCount < requestMaxCount) {
                        _settledData.postValue(arrayListOf())
                    }
                }
            }
        }
    }

}
