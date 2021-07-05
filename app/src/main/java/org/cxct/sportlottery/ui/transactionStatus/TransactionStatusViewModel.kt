package org.cxct.sportlottery.ui.transactionStatus

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.game.BetRecordType
import org.cxct.sportlottery.util.TimeUtil

class TransactionStatusViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseNoticeViewModel(androidContext, userInfoRepository, loginRepository, betInfoRepository, infoCenterRepository) {

    val loading: LiveData<Boolean>
        get() = _loading
    private val _loading = MutableLiveData<Boolean>()

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult
    private val _messageListResult = MutableLiveData<MessageListResult?>()

    //整理後未結算投注紀錄資料格式
    val betListData: LiveData<BetListData>
        get() = _betListData
    private val _betListData = MutableLiveData<BetListData>()


    //獲取系統公告
    fun getAnnouncement() {
        if (isLogin.value == true) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    val typeList = arrayOf(1)
                    OneBoSportApi.messageService.getPromoteNotice(typeList)
                }?.let { result -> _messageListResult.postValue(result) }
            }
        } else {
            _messageListResult.value = null
        }
    }

    private val pageSize = 20

    //獲取交易狀況資料(未結算)
    fun getBetList(firstPage: Boolean) {

        val page = if (firstPage) 1 else betListData.value?.page?.plus(1) ?: 1
        val betListRequest = createUnSettlementBetListRequest(page)

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }?.let { result ->
                hideLoading()
                val rowList =
                    if (page == 1) mutableListOf<Row>()
                    else betListData.value?.row?.toMutableList() ?: mutableListOf<Row>()
                result.rows?.let { rowList.addAll(it) }
                _betListData.value =
                    BetListData(rowList, result.other?.totalAmount ?: 0, page, (rowList.size >= (result.total ?: 0)))
            }
        }
    }

    private fun createUnSettlementBetListRequest(page: Int): BetListRequest {
        return BetListRequest(
            championOnly = 0,
            BetRecordType.UNSETTLEMENT.code,
            page = page,
            startTime = TimeUtil.getDefaultTimeStamp().startTime,
            endTime = TimeUtil.getDefaultTimeStamp().endTime,
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