package org.cxct.sportlottery.ui.results

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchInfo
import org.cxct.sportlottery.network.matchresult.list.MatchResultList
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SettlementRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel


class SettlementViewModel(
    private val androidContext: Context,
    private val settlementRepository: SettlementRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val gameResultDetailResult: LiveData<SettlementRvData>
        get() = _gameResultDetailResult
    val outRightListResult: LiveData<OutrightResultListResult>
        get() = _outRightListResult
    val outRightList: LiveData<List<org.cxct.sportlottery.network.outright.Row>>
        get() = _outRightList

    private var _gameResultDetailResult = MutableLiveData<SettlementRvData>(SettlementRvData(-1, -1, mutableMapOf()))

    private var _outRightListResult = MutableLiveData<OutrightResultListResult>()
    private val _outRightList = MutableLiveData<List<org.cxct.sportlottery.network.outright.Row>>()


    private var matchResultReformatted = mutableListOf<MatchResultData>() //重構後的資料結構
    private val _showMatchResultData = MutableLiveData<List<MatchResultData>>() //過濾後的資料
    val showMatchResultData: LiveData<List<MatchResultData>>
        get() = _showMatchResultData

    private val _leagueFilterList = MutableLiveData<MutableList<LeagueItemData>>() //聯賽過濾器的清單
    val leagueFilterList: LiveData<MutableList<LeagueItemData>>
        get() = _leagueFilterList

    private var dataType = SettleType.MATCH

    private var gameLeagueSet = mutableSetOf<String>()
    private var gameKeyWord = ""

    lateinit var requestListener: ResultsSettlementActivity.RequestListener

    fun getMatchResultList(gameType: String, pagingParams: PagingParams?, timeRangeParams: TimeRangeParams) {
        dataType = SettleType.MATCH
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultList(
                    pagingParams = pagingParams,
                    timeRangeParams = timeRangeParams,
                    gameType = gameType
                )
            }?.let { result ->
                reformatMatchResultData(result.matchResultList).let {
                    matchResultReformatted = it
                    //TODO Dean : review 過濾資料
                    //獲取賽果資料後,更新聯賽列表
                    setupLeagueFilterList(it)
                    //過濾資料
                    filterToShowMatchResult()
                }
            }
            requestListener.requestIng(false)
//            reSetDetailStatus()
        }
    }

    private fun setupLeagueFilterList(matchResultData: List<MatchResultData>) {
        _leagueFilterList.value = matchResultData.filter {
            it.dataType == ListType.TITLE
        }.map { rows ->
            LeagueItemData(null, rows.titleData?.name ?: "", true)
        }.toMutableList()
    }

    private fun reformatMatchResultData(matchResultList: List<MatchResultList>?): MutableList<MatchResultData> {
        val matchResultData = mutableListOf<MatchResultData>()
        matchResultList?.let { resultList ->
            resultList.forEach { matchResultList ->
                matchResultData.add(MatchResultData(ListType.TITLE, titleData = matchResultList.league))
                matchResultList.list.forEach { match ->
                    matchResultData.add(MatchResultData(ListType.MATCH, matchData = match))
                }
            }
        }
        return matchResultData
    }

    private fun setupExpandData(matchResultList: List<MatchResultData>?) {
        val expandData = mutableListOf<MatchResultData>()
        var showMatch: Boolean = false
        var showDetail = false
        var nowLeague: MatchResultData? = null
        matchResultList?.apply {
            forEach {
                when (it.dataType) {
                    ListType.TITLE -> {
                        nowLeague = it
                        showMatch = if (it.leagueShow) {
                            expandData.add(it)
                            it.titleExpanded
                        } else {
                            false
                        }
                    }
                    ListType.MATCH -> {
                        if (nowLeague?.leagueShow == true && showMatch) {
                            expandData.add(it)
                            showDetail = it.matchExpanded
                        }
                    }
                    ListType.FIRST_ITEM_FT, ListType.FIRST_ITEM_BK, ListType.FIRST_ITEM_TN, ListType.FIRST_ITEM_BM, ListType.FIRST_ITEM_VB, ListType.DETAIL -> {
                        if (showDetail) {
                            expandData.add(it)
                        }
                    }
                }
            }
        }
        _showMatchResultData.value = expandData
    }

    fun clickResultItem(gameType: String? = null, expandPosition: Int) {
        val clickedItem = showMatchResultData.value?.get(expandPosition)

        when (clickedItem?.dataType) {
            ListType.TITLE -> {
                clickLeagueExpand(clickedItem)
            }
            ListType.MATCH -> {
                clickMatchExpand(gameType ?: "", clickedItem)
            }
            else -> {
                /*do nothing*/
            }
        }
    }

    private fun clickLeagueExpand(clickedItem: MatchResultData) {
        matchResultReformatted.find { it == clickedItem }?.let { it.titleExpanded = !(it.titleExpanded) }
        filterToShowMatchResult()
    }

    private fun clickMatchExpand(gameType: String, clickedItem: MatchResultData) {
        matchResultReformatted.find { it == clickedItem }?.let { it.matchExpanded = !(it.matchExpanded) }
        val listType = getFirstItemListType(gameType)
        val clickedIndex = matchResultReformatted.indexOf(clickedItem)

        //若資料已存在則不再一次請求資料
        if (clickedItem.matchExpanded && if (clickedIndex + 1 < matchResultReformatted.size) matchResultReformatted.get(clickedIndex + 1).dataType != listType else true) {
            clickedItem.matchData?.matchInfo?.id?.let { getMatchDetail(it, clickedItem, gameType) }
        } else {
            filterToShowMatchResult()
        }
    }

    private fun getMatchDetail(matchId: String, clickedItem: MatchResultData, gameType: String) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultPlayList(matchId)
            }?.let { result ->
                makeUpMatchDetailData(result.matchResultPlayList, clickedItem, gameType)
            }
            requestListener.requestIng(false)
        }
    }

    private fun makeUpMatchDetailData(matchResultPlayList: List<MatchResultPlayList>? = null, clickedItem: MatchResultData, gameType: String) {
        val listType = getFirstItemListType(gameType)
        val clickedIndex = matchResultReformatted.indexOf(clickedItem)
        matchResultPlayList?.asReversed()?.forEach { matchResultReformatted.add(clickedIndex + 1, MatchResultData(ListType.DETAIL, matchDetailData = it)) }
        matchResultReformatted.add(clickedIndex + 1, MatchResultData(listType, matchData = clickedItem.matchData))
        filterToShowMatchResult()
    }

    private fun getFirstItemListType(gameType: String): ListType {
        return when (gameType) {
            GameType.FT.key -> ListType.FIRST_ITEM_FT
            GameType.BK.key -> ListType.FIRST_ITEM_BK
            GameType.TN.key -> ListType.FIRST_ITEM_TN
            GameType.BM.key -> ListType.FIRST_ITEM_BM
            GameType.VB.key -> ListType.FIRST_ITEM_VB
            else -> ListType.DETAIL
        }
    }

    fun getOutrightResultList(gameType: String) {
        dataType = SettleType.OUTRIGHT
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultOutRightList(gameType = gameType)
            }?.let { result ->
                _outRightListResult.postValue(result)
            }

//            filterResult() //TODO Dean : review
            requestListener.requestIng(false)
//            reSetDetailStatus() //TODO Dean : review
        }
    }

    /**
     * 設置聯盟篩選條件
     */
    fun setLeagueFilter(gameLeaguePosition: MutableSet<String>) {
        gameLeagueSet = gameLeaguePosition
        filterToShowMatchResult()
    }

    /**
     * 設置關鍵字篩選條件
     */
    fun setKeyWordFilter(keyWord: String) {
        gameKeyWord = keyWord
        filterToShowMatchResult()
    }

    /**
     * 依選擇聯盟、關鍵字進行篩選做資料顯示
     */
    @SuppressLint("DefaultLocale")
    private fun filterResult() {
        var nowLeagueItem: MatchResultData? = null
        when (dataType) {
            SettleType.MATCH -> {
                matchResultReformatted.forEachIndexed { index, matchResultData ->
                    when (matchResultData.dataType) {
                        ListType.TITLE -> {
                            nowLeagueItem = matchResultData
                            matchResultData.leagueShow = gameLeagueSet.contains(matchResultData.titleData?.name) &&
                                    (gameKeyWord.isEmpty() || (matchResultData.titleData?.name?.toLowerCase()?.contains(gameKeyWord) == true))

                        }
                        ListType.MATCH -> {
                            if (filterTeamNameByKeyWord(matchResultData.matchData?.matchInfo, gameKeyWord)) {
                                nowLeagueItem?.leagueShow = true
                            }
                        }
                        else -> {

                        }
                    }
                }
                /*_matchResultListResult.value?.matchResultList?.filterIndexed { index, row ->
                    gameLeagueSet.contains(index) && (gameKeyWord.isEmpty() || row.league.name.toLowerCase().contains(gameKeyWord) || filterTeamNameByKeyWord(row, gameKeyWord))
                }*/
            }
            SettleType.OUTRIGHT -> {
                /*_outRightList.postValue(_outRightListResult.value?.rows?.filterIndexed { index, row ->
                    gameLeagueSet.contains(index) && (gameKeyWord.isEmpty() || row.season.name.toLowerCase().contains(gameKeyWord))
                })*/
            }
        }
    }

    private fun filterToShowMatchResult() {
        filterResult()
        setupExpandData(matchResultReformatted)
    }

    @SuppressLint("DefaultLocale")
    private fun filterTeamNameByKeyWord(matchInfo: MatchInfo?, keyWord: String): Boolean {
        if (keyWord.isEmpty())
            return false
        if (matchInfo?.homeName?.toLowerCase()?.contains(keyWord.toLowerCase()) == true) {
            return true
        }
        if (matchInfo?.awayName?.toLowerCase()?.contains(keyWord.toLowerCase()) == true) {
            return true
        }
        return false
    }
}