package org.cxct.sportlottery.ui.betRecord.accountHistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.bet.settledList.RemarkBetResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.JsonUtil
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

    private val _loading = MutableLiveData<Boolean>()
    private val remarkBetLiveData: MutableLiveData<RemarkBetResult> = SingleLiveEvent()

    fun observerRemarkBetLiveData(lifecycleOwner: LifecycleOwner? = null, block: (RemarkBetResult) -> Unit) {
        if (lifecycleOwner == null) {
            val observer = object : Observer<RemarkBetResult> {
                override fun onChanged(it: RemarkBetResult) {
                    block.invoke(it)
                    remarkBetLiveData.removeObserver(this)
                }
            }
            remarkBetLiveData.observeForever(observer)
        } else {
            remarkBetLiveData.observe(lifecycleOwner) { block.invoke(it) }
        }
    }

    var isLastPage = false

    //20220621 按照默认时区来请求数据
    val startTime = TimeUtil.getDefaultTimeStamp(7).startTime
    val endTime = TimeUtil.getDefaultTimeStamp(7).endTime


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

    val responseFailed: LiveData<Boolean>
        get() = _responseFailed
    private val _responseFailed = MutableLiveData<Boolean>()

    private var betListRequesting = false


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
                    }

                } else {
                    unsettledDataEvent.postValue(arrayListOf())
                }
            }
        }
    }




    val settledData: LiveData<List<org.cxct.sportlottery.network.bet.list.Row>>
        get() = _settledData
    private val _settledData = MutableLiveData<List<org.cxct.sportlottery.network.bet.list.Row>>()

    val errorEvent=SingleLiveEvent<String>()


    var pageSettledIndex=1
    var hasMore=true
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
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(2,3,4,5,6,7), //234567 结算注单
            page = pageSettledIndex,
            gameType = "",
            pageSize = pageSize,
            queryTimeType="settleTime",
            startTime = settledStartTime.toString(),
            endTime = settledEndTime.toString()
        )
        if(pageSize==1){
            totalReward=0.0
            totalBet=0.0
            totalEfficient=0.0
        }
        viewModelScope.launch {
            val resultData=doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }

            if(resultData==null){
                _responseFailed.postValue(true)
                return@launch
            }

            resultData.let { result ->
                if (result.success) {
                    pageSettledIndex++
                    result.rows?.let {
                        if(it.isEmpty()){
                            hasMore=false
                        }
                        _settledData.postValue(it)
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
                    errorEvent.postValue(result.msg)
                }
            }
        }
    }

}
