package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.BlankResult
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.network.third_game.money_transfer.GameDataInPlat
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersRequest
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.component.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.ui.profileCenter.sportRecord.SheetData
import org.cxct.sportlottery.util.TimeUtil

class MoneyTransferViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val gameNameMap: Map<String?, String> = mapOf(
        "CG" to androidContext.getString(R.string.plat_money),
        "DF" to androidContext.getString(R.string.third_game_df),
        "SBTY" to androidContext.getString(R.string.third_game_sbty),
        "AG" to androidContext.getString(R.string.third_game_ag),
        "IBO" to androidContext.getString(R.string.third_game_ibo),
        "CQ9" to androidContext.getString(R.string.third_game_cq9),
        "CGCP" to androidContext.getString(R.string.third_game_cgcp),
        "OGPLUS" to androidContext.getString(R.string.third_game_ogplus),
        "CR" to androidContext.getString(R.string.third_game_cr),
    )

    private val resultList = mutableListOf<GameData>()
    var outPlatDataList = mutableListOf<GameData>()
    var inPlatDataList = mutableListOf<GameDataInPlat>()

    val statusList = androidContext.resources.getStringArray(R.array.recharge_state_array).map {
        when (it) {
            androidContext.getString(R.string.recharge_state_processing) -> {
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

    private val inPlatRecordList by lazy {
        mutableListOf<StatusSheetData>().apply {
            this.add(StatusSheetData(null, androidContext.getString(R.string.all_in_plat)))
            inPlatDataList.forEach {
                this.add(StatusSheetData(it.code, it.showName))
            }
        }
    }

    private val outPlatRecordList by lazy {
        mutableListOf<StatusSheetData>().apply {
            this.add(StatusSheetData(null, androidContext.getString(R.string.all_out_plat)))
            Log.e(">>>", "list size = ${outPlatDataList.size}")
            outPlatDataList.forEach {
                this.add(StatusSheetData(it.code, it.showName))
            }
        }
    }

    val isPlatSwitched: LiveData<Boolean?>
        get() = _isPlatSwitched

    val allBalanceResultList: LiveData<List<GameData>>
        get() = _allBalanceResultList

    val thirdGamesResult: LiveData<ThirdGamesResult>
        get() = _thirdGamesResult

    val recycleAllMoneyResult: LiveData<BlankResult?>
        get() = _recycleAllMoneyResult

    val transferResult: LiveData<BlankResult?>
        get() = _transferResult

    val queryTransfersResult: LiveData<QueryTransfersResult>
        get() = _queryTransfersResult

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val isShowTitleBar: LiveData<Boolean>
        get() = _isShowTitleBar

    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val toolbarName: LiveData<String>
        get() = _toolbarName


    private val _isShowTitleBar = MutableLiveData<Boolean>().apply { this.value = true }
    private val _isPlatSwitched = MutableLiveData<Boolean?>()
    private val _loading = MutableLiveData<Boolean>()
    private val _toolbarName = MutableLiveData<String>()
    private val _userMoney = MutableLiveData<Double?>()
    private var _allBalanceResultList = MutableLiveData<List<GameData>>()
    private var _thirdGamesResult = MutableLiveData<ThirdGamesResult>()
    private var _recycleAllMoneyResult = MutableLiveData<BlankResult?>()
    private var _transferResult = MutableLiveData<BlankResult?>()
    private var _queryTransfersResult = MutableLiveData<QueryTransfersResult>()

    fun showTitleBar(visible: Boolean) {
        _isShowTitleBar.value = visible
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun getMoney() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }?.let {
                hideLoading()
                _userMoney.postValue(it.money)
            }
        }
    }

    fun setToolbarName(name: String) {
        _toolbarName.value = name
    }

    fun setIsPlatSwitched(isSwitched: Boolean?) {
        isSwitched.let {
            _isPlatSwitched.value = it
        }
    }

    fun getAllBalance() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getAllBalance()
            }.let { result ->
                hideLoading()
                result?.apply {
                    resultList.clear()
                    for ((key, value) in result.resultMap ?: mapOf()) {
                        value?.apply {
                            val gameData = GameData(money, remark, transRemaining).apply {
                                code = key
                                showName = gameNameMap[key] ?: key
                            }

                            resultList.add(gameData)
                        }
                    }
                    setPlatDataList()
                    _allBalanceResultList.postValue(resultList)
                }
            }
        }
    }

    fun getInPlatNameList(selectedOutPlatName: String?= null): List<StatusSheetData> {
        return inPlatRecordList.filter { it.showName != selectedOutPlatName }
    }

    fun getOutPlatNameList(selectedInPlatName: String?= null): List<StatusSheetData> {
        return outPlatRecordList.filter { it.showName != selectedInPlatName }
    }

    var defaultOutPlat = "CG"
    var defaultInPlat : String? = null

    fun setPlatDataList() {
        outPlatDataList.clear()
        inPlatDataList.clear()

        resultList.forEach { gameData ->
            val inPlatData = GameDataInPlat(gameData.money, gameData.remark, gameData.transRemaining).apply {
                isChecked = false
                showName = gameData.showName
                code = gameData.code
            }
            inPlatDataList.add(inPlatData)
        }

        resultList.forEach { gameData ->
            val inPlatData = gameData.apply { gameData.isChecked = false }
            outPlatDataList.add(inPlatData)
        }

        Log.e(">>>", "outPlatDataList size = ${outPlatDataList.size}")
        inPlatDataList.add(0, GameDataInPlat().apply {
            isChecked = false
            code = "CG"
            showName = androidContext.getString(R.string.plat_money)
        })

        outPlatDataList.add(0, GameData().apply {
            isChecked = false
            code = "CG"
            showName = androidContext.getString(R.string.plat_money)
        })

    }

    fun recycleAllMoney() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.allTransferOut()
            }?.let { result ->
                hideLoading()
                _recycleAllMoneyResult.value = result

                getAllBalance()
                getMoney()
                clearRecycleAllMoneyResult()
            }
        }
    }

    fun transfer(outPlat: String, inPlat: String, amount: Long?) {
        if (amount == null) return
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.transfer(outPlat, inPlat, amount)
            }?.let { result ->
                hideLoading()
                _transferResult.value = result

                getAllBalance()
                getMoney()
                clearTransferResult()
            }
        }
    }

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun queryTransfers(page: Int? = 1,
                       startTime: String ?= TimeUtil.getDefaultTimeStamp().startTime,
                       endTime: String ?= TimeUtil.getDefaultTimeStamp().endTime,
                       status: String ?= null,
                       firmTypeIn: String ?= null,
                       firmTypeOut: String ?= null) {

        loading()
        if (page == 1) {
            nowPage = 1
            recordDataList.clear()
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.queryTransfers(QueryTransfersRequest(page, PAGE_SIZE, startTime, endTime, firmTypeIn, firmTypeOut, status?.toIntOrNull()))
            }?.let { result ->
                hideLoading()
                isLoading = false
                recordDataList.addAll(result.rows as List<Row>)
                isLastPage = (recordDataList.size >= (result.total ?:0))
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


    fun getThirdGames() {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                hideLoading()
                _thirdGamesResult.value = result
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun clearTransferResult() {
        _transferResult.postValue(null)
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun clearRecycleAllMoneyResult() {
        _recycleAllMoneyResult.postValue(null)
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }

}