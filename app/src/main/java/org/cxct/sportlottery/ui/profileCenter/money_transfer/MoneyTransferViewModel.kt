package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersRequest
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil

class MoneyTransferViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    enum class PLAT {
        IN_PLAT, OUT_PLAT
    }

    private val allPlat = "ALL_PLAT"
    val platCode = "CG"

    var inCode: String? = null
    var outCode: String? = null

    val statusList = androidContext.resources.getStringArray(R.array.transfer_state_array).map {
        when (it) {
            androidContext.getString(R.string.log_state_processing) -> {
                StatusSheetData(Status.PROCESSING.code.toString(), it)
            }
            androidContext.getString(R.string.recharge_state_success) -> {
                StatusSheetData(Status.SUCCESS.code.toString(), it)

            }
            androidContext.getString(R.string.recharge_state_failed) -> {
                StatusSheetData(Status.FAILED.code.toString(), it)
            }
            else -> {
                StatusSheetData(null, it).apply { isChecked = true }
            }
        }
    }

    val allBalanceResultList: LiveData<List<GameData>>
        get() = _allBalanceResultList

    val subInPlatSheetList: LiveData<List<StatusSheetData>>
        get() = _subInPlatSheetList

    val subOutPlatSheetList: LiveData<List<StatusSheetData>>
        get() = _subOutPlatSheetList

    val recordInPlatSheetList: LiveData<List<StatusSheetData>>
        get() = _recordInPlatSheetList

    val recordOutPlatSheetList: LiveData<List<StatusSheetData>>
        get() = _recordOutPlatSheetList

    val recycleAllMoneyResult: LiveData<Event<NetResult?>>
        get() = _recycleAllMoneyResult

    val transferResult: LiveData<Event<NetResult?>>
        get() = _transferResult

    val queryTransfersResult: LiveData<QueryTransfersResult>
        get() = _queryTransfersResult

    val isShowTitleBar: LiveData<Boolean>
        get() = _isShowTitleBar

    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val toolbarName: LiveData<String>
        get() = _toolbarName

    private val _subInPlatSheetList = MutableLiveData<List<StatusSheetData>>()
    private val _subOutPlatSheetList = MutableLiveData<List<StatusSheetData>>()
    private val _recordInPlatSheetList = MutableLiveData<List<StatusSheetData>>()
    private val _recordOutPlatSheetList = MutableLiveData<List<StatusSheetData>>()
    private val _isShowTitleBar = MutableLiveData<Boolean>().apply { this.value = true }
    private val _loading = MutableLiveData<Boolean>()
    private val _toolbarName = MutableLiveData<String>()
    private var _allBalanceResultList = MutableLiveData<List<GameData>>()
    private var _recycleAllMoneyResult = MutableLiveData<Event<NetResult?>>()
    private var _transferResult = MutableLiveData<Event<NetResult?>>()
    private var _queryTransfersResult = MutableLiveData<QueryTransfersResult>()

    fun showTitleBar(visible: Boolean) {
        _isShowTitleBar.value = visible
    }

    fun setToolbarName(name: String) {
        _toolbarName.value = name
    }

    private var thirdGameMap = mutableMapOf<String?, String?>()

    fun getThirdGames(showLoading: Boolean = true) {
        if(showLoading){
            loading()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                if(showLoading){
                    hideLoading()
                }
                dealwithThirdGameResult(result)
            }
        }
    }
    fun getAllBalance(showLoading: Boolean = true) {
        if(showLoading){
            loading()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getAllBalance()
            }?.let { result ->
                dealwithGetAllBalanceResult(result)
            }
            if(showLoading){
                hideLoading()
            }
        }
    }
    private fun dealwithThirdGameResult(result: ThirdGamesResult){
        val resultList = mutableListOf<GameData>()
        var gameFirmList = result.t?.gameFirmMap?.entries?.toList()

        if (!gameFirmList.isNullOrEmpty()) {

            gameFirmList = gameFirmList.sortedBy { it.value.sort }
            gameFirmList.forEach {
                val value = it.value
                if (!thirdGameMap.containsKey(value.firmType)){
                    thirdGameMap[value.firmType] = value.firmShowName
                    if (value.open == 1) {
                        resultList.add(GameData(null, null, null).apply {
                            code = it.key
                            showName = value.firmName ?: it.key
                        })
                    }
                }
            }
        }
        setRecordInSheetDataList(resultList)
        setRecordOutSheetDataList(resultList)
    }
    private fun dealwithGetAllBalanceResult(result: GetAllBalanceResult){
        val resultList = mutableListOf<GameData>()
        for ((key, value) in result.resultMap ?: mapOf()) {
            value?.apply {
                val gameData = GameData(money, remark, transRemaining).apply {
                    code = key
                    showName = thirdGameMap[key] ?: key
                }
                resultList.add(gameData)
            }
        }
        _allBalanceResultList.postValue(resultList)
        if (result.success) {
            setSubInSheetDataList(resultList)
            setSubOutSheetDataList(resultList)
        }
    }

    fun getThirdGamesWithMoney(showLoading:Boolean = true) {
        if(showLoading){
            loading()
        }
        viewModelScope.launch {
            val task1 = async {
                doNetwork(androidContext) {
                    OneBoSportApi.thirdGameService.getThirdGames()
                }
            }
            val task2 = async {
                doNetwork(androidContext) {
                    OneBoSportApi.thirdGameService.getAllBalance()
                }
            }
            val result1 = task1.await()
            val result2 = task2.await()
            result1?.let {
                dealwithThirdGameResult(it)
            }
            result2?.let {
                dealwithGetAllBalanceResult(it)
            }
            if (showLoading) {
                hideLoading()
            }
        }

    }

    private var defaultSubOutList = listOf<StatusSheetData>()
    private var defaultSubInList = listOf<StatusSheetData>()
    private var defaultRecordOutList = listOf<StatusSheetData>()
    private var defaultRecordInList = listOf<StatusSheetData>()

    private fun setSubInSheetDataList(resultList: List<GameData>) {
        val list = mutableListOf<StatusSheetData>()
        list.add(StatusSheetData(platCode, androidContext.getString(R.string.plat_money)))
        resultList.forEach {
            list.add(StatusSheetData(it.code, it.showName))
        }
        defaultSubInList = list
        _subInPlatSheetList.value = list
    }

    private fun setSubOutSheetDataList(resultList: List<GameData>) {
        val list = mutableListOf<StatusSheetData>()
        list.add(StatusSheetData(platCode, androidContext.getString(R.string.plat_money)))
        resultList.forEach {
            list.add(StatusSheetData(it.code, it.showName))
        }
        defaultSubOutList = list
        _subOutPlatSheetList.value = list
    }

    private fun setRecordInSheetDataList(resultList: List<GameData>) {
        val list = mutableListOf<StatusSheetData>()
        list.add(StatusSheetData(allPlat, androidContext.getString(R.string.all_in_plat)))
        list.add(StatusSheetData(platCode, androidContext.getString(R.string.plat_money)))
        resultList.forEach {
            list.add(StatusSheetData(it.code, it.showName))
        }
        defaultRecordInList = list
        _recordInPlatSheetList.value = list
    }


    private fun setRecordOutSheetDataList(resultList: List<GameData>) {
        val list = mutableListOf<StatusSheetData>()
        list.add(StatusSheetData(allPlat, androidContext.getString(R.string.all_out_plat)))
        list.add(StatusSheetData(platCode, androidContext.getString(R.string.plat_money)))
        resultList.forEach {
            list.add(StatusSheetData(it.code, it.showName))
        }
        defaultRecordOutList = list
        _recordOutPlatSheetList.value = list
    }

    fun filterSubList(plat: PLAT, filterPlat: String?) {
        if (plat == PLAT.IN_PLAT) {
            val list = defaultSubInList.filter { it.showName != filterPlat }
            _subInPlatSheetList.value = list
        } else {
            val list = defaultSubOutList.filter { it.showName != filterPlat }
            _subOutPlatSheetList.value = list
        }
    }

    fun filterRecordList(plat: PLAT, filterPlat: String?) {
        if (plat == PLAT.IN_PLAT) {
            val list = defaultRecordInList.filter { it.showName != filterPlat }
            _recordInPlatSheetList.value = list
        } else {
            val list = defaultRecordOutList.filter { it.showName != filterPlat }
            _recordOutPlatSheetList.value = list
        }
    }

    fun recycleAllMoney() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.allTransferOut()
            }?.let { result ->
                hideLoading()
                _recycleAllMoneyResult.value = Event(result)

                getAllBalance(false)
                getMoneyAndTransferOut()
            }
        }
    }

    fun transfer(isReversed: Boolean, outPlat: String?, inPlat: String?, amount: Long?) {
        if (amount == null || outPlat == null || inPlat == null) return
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                if (isReversed) {
                    OneBoSportApi.thirdGameService.transfer(inPlat, outPlat, amount)
                } else {
                    OneBoSportApi.thirdGameService.transfer(outPlat, inPlat, amount)
                }
            }?.let { result ->
                hideLoading()
                _transferResult.value = Event(result)

                getAllBalance()
                getMoneyAndTransferOut()
            }
        }
    }

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()
    fun queryTransfers(
        page: Int? = 1,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        status: String? = null,
        firmTypeIn: String? = null,
        firmTypeOut: String? = null,
    ) {

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                val firmFilter = { item: String? -> if (item == allPlat || item.isNullOrEmpty()) null else item }

                OneBoSportApi.thirdGameService.queryTransfers(QueryTransfersRequest(page,
                                                                                    PAGE_SIZE,
                                                                                    startTime,
                                                                                    endTime,
                                                                                    firmFilter(firmTypeIn),
                                                                                    firmFilter(firmTypeOut),
                                                                                    status?.toIntOrNull()))
            }?.let { result ->
                hideLoading()
                isLoading = false
                if (page == 1) {
                    nowPage = 1
                    recordDataList.clear()
                }
                result.rows?.let {
                    recordDataList.addAll(it)
                }
                isLastPage = (recordDataList.size >= (result.total ?: 0))
                _queryTransfersResult.value = result
            }
        }
    }

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
                queryTransfers(++nowPage)
            }
        }
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }

    fun initCode() {
        inCode = null
        outCode = null
    }
}