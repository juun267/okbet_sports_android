package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.Order
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryRequest
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OrderData
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OtherBetHistoryDetailResult
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import java.util.*


class OtherBetRecordViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    val allPlatTag = "ALL_PLAT"

    private val filter = { firm: String? -> if (firm == allPlatTag || firm.isNullOrEmpty()) null else firm }

    companion object {
        private const val PAGE_SIZE = 20

    }


    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val recordResult: LiveData<OtherBetHistoryResult?>
        get() = _recordResult

    val recordDetailResult: LiveData<OtherBetHistoryDetailResult?>
        get() = _recordDetailResult

    val thirdGamesResult: LiveData<List<StatusSheetData>?>
        get() = _thirdGamesResult

    val lastPage: LiveData<Boolean>
        get() = _lastPage

    private var _recordResult = MutableLiveData<OtherBetHistoryResult?>()
    private var _recordDetailResult = MutableLiveData<OtherBetHistoryDetailResult?>()
    private var _thirdGamesResult = MutableLiveData<List<StatusSheetData>?>()
    private val _loading = MutableLiveData<Boolean>()
    private val _lastPage = MutableLiveData<Boolean>()

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
                val resultList = mutableListOf<StatusSheetData>()

                resultList.add(StatusSheetData(allPlatTag, androidContext.getString(R.string.all_plat_type)))
                for ((key, value) in result.t?.gameFirmMap ?: mapOf()) {
                    if (value.open == 1) {
                        thirdGameList.add(value)
                    }
                }

                thirdGameList.sortedBy { it.sort }.forEach {
                    resultList.add(StatusSheetData(it.firmType, it.firmName))
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
                if (!result?.success){
                    ToastUtil.showToast(androidContext,result.msg)
                    return@let
                }
                result.t?.orderList?.let {
                    recordDataList.addAll(it)
                }
                isLastPage = (recordDataList.size >= (result.t?.totalCount ?: 0))
                _lastPage.value = isLastPage
                _recordResult.value = result
            }
        }
    }

    fun querySecondOrders(firmType: String? = null, today: String? = null) {
        val timeZone = TimeZone.getTimeZone(TimeUtil.TIMEZONE_DEFAULT)
        val startTime =
            TimeUtil.dateToTimeStamp(today, TimeUtil.TimeType.START_OF_DAY, timeZone = timeZone)
                .toString()
        val endTime =
            TimeUtil.dateToTimeStamp(today, TimeUtil.TimeType.END_OF_DAY, timeZone = timeZone)
                .toString()
        querySecondOrders(startTime = startTime, endTime = endTime, firmType = firmType)
    }

    private fun querySecondOrders(page: Int? = 1, startTime: String? = null, endTime: String? = null, firmType: String? = null) {
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
                    firmType = filter(firmType)
                )

                recordRequest.let { OneBoSportApi.thirdGameService.querySecondOrders(it) }

            }?.let { result ->
                hideLoading()
                isLoading = false
                if (!result?.success){
                    ToastUtil.showToast(androidContext,result.msg)
                    return@let
                }
                result.t?.orderList?.dataList?.let { recordDetailDataList.addAll(it) }
                isLastPage = (recordDetailDataList.size >= (result.t?.totalCount ?: 0))
                _lastPage.value = isLastPage
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
