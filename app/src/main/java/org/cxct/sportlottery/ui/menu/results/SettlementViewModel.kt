package org.cxct.sportlottery.ui.menu.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.matchresult.list.Row
import org.cxct.sportlottery.network.matchresult.playlist.RvPosition
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData
import org.cxct.sportlottery.repository.SettlementRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class SettlementViewModel(private val settlementRepository: SettlementRepository) : BaseViewModel() {
    val matchResultListResult: LiveData<MatchResultListResult?>
        get() = _matchResultListResult
    val gameResultDetailResult: LiveData<SettlementRvData?>
        get() = _gameResultDetailResult
    val matchResultList: LiveData<List<Row>>
        get() = _matchResultList

    private val _matchResultListResult = MutableLiveData<MatchResultListResult?>()
    private var _gameResultDetailResult = MutableLiveData<SettlementRvData?>(SettlementRvData(-1, -1, mutableMapOf()))
    private val _matchResultList = MutableLiveData<List<Row>>()


    private var gameLeagueSet = mutableSetOf<Int>()
    private var gameKeyWord = ""

    lateinit var requestListener: ResultsSettlementActivity.RequestListener

    fun getSettlementData(gameType: String, pagingParams: PagingParams?, timeRangeParams: TimeRangeParams) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            val result = doNetwork {
                settlementRepository.resultList(pagingParams = pagingParams, timeRangeParams = timeRangeParams, gameType = gameType)
            }
            _matchResultListResult.postValue(result)
            filterResult()
            requestListener.requestIng(false)
            _gameResultDetailResult.value = SettlementRvData(-1, -1, mutableMapOf())  //要清空聯賽列表中比賽詳情的點選狀態
        }
    }

    fun getSettlementDetailData(settleRvPosition: Int, gameResultRvPosition: Int, matchId: String) {
        requestListener.requestIng(true)
        viewModelScope.launch {
            val result = doNetwork {
                settlementRepository.resultPlayList(matchId)
            }
            _gameResultDetailResult.postValue(_gameResultDetailResult.value?.apply {
                this.settleRvPosition = settleRvPosition
                this.gameResultRvPosition = gameResultRvPosition
                this.settlementRvMap[RvPosition(settleRvPosition, gameResultRvPosition)] = result
                requestListener.requestIng(false)
            })
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
    private fun filterResult() {
        _matchResultList.postValue(_matchResultListResult.value?.rows?.filterIndexed { index, row ->
            gameLeagueSet.contains(index) && (gameKeyWord.isEmpty() || row.league.name.contains(gameKeyWord))
        })
    }
}