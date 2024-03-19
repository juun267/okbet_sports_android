package org.cxct.sportlottery.ui.sport.endcard

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.SingleLiveEvent

class EndCardVM(androidContext: Application): SportListViewModel(androidContext) {

    val betNum by lazy { MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 10) }
    val endcardMatchList = MutableLiveData<List<LeagueOdd>?>()
    val addBetResult = SingleLiveEvent<Receipt>()
    val addFaildResult = SingleLiveEvent<String>()
    val lgpcoflDetail = SingleLiveEvent<Array<EndCardBet>?>()

    fun loadEndCardMatchList() {
        val matchType = "LGPCOFL"
        val gameType = GameType.BK.key

        doRequest({ OneBoSportApi.oddsService.getOddsList(
            OddsListRequest(gameType, matchType, playCateMenuCode = matchType))}
        ) { result ->

            val leagueOdds = result?.oddsListData?.leagueOdds
            if (leagueOdds == null) {
                endcardMatchList.value = null
            } else {
                dealLeagueList(matchType, matchType, leagueOdds, listOf())
                endcardMatchList.value = leagueOdds
            }
        }

    }

    fun addBetLGPCOFL(matchId: String,scoreList: List<String>,nickName: String,stake: Int){
       callApi({SportRepository.addBetLGPCOFL(matchId, scoreList,nickName,stake)}){
           if (it.succeeded()){
               addBetResult.postValue(it.getData())
               getMoneyAndTransferOut(true)
               viewModelScope.launch { betNum.emit(Pair(matchId, scoreList.size)) }
           }else{
               addFaildResult.postValue(it.msg)
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
                it?.page = page
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
                it?.page = page
                settledResult.postValue(it)
            }
        }
    }

    fun getLGPCOFLDetail(matchId: String) {
        callApi({ SportRepository.getLGPCOFLDetail(matchId) }) {
            lgpcoflDetail.value = it.getData()
        }
    }


}