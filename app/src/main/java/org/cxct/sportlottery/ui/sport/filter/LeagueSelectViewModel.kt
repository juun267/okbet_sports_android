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
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListData
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.VerifyConstUtil
import java.text.Collator
import java.util.*

class LeagueSelectViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
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

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()
    private var jobGetOddsList: Job? = null


    fun clearSelectedLeague() {
        _leagueOddList.postValue(mutableListOf())
    }

     fun getOddsList(
        gameType: String,
        matchType: String,
        startTime: String? = null,
        endTime: String?=null,
        matchIdList:List<String>?=null
    ) {
        var playCateMenuCode = MenuCode.MAIN.code
        if (matchType == MatchType.CS.postValue) {
            playCateMenuCode = MenuCode.CS.code
        } else if (matchType == MatchType.END_SCORE.postValue) {
            playCateMenuCode = MatchType.END_SCORE.postValue
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
                            startTime = startTime,
                            endTime = endTime,
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
                            startTime = startTime,
                            endTime = endTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }
            }
            var leagueOddData = mutableListOf<LeagueOdd>()
            result?.oddsListData?.leagueOdds?.forEach {
                it.isExpanded = false
                it.league.firstCap = Pinyin.toPinyin(it.league.name.first()).first().toString()
                it.matchOdds.forEach {
                    it.isSelected = if (matchIdList.isNullOrEmpty()) true else matchIdList.contains(it.matchInfo?.id)
                }
                it.league.isSelected = if (matchIdList.isNullOrEmpty()) true else it.matchOdds.all { it.isSelected }
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

}