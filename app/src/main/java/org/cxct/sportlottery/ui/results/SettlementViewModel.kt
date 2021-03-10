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
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.matchresult.list.MatchResultList
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import org.cxct.sportlottery.network.matchresult.playlist.RvPosition
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

    val matchResultListResult: LiveData<MatchResultListResult>
        get() = _matchResultListResult
    val gameResultDetailResult: LiveData<SettlementRvData>
        get() = _gameResultDetailResult
    val outRightListResult: LiveData<OutrightResultListResult>
        get() = _outRightListResult
    val outRightList: LiveData<List<org.cxct.sportlottery.network.outright.Row>>
        get() = _outRightList

    private val _matchResultListResult = MutableLiveData<MatchResultListResult>()
    private var _gameResultDetailResult = MutableLiveData<SettlementRvData>(SettlementRvData(-1, -1, mutableMapOf()))

    private var _outRightListResult = MutableLiveData<OutrightResultListResult>()
    private val _outRightList = MutableLiveData<List<org.cxct.sportlottery.network.outright.Row>>()


    private var matchResultReformatted = mutableListOf<MatchResultData>() //重構後的資料結構
    private val _showMatchResultData = MutableLiveData<List<MatchResultData>>() //過濾後的資料
    val showMatchResultData: LiveData<List<MatchResultData>>
        get() = _showMatchResultData

    private var dataType = SettleType.MATCH

    private var gameLeagueSet = mutableSetOf<Int>()
    private var gameKeyWord = ""

    lateinit var requestListener: ResultsSettlementActivity.RequestListener

    @Deprecated("重構, 使用新方法getMatchResultList")
    fun getSettlementData(gameType: String, pagingParams: PagingParams?, timeRangeParams: TimeRangeParams) {
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
                _matchResultListResult.postValue(result)
            }

            filterResult()
            requestListener.requestIng(false)
            reSetDetailStatus()
        }
    }

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
                    filterMatchResultData(it)
                }
            }

            filterResult()
            requestListener.requestIng(false)
            reSetDetailStatus()
        }
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

    private fun filterMatchResultData(matchResultList: List<MatchResultData>?) {
        val filteredData = mutableListOf<MatchResultData>()
        var showMatch: Boolean = false
        var showDetail = false
        matchResultList?.apply {
            forEach {
                when (it.dataType) {
                    ListType.TITLE -> {
                        filteredData.add(it)
                        showMatch = it.titleExpanded
                    }
                    ListType.MATCH -> {
                        Log.e("Dean", "showMatch before= $showMatch , showDetail = $showDetail")
                        if (showMatch) {
                            filteredData.add(it)
                            showDetail = it.matchExpanded
                        }
                        Log.e("Dean", "showMatch after= $showMatch , showDetail = $showDetail")
                    }
                    ListType.FIRST_ITEM_FT, ListType.FIRST_ITEM_BK, ListType.FIRST_ITEM_TN, ListType.FIRST_ITEM_BM, ListType.FIRST_ITEM_VB, ListType.DETAIL -> {
                        Log.e("Dean", "FirstItem showDetail = $showDetail")
                        if (showDetail) {
                            filteredData.add(it)
                        }
                    }
                }
            }
        }
        _showMatchResultData.value = filteredData
    }

    fun clickResultItem(gameType: String? = null, expandPosition: Int) {
        Log.e("Dean", "clickExpandData = $expandPosition")
        val clickedItem = showMatchResultData.value?.get(expandPosition)
        val showData = showMatchResultData.value?.toMutableList()
        val addData = mutableListOf<MatchResultData>()
        Log.e("Dean", "clickedItem = $clickedItem , addData = $addData")

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
        Log.e("Dean", "check clickedItem = $clickedItem")
        matchResultReformatted.find { it == clickedItem }?.let { it.titleExpanded = !(it.titleExpanded) }
        filterMatchResultData(matchResultReformatted)
    }

    private fun clickMatchExpand(gameType: String, clickedItem: MatchResultData) {
        matchResultReformatted.find { it == clickedItem }?.let { it.matchExpanded = !(it.matchExpanded) }
        val listType = getFirstItemListType(gameType)
        val clickedIndex = matchResultReformatted.indexOf(clickedItem)

        //若資料已存在則不再一次請求資料
        if (clickedItem.matchExpanded && if (clickedIndex + 1 < matchResultReformatted.size) matchResultReformatted.get(clickedIndex + 1).dataType != listType else true) {
            clickedItem.matchData?.matchInfo?.id?.let { getMatchDetail(it, clickedItem, gameType) }
        } else {
            filterMatchResultData(matchResultReformatted)
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
        filterMatchResultData(matchResultReformatted)
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

    @Deprecated("棄用,改使用getMatchDetail")
    fun getSettlementDetailData(settleRvPosition: Int, gameResultRvPosition: Int, matchId: String) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultPlayList(matchId)
            }?.let { result ->
                _gameResultDetailResult.postValue(_gameResultDetailResult.value?.apply {
                    this.settleRvPosition = settleRvPosition
                    this.gameResultRvPosition = gameResultRvPosition
                    this.settlementRvMap[RvPosition(settleRvPosition, gameResultRvPosition)] = result
                })
            }
            requestListener.requestIng(false)
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

            filterResult()
            requestListener.requestIng(false)
            reSetDetailStatus()
        }
    }

    /**
     * 設置聯盟篩選條件
     */
    fun setLeagueFilter(gameLeaguePosition: MutableSet<Int>) {
        gameLeagueSet = gameLeaguePosition
        filterResult()
    }

    /**
     * 設置關鍵字篩選條件
     */
    fun setKeyWordFilter(keyWord: String) {
        gameKeyWord = keyWord
        filterResult()
    }

    /**
     * 依選擇聯盟、關鍵字進行篩選做資料顯示
     */
    @SuppressLint("DefaultLocale")
    private fun filterResult() {
        when (dataType) {
            SettleType.MATCH -> {
                /*_matchResultList.postValue(_matchResultListResult.value?.matchResultList?.filterIndexed { index, row ->
                    gameLeagueSet.contains(index) && (gameKeyWord.isEmpty() || row.league.name.toLowerCase().contains(gameKeyWord) || filterTeamNameByKeyWord(row, gameKeyWord))
                })*/
            }
            SettleType.OUTRIGHT -> {
                _outRightList.postValue(_outRightListResult.value?.rows?.filterIndexed { index, row ->
                    gameLeagueSet.contains(index) && (gameKeyWord.isEmpty() || row.season.name.toLowerCase().contains(gameKeyWord))
                })
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun filterTeamNameByKeyWord(matchResultList: MatchResultList, keyWord: String): Boolean {
        matchResultList.list.forEach { match ->
            if (match.matchInfo.homeName.toLowerCase().contains(keyWord.toLowerCase())) {
                return true
            }
            if (match.matchInfo.awayName.toLowerCase().contains(keyWord.toLowerCase())) {
                return true
            }
        }
        return false
    }

    /**
     * 重新獲取聯賽資料時須要清空內部比賽詳情點選狀態及資料
     */
    private fun reSetDetailStatus() {
        _gameResultDetailResult.value = SettlementRvData(-1, -1, mutableMapOf())  //要清空聯賽列表中比賽詳情的點選狀態
    }
}