package org.cxct.sportlottery.ui.betRecord

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel


class TransactionStatusViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
) {

    //投注單球類
    var gameType: String? = null
        set(value) {
            field = value
            getBetList(true)
        }

    var statusList: List<Int>? = listOf(0, 1)
        set(value) {
            field = value
            getBetList(true)
        }

    val loading: LiveData<Boolean>
        get() = _loading
    private val _loading = MutableLiveData<Boolean>()

    //整理後未結算投注紀錄資料格式
    val betListData: LiveData<BetListData>
        get() = _betListData
    private val _betListData = MutableLiveData<BetListData>()


//    val sportCodeList: LiveData<List<StatusSheetData>>
//        get() = _sportCodeSpinnerList
//    private val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單


    private val pageSize = 20
    private var betListRequesting = false
    private var requestCount = 0
    private val requestMaxCount = 2

    //獲取交易狀況資料(未結算)
    fun getBetList(firstPage: Boolean = false) {
        if (betListRequesting || (betListData.value?.isLastPage == true && !firstPage)) return
        betListRequesting = true

        val page = if (firstPage) 1 else betListData.value?.page?.plus(1) ?: 1
        val betListRequest = createUnSettlementBetListRequest(page)

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }?.let { result ->
                betListRequesting = false
                if (result.success) {
                    requestCount = 0
                    val rowList = if (page == 1) mutableListOf()
                    else betListData.value?.row?.toMutableList() ?: mutableListOf()
                    result.rows?.let { rowList.addAll(it.apply { }) }

                    _betListData.value = BetListData(
                        rowList,
                        MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK,
                        result.other?.totalAmount ?: 0.0,
                        page,
                        (rowList.size >= (result.total ?: 0))
                    )
                } else {
                    if (result.code == NetWorkResponseType.REQUEST_TOO_FAST.code && requestCount < requestMaxCount) {
                        requestCount += 1
                    }
                }

                hideLoading()
            }
        }
    }



    private fun createUnSettlementBetListRequest(page: Int): BetListRequest {
        return BetListRequest(
            championOnly = 0,
            statusList = statusList,
            page = page,
            gameType = gameType,
            pageSize = pageSize
        )
    }


    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }


}