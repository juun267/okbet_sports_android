package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.Order
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryRequest
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OrderData
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OtherBetHistoryDetailResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.TimeUtil

class OtherBetRecordViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val allPlatTag = "ALL_PLAT"

    companion object {
        private const val PAGE_SIZE = 20

    }

    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val recordResult: LiveData<OtherBetHistoryResult?>
        get() = _recordResult

    val recordDetailResult: LiveData<OtherBetHistoryDetailResult?>
        get() = _recordDetailResult

    val thirdGamesResult: LiveData<List<SheetData>?>
        get() = _thirdGamesResult

    private var _recordResult = MutableLiveData<OtherBetHistoryResult?>()
    private var _recordDetailResult = MutableLiveData<OtherBetHistoryDetailResult?>()
    private var _thirdGamesResult = MutableLiveData<List<SheetData>?>()
    private val _loading = MutableLiveData<Boolean>()

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Order>()
    val recordDetailDataList = mutableListOf<OrderData>()

    private var recordRequest: OtherBetHistoryRequest? = null

    fun getThirdGames() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                hideLoading()

                val thirdGameList = mutableListOf<GameFirmValues>()
                val resultList = mutableListOf<SheetData>()

                resultList.add(SheetData(allPlatTag, androidContext.getString(R.string.all_plat_type)))
                for ((key, value) in result.t?.gameFirmMap ?: mapOf()) {
                    if (value.open == 1) {
                        thirdGameList.add(value)
                    }
                }

                thirdGameList.sortedBy { it.sort }.forEach {
                    resultList.add(SheetData(it.firmType, it.firmShowName))
                }

                _thirdGamesResult.value = resultList.distinct()
            }
        }
    }

    fun queryFirstOrders(page: Int? = 1, startTime: String? = TimeUtil.getDefaultTimeStamp().startTime, endTime: String? = TimeUtil.getDefaultTimeStamp().endTime, firmType: String? = null) {
        loading()

        if (page == 1) {
            nowPage = 1
            recordDataList.clear()
        }

        val filter = { firm: String? -> if (firm == allPlatTag || firm.isNullOrEmpty()) null else firm }

        viewModelScope.launch {
            doNetwork(androidContext) {
                recordRequest = OtherBetHistoryRequest(
                    page, PAGE_SIZE,
                    startTime = startTime,
                    endTime = endTime,
                    firmType = filter(firmType)
                )

                recordRequest.let { OneBoSportApi.thirdGameService.queryFirstOrders(it) }

            }?.let { result ->
                hideLoading()
                isLoading = false
                recordDataList.addAll(result.t?.orderList as List<Order>)
                isLastPage = (recordDataList.size >= (result.t.totalCount ?: 0))
                _recordResult.value = result
            }
        }
    }

    fun querySecondOrders(today: String? = null) {
        querySecondOrders(startTime = today?.let { TimeUtil.getDayDateTimeRangeParams(it).startTime },
            endTime = today?.let { TimeUtil.getDayDateTimeRangeParams(it).endTime })
    }

    fun querySecondOrders(page: Int? = 1, startTime: String? = null, endTime: String? = null, firmType: String? = null) {
        loading()
        if (page == 1) {
            nowPage = 1
            recordDetailDataList.clear()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                recordRequest = OtherBetHistoryRequest(
                    page, PAGE_SIZE,
                    startTime = startTime,
                    endTime = endTime,
                    firmType = firmType
                )

                recordRequest.let { OneBoSportApi.thirdGameService.querySecondOrders(it) }

            }?.let { result ->
                hideLoading()
                isLoading = false
                recordDetailDataList.addAll(result.t?.orderList?.dataList as List<OrderData>)
                isLastPage = (recordDetailDataList.size >= (result.t.totalCount ?: 0))

                _recordDetailResult.value = result
            }
        }
    }

    fun getRecordNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
                recordRequest?.let { queryFirstOrders(++nowPage, it.startTime, it.endTime, it.firmType) }
            }
        }
    }

    fun getRecordDetailNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
                recordRequest?.let { querySecondOrders(++nowPage, it.startTime, it.endTime, it.firmType) }
            }
        }
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }

}
