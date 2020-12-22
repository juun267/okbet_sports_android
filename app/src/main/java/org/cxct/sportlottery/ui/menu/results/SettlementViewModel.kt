package org.cxct.sportlottery.ui.menu.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.repository.SettlementRepository

class SettlementViewModel(private val settlementRepository: SettlementRepository) : ViewModel() {
    val settlementFilter: LiveData<SettlementFilter>
        get() = _settlementFilter
    val settlementData: LiveData<List<SettlementItem>>
        get() = _settlementData
    val matchResultListResult: LiveData<MatchResultListResult?>
        get() = _matchResultListResult

    private var _settlementFilter = MutableLiveData<SettlementFilter>()
    private val _settlementData = MutableLiveData<List<SettlementItem>>()
    private val _matchResultListResult = MutableLiveData<MatchResultListResult?>()

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
        //TODO Dean : 串接api資料
    }

    fun setGameTypeFilter(gameType: String) {
        //TODO Dean : 篩選後更新_settlementData
    }
}