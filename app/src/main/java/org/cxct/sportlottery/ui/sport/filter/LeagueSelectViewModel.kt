package org.cxct.sportlottery.ui.sport.filter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListData
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.*
import java.text.Collator
import java.util.*

class LeagueSelectViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {

    val leagueList: LiveData<MutableList<LeagueOdd>>
        get() = _leagueOddList
    private val _leagueOddList = MutableLiveData<MutableList<LeagueOdd>>()

    private var jobGetOddsList: Job? = null


     fun getOddsList(
        gameType: String,
        matchType: String,
        startTime: String? = null,
        endTime: String?=null,
        isDateSelected: Boolean = false,
        categoryCodeList: List<String>?=null
    ) {
        var playCateMenuCode = MenuCode.MAIN.code
        if (matchType == MatchType.CS.postValue) {
            playCateMenuCode = MenuCode.CS.code
        } else if (matchType == MatchType.END_SCORE.postValue) {
            playCateMenuCode = MatchType.END_SCORE.postValue
        }
         var selectStartTime:String?=""
         var selectEndTime:String?=""
         when(matchType){
             MatchType.AT_START.postValue->{
                 TimeUtil.getAtStartTimeRangeParams().let {
                     selectStartTime = it.startTime
                     selectEndTime = it.endTime
                 }
             }
             MatchType.TODAY.postValue->{
                 TimeUtil.getTodayTimeRangeParams().let {
                     selectStartTime = it.startTime
                     selectEndTime = it.endTime
                 }
             }
             MatchType.IN12HR.postValue->{
                 TimeUtil.getInHrRangeParams(12).let {
                     selectStartTime = it.startTime
                     selectEndTime = it.endTime
                 }
             }
             MatchType.IN24HR.postValue->{
                 TimeUtil.getInHrRangeParams(24).let {
                     selectStartTime = it.startTime
                     selectEndTime = it.endTime
                 }
             }
             else->{
                 selectStartTime=startTime
                 selectEndTime=endTime
             }
         }
        if (jobGetOddsList?.isActive == true) {
            jobGetOddsList?.cancel()
        }

        jobGetOddsList = viewModelScope.launch(Dispatchers.IO) {
            var result: OddsListResult? = null
            if (matchType == MatchType.IN_PLAY.postValue && gameType == GameType.ALL.key) {
                doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getInPlayAllList(
                        OddsListRequest(
                            gameType,
                            matchType,
                            startTime = selectStartTime,
                            endTime = selectEndTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }?.let {
                    var leagueOdds = mutableListOf<LeagueOdd>().apply {
                        it.OddsListDataList.forEach {
                            it?.leagueOdds?.let {
                                addAll(it)
                            }
                        }
                    }
                    result = OddsListResult(
                        it.code, it.msg, it.success,
                        OddsListData(leagueOdds)
                    )
                }
            } else {
                result = doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getOddsList(
                        OddsListRequest(
                            gameType,
                            matchType,
                            startTime = selectStartTime,
                            endTime = selectEndTime,
                            playCateMenuCode = playCateMenuCode,
                            categoryCodeList = categoryCodeList
                        )
                    )
                }


            }
            var leagueOddData = mutableListOf<LeagueOdd>()
            result?.oddsListData?.leagueOdds?.forEach {
                it.isExpanded = false
                it.league.firstCap = Pinyin.toPinyin(it.league.name.first()).first().toString()
                it.matchOdds.forEach {
                    it.isSelected = !isDateSelected
                }
                it.league.isSelected = when{
                    isDateSelected->false
                    else->it.matchOdds.all { it.isSelected }
                }
                leagueOddData.add(it)
            }

            val compar = Collator.getInstance(Locale.CHINESE)
            Collections.sort(leagueOddData, kotlin.Comparator { o1, o2 ->
                compar.compare(o1.league.name, o2.league.name)
            })
            leagueOddData.filter { !VerifyConstUtil.isValidEnglishWord(it.league?.firstCap?:"") }?.forEach {
                it.league.firstCap = "#"
            }
            _leagueOddList.postValue(leagueOddData)
        }
    }

    val outrightLeagues = SingleLiveEvent<List<org.cxct.sportlottery.network.outright.odds.LeagueOdd>>()
    fun getOutRightLeagueList(gameType: String, categoryCodeList: List<String>?=null) {

        val params = OutrightOddsListRequest(gameType, matchType = MatchType.OUTRIGHT.postValue,categoryCodeList =categoryCodeList)
        doRequest({ OneBoSportApi.outrightService.getOutrightOddsList(params) }) {
            val leagues = it?.outrightOddsListData?.leagueOdds
            if (leagues == null) {
                outrightLeagues.value = listOf()
                return@doRequest
            }

            outrightLeagues.value = leagues!!
        }
    }

}