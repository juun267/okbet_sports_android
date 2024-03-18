package org.cxct.sportlottery.ui.sport.endcard

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.SingleLiveEvent

class EndCardVM(androidContext: Application): SportListViewModel(androidContext) {

    val endcardMatchList = MutableLiveData<List<LeagueOdd>?>()
    val addBetResult = SingleLiveEvent<String>()

    fun loadEndCardMatchList() {
        val matchType = MatchType.END_SCORE.postValue
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
           addBetResult.postValue(it.getData())
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


}