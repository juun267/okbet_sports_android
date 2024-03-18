package org.cxct.sportlottery.ui.sport.endcard

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.OneBoSportApi
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


}