package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.Order
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryRequest
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

class OtherBetRecordViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {


    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val recordResult: LiveData<OtherBetHistoryResult?>
        get() = _recordResult

    val thirdGamesResult: LiveData<List<SheetData>?>
        get() = _thirdGamesResult

    private var _recordResult = MutableLiveData<OtherBetHistoryResult?>()//List<Order>
    private var _thirdGamesResult = MutableLiveData<List<SheetData>?>()
    private val _loading = MutableLiveData<Boolean>()

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Order>()

    private var recordRequest: OtherBetHistoryRequest? = null

    fun getThirdGames() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                hideLoading()

                val resultList = mutableListOf<SheetData>()
                resultList.add(SheetData("ALL_PLAT", androidContext.getString(R.string.all_plat_type)))
                for ((key, value) in result.t?.gameFirmMap?: mapOf()) {
                        resultList.add(SheetData(value.firmType, value.firmShowName))
                }

                _thirdGamesResult.value = resultList.distinct()
            }
        }
    }

    fun queryFirstOrders(page: Int? = 1, startTime: String ?= null, endTime: String ?= null, firmType: String ?= null) {
        loading()
        if (page == 1) {
            nowPage = 1
            recordDataList.clear()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                recordRequest = OtherBetHistoryRequest(page, PAGE_SIZE,
                                           startTime = startTime,
                                           endTime = endTime,
                                           firmType = firmType)

                recordRequest.let { OneBoSportApi.thirdGameService.queryFirstOrders(it) }

            }?.let { result ->
                hideLoading()
                isLoading = false
                recordDataList.addAll(result.t?.orderList as List<Order>)
                _recordResult.value = result
            }
        }
    }

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
                recordRequest?.let { queryFirstOrders(++nowPage, it.startTime, it.endTime, it.firmType ) }
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

data class SheetData(val firmType: String?, val showName: String?) {
    var isChecked = false
}