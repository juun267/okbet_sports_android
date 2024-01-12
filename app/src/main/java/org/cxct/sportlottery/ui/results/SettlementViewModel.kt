package org.cxct.sportlottery.ui.results

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchInfo
import org.cxct.sportlottery.network.matchresult.list.MatchResultList
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData


class SettlementViewModel(
    androidContext: Application,
    private val settlementRepository: SettlementRepository,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    private val _matchResultPlayListResult = MutableLiveData<MatchResultPlayListResult>()
    val matchResultPlayListResult: LiveData<MatchResultPlayListResult>
        get() = _matchResultPlayListResult

    private var matchResultReformatted = mutableListOf<MatchResultData>() //重構後的資料結構
    private val _showMatchResultData = MutableLiveData<List<MatchResultData>>() //過濾後的資料
    val showMatchResultData: LiveData<List<MatchResultData>>
        get() = _showMatchResultData

    //冠軍重構資料結構
    private var outrightDataReformatted = mutableListOf<OutrightResultData>()
    private val _showOutrightData = MutableLiveData<List<OutrightResultData>>()
    val showOutrightData: LiveData<List<OutrightResultData>>
        get() = _showOutrightData

    private val _leagueFilterList = MutableLiveData<MutableList<LeagueItemData>>() //聯賽過濾器的清單
    val leagueFilterList: LiveData<MutableList<LeagueItemData>>
        get() = _leagueFilterList

    private var dataType = SettleType.MATCH

    //filter condition
    private var gameLeagueSet = mutableSetOf<String>()
    private var gameKeyWord = ""

//    val sportCodeList: LiveData<List<StatusSheetData>>
//        get() = _sportCodeSpinnerList
//    private val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單

    lateinit var requestListener: ResultsSettlementActivity.RequestListener

    val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單
    val sportCodeList: LiveData<List<StatusSheetData>>
        get() = _sportCodeSpinnerList

    fun getMatchResultList(
        gameType: String,
        pagingParams: PagingParams?,
        timeRangeParams: TimeRangeParams,
        matchId:String? = null,
        leagueId:String? = null,
    ) {
        dataType = SettleType.MATCH
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultList(
                    pagingParams = pagingParams,
                    timeRangeParams = timeRangeParams,
                    gameType = gameType,
                    matchId = matchId,
                    leagueId  = leagueId
                )
            }?.let { result ->
                reformatMatchResultData(result.matchResultList).let {
                    matchResultReformatted = it
                    //獲取賽果資料後,更新聯賽列表
                    setupLeagueFilterList(it)
                    //過濾資料
                    filterToShowMatchResult()
                }
            }
            requestListener.requestIng(false)
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
                matchResultList.list.forEachIndexed { index, match ->
                    val matchData = MatchResultData(ListType.MATCH, matchData = match)
                    if (index == matchResultList.list.size - 1) {
                        matchData.isLastMatchData = true
                    }
                    matchResultData.add(matchData)
                }
            }
        }
        return matchResultData
    }

    private fun setupExpandData(matchResultList: List<MatchResultData>?) {
        val expandData = mutableListOf<MatchResultData>()
        var showMatch = false
        var showMatchByTeamName = false
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
                        showMatchByTeamName = if (!it.leagueShow && it.leagueShowByTeamName) {
                            expandData.add(it)
                            it.titleExpanded
                        } else {
                            false
                        }
                    }
                    ListType.MATCH -> {
                        //如果關鍵字匹配的是聯賽名稱，顯示聯賽下所有賽事
                        if (nowLeague?.leagueShow == true && showMatch) {
                            expandData.add(it)
                            showDetail = it.matchExpanded
                        }
                        //如果關鍵字只匹配到比賽隊伍名，就只顯示包含該球隊名的比賽
                        if (nowLeague?.leagueShowByTeamName == true && showMatchByTeamName) {
                            if (filterTeamNameByKeyWord(it.matchData?.matchInfo, gameKeyWord)) {
                                expandData.add(it)
                                showDetail = it.matchExpanded
                            }
                        }
                    }
                    ListType.FIRST_ITEM_FT, ListType.FIRST_ITEM_BK, ListType.FIRST_ITEM_TN, ListType.FIRST_ITEM_BM, ListType.FIRST_ITEM_VB, ListType.DETAIL,
                    ListType.FIRST_ITEM_TT, ListType.FIRST_ITEM_IH, ListType.FIRST_ITEM_BX, ListType.FIRST_ITEM_CB, ListType.FIRST_ITEM_CK, ListType.FIRST_ITEM_BB,
                    ListType.FIRST_ITEM_RB, ListType.FIRST_ITEM_MR, ListType.FIRST_ITEM_GF, ListType.FIRST_ITEM_AFT -> {
                        if (showDetail) {
                            expandData.add(it)
                        }
                    }
                    else -> {
                        if (showDetail) {
                            expandData.add(it)
                        }
                    }
                }
            }
        }
        _showMatchResultData.value = expandData
    }

    private fun setupExpandOutrightData(outrightResultData: MutableList<OutrightResultData>) {
        val showData = mutableListOf<OutrightResultData>()
        var showOutright = false
        var nowSeason: OutrightResultData? = null

        outrightResultData.forEach {
            when (it.dataType) {
                OutrightType.TITLE -> {
                    nowSeason = it
                    showOutright = if (it.seasonShow) {
                        showData.add(it)
                        it.seasonExpanded
                    } else {
                        false
                    }
                }
                OutrightType.OUTRIGHT -> {
                    if (nowSeason?.seasonShow == true && showOutright) {
                        showData.add(it)
                    }
                }
                else -> {
                }
            }
        }
        _showOutrightData.value = showData
    }

    fun clickResultItem(gameType: String? = null, expandPosition: Int) {
        val clickedItem = showMatchResultData.value?.getOrNull(expandPosition)

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
        if (clickedItem.matchExpanded && if (clickedIndex + 1 < matchResultReformatted.size) matchResultReformatted.getOrNull(clickedIndex + 1)?.dataType != listType else true) {
            clickedItem.matchData?.matchInfo?.id?.let { getMatchDetail(it, clickedItem, gameType) }
        } else {
            filterToShowMatchResult()
        }
    }

    fun clickOutrightItem(clickedItem: OutrightResultData) {
        clickSeasonExpand(clickedItem)
    }

    private fun clickSeasonExpand(clickedItem: OutrightResultData) {
        outrightDataReformatted.find { it == clickedItem }?.let { it.seasonExpanded = !(it.seasonExpanded) }
        filterToShowOutrightResult()
    }

    private fun getMatchDetail(matchId: String, clickedItem: MatchResultData, gameType: String) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            doNetwork(androidContext) {
                settlementRepository.resultPlayList(matchId)
            }?.let { result ->
                if (result.success)
                    makeUpMatchDetailData(result.matchResultPlayList, clickedItem, gameType)
                else _matchResultPlayListResult.postValue(result)
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
            GameType.VB.key -> ListType.FIRST_ITEM_VB
            GameType.TT.key -> ListType.FIRST_ITEM_TT
            GameType.IH.key -> ListType.FIRST_ITEM_IH
            GameType.BX.key -> ListType.FIRST_ITEM_BX
            GameType.CB.key -> ListType.FIRST_ITEM_CB
            GameType.CK.key -> ListType.FIRST_ITEM_CK
            GameType.BB.key -> ListType.FIRST_ITEM_BB
            GameType.RB.key -> ListType.FIRST_ITEM_RB
            GameType.MR.key -> ListType.FIRST_ITEM_MR
            GameType.GF.key -> ListType.FIRST_ITEM_GF
            GameType.ES.key -> ListType.FIRST_ITEM_ES
            GameType.AFT.key -> ListType.FIRST_ITEM_AFT
            GameType.BM.key -> ListType.FIRST_ITEM_BM
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
                //重組資料結構
                reformatOutrightResultData(result).let {
                    outrightDataReformatted = it
                    //更新聯賽篩選清單
                    setupOutrightLeagueFilterList(it)
                    //過濾冠軍資料
                    filterToShowOutrightResult()
                }
            }
            requestListener.requestIng(false)
        }
    }

    private fun reformatOutrightResultData(result: OutrightResultListResult): MutableList<OutrightResultData> {
        val dataList = result.rows
        val reformatDataList: MutableList<OutrightResultData> = mutableListOf()
        dataList?.forEach { data ->
            reformatDataList.add(OutrightResultData(OutrightType.TITLE, seasonData = data.season))
            data.resultList.forEachIndexed { index, outright ->
                val outrightData = OutrightResultData(OutrightType.OUTRIGHT, seasonData = data.season, outrightData = outright)
                if (index == data.resultList.size - 1) {
                    outrightData.isLastOutrightData = true
                }
                reformatDataList.add(outrightData)
            }
        }
        return reformatDataList
    }

    private fun setupOutrightLeagueFilterList(outrightDataList: MutableList<OutrightResultData>) {
        _leagueFilterList.value = outrightDataList.filter {
            it.dataType == OutrightType.TITLE
        }.map { outright ->
            LeagueItemData(null, outright.seasonData?.name ?: "", true)
        }.toMutableList()
    }

    /**
     * 設置聯盟篩選條件
     */
    fun setLeagueFilter(gameLeaguePosition: MutableSet<String>) {
        gameLeagueSet = gameLeaguePosition
        when (dataType) {
            SettleType.MATCH -> filterToShowMatchResult()
            SettleType.OUTRIGHT -> filterToShowOutrightResult()
        }
    }

    /**
     * 設置關鍵字篩選條件
     */
    fun setKeyWordFilter(keyWord: String) {
        gameKeyWord = keyWord
        when (dataType) {
            SettleType.MATCH -> filterToShowMatchResult()
            SettleType.OUTRIGHT -> filterToShowOutrightResult()
        }
    }

    /**
     * 依選擇聯盟、關鍵字進行篩選做資料顯示
     */
    @SuppressLint("DefaultLocale")
    private fun filterResult() {
        var nowLeagueItem: MatchResultData? = null
        var nowOutrightItem: OutrightResultData? = null
        when (dataType) {
            SettleType.MATCH -> {
                matchResultReformatted.forEachIndexed { index, matchResultData ->
                    when (matchResultData.dataType) {
                        ListType.TITLE -> {
                            nowLeagueItem = matchResultData
                            matchResultData.leagueShow = gameLeagueSet.contains(matchResultData.titleData?.name) &&
                                    (gameKeyWord.isEmpty() || (matchResultData.titleData?.name?.toLowerCase()?.contains(gameKeyWord.toLowerCase()) == true))

                        }
                        ListType.MATCH -> {
                            if (filterTeamNameByKeyWord(matchResultData.matchData?.matchInfo, gameKeyWord)) {
                                nowLeagueItem?.leagueShowByTeamName = true
                            }
                        }
                        else -> {

                        }
                    }
                }
            }
            SettleType.OUTRIGHT -> {
                outrightDataReformatted.forEach { outrightResultData ->
                    when (outrightResultData.dataType) {
                        OutrightType.TITLE -> {
                            nowOutrightItem = outrightResultData
                            outrightResultData.seasonShow = gameLeagueSet.contains(outrightResultData.seasonData?.name) &&
                                    (gameKeyWord.isEmpty() || (outrightResultData.seasonData?.name?.toLowerCase()?.contains(gameKeyWord.toLowerCase()) == true))
                        }
                        OutrightType.OUTRIGHT -> {
                            if (filterOutrightByKeyWord(outrightResultData, gameKeyWord)) {
                                nowOutrightItem?.seasonShow = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun filterToShowMatchResult() {
        filterResult()
        setupExpandData(matchResultReformatted)
    }

    private fun filterToShowOutrightResult() {
        filterResult()
        setupExpandOutrightData(outrightDataReformatted)
    }

    @SuppressLint("DefaultLocale")
    private fun filterOutrightByKeyWord(outrightResultData: OutrightResultData, keyWord: String): Boolean {
        val result = outrightResultData.outrightData
        if (keyWord.isEmpty())
            return false
        if (outrightResultData.seasonData?.name?.toLowerCase()?.contains(keyWord.toLowerCase()) == true ||
            result?.playName?.toLowerCase()?.contains(keyWord.toLowerCase()) == true ||
            result?.playCateName?.toLowerCase()?.contains(keyWord.toLowerCase()) == true
        )
            return true

        return false
    }

    /**
     * 獲取當前可用球種清單
     */
    fun getSportList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportList()//BUG修复删除参数 后续需要找产品确认
            }?.let { sportListResponse ->
                if (sportListResponse.success) {
                    val sportCodeList = mutableListOf<StatusSheetData>()
                    //根據api回傳的球類添加進當前啟用球種篩選清單
                    sportListResponse.rows.sortedBy { it.sortNum }.map {
                        sportCodeList.add(
                            StatusSheetData(
                                it.code,
                                GameType.getGameTypeString(androidContext, it.code)
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        _sportCodeSpinnerList.value = sportCodeList
                    }
                }
            }
        }
    }

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
