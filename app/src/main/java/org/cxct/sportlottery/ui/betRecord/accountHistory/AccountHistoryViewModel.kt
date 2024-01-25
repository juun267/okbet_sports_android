package org.cxct.sportlottery.ui.betRecord.accountHistory

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.bet.settledList.RemarkBetResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.TimeUtil


class AccountHistoryViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
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


    private var betListRequesting = false

    val unSettledResult = SingleLiveEvent<BetListResult>()
    private val pageSize=20
    fun getUnsettledList(page: Int,startTime: Long?=null,endTime: Long?=null) {
        if (betListRequesting){
            return
        }
        betListRequesting = true
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(0,1), //全部注單，(0:待成立, 1:未結算)
            page = page,
            gameType = null,
            pageSize = pageSize,
            queryTimeType = null,
            startTime = startTime?.toString(),
            endTime = endTime?.toString()
        )

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }.let {
                betListRequesting = false
                unSettledResult.postValue(it)
            }
        }
    }


    val settledResult = SingleLiveEvent<BetListResult>()

    fun getSettledList(page: Int,startTime: Long?=null,endTime: Long?=null) {
        if (betListRequesting){
            return
        }
        betListRequesting = true
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(2,3,4,5,6,7), //234567 结算注单
            page = page,
            gameType = "",
            pageSize = pageSize,
            queryTimeType="settleTime",
            startTime = startTime.toString(),
            endTime = endTime.toString()
        )
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }.let {
                betListRequesting = false
                settledResult.postValue(it)
            }
        }
    }

}
