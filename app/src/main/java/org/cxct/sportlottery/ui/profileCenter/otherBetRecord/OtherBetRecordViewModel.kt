package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersRequest
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class OtherBetRecordViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    val typeAllPlat = "ALL_PLAT"

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val thirdGamesResult: LiveData<List<SheetData>?>
        get() = _thirdGamesResult

    private var _thirdGamesResult = MutableLiveData<List<SheetData>?>()
    private val _loading = MutableLiveData<Boolean>()

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    private val recordDataList = mutableListOf<Row>()

    fun getThirdGames() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                hideLoading()

                val resultList = mutableListOf<SheetData>()
                resultList.add(SheetData(typeAllPlat, androidContext.getString(R.string.all_plat_type)))
                for ((key, value) in result.t?.gameFirmMap?: mapOf()) {
                        resultList.add(SheetData(value.firmType, value.firmShowName))
                }

                _thirdGamesResult.value = resultList.distinct()
            }
        }
    }

    fun queryFirstOrders(page: Int? = 1) {
        loading()
        if (page == 1) {
            nowPage = 1
            recordDataList.clear()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.queryTransfers(QueryTransfersRequest(page, PAGE_SIZE))
            }?.let { result ->
                hideLoading()
                isLoading = false
//                _queryTransfersResult.value = result
                recordDataList.addAll(result.rows as List<Row>)
            }
        }
    }

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
                queryFirstOrders(++nowPage)
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