package org.cxct.sportlottery.ui.menu.results

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.matchresult.playlist.RvPosition
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData
import org.cxct.sportlottery.repository.SettlementRepository

class SettlementViewModel(private val settlementRepository: SettlementRepository) : ViewModel() {
    val settlementFilter: LiveData<SettlementFilter>
        get() = _settlementFilter
    val settlementData: LiveData<List<SettlementItem>>
        get() = _settlementData
    val matchResultListResult: LiveData<MatchResultListResult?>
        get() = _matchResultListResult
    val gameResultDetailResult: LiveData<SettlementRvData?>
        get() = _gameResultDetailResult


    private var _settlementFilter = MutableLiveData<SettlementFilter>()
    private val _settlementData = MutableLiveData<List<SettlementItem>>()
    private val _matchResultListResult = MutableLiveData<MatchResultListResult?>()
    private var _gameResultDetailResult = MutableLiveData<SettlementRvData?>(SettlementRvData(-1, -1, mutableMapOf()))

    lateinit var requestListener: ResultsSettlementActivity.RequestListener

    fun getSettlementData(
        gameType: String,
        pagingParams: PagingParams,
        timeRangeParams: TimeRangeParams
    ) {
        _settlementData.postValue(
            listOf(
                SettlementItem("FT", false),
                SettlementItem("BK", false),
                SettlementItem("TN", false),
                SettlementItem("BM", true),
                SettlementItem("VB", false)
            )
        )
        requestListener.requestIng(true)
        viewModelScope.launch {
            _matchResultListResult.postValue(
                settlementRepository.resultList(
                    pagingParams = pagingParams,
                    timeRangeParams = timeRangeParams,
                    gameType = gameType
                )
            )
        }
    }

    fun getSettlementDetailData(settleRvPosition: Int, gameResultRvPosition: Int, matchId: String) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            settlementRepository.resultPlayList(matchId)?.let { result ->
                _gameResultDetailResult.value?.apply {
                    this.settleRvPosition = settleRvPosition
                    this.gameResultRvPosition = gameResultRvPosition
                    this.settlementRvMap?.put(RvPosition(settleRvPosition, gameResultRvPosition), result)
                }
                _gameResultDetailResult.value = _gameResultDetailResult.value //touch observe
            }
        }
    }

    fun setGameTypeFilter(gameType: String) {
        //TODO Dean : 篩選後更新_settlementData
    }
}