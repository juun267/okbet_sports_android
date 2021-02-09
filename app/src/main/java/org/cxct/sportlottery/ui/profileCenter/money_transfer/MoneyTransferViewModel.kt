package org.cxct.sportlottery.ui.profileCenter.money_transfer

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
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class MoneyTransferViewModel(
        private val androidContext: Context,
        private val userInfoRepository: UserInfoRepository,
        private val loginRepository: LoginRepository,
        betInfoRepository: BetInfoRepository,
) : BaseOddButtonViewModel(betInfoRepository) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    val gameNameMap: Map<String?, String> = mapOf(
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


    val allBalanceResultList: LiveData<List<GameData>>
        get() = _allBalanceResultList

    val thirdGamesResult: LiveData<ThirdGamesResult>
        get() = _thirdGamesResult

    val recycleAllMoneyResult: LiveData<BlankResult>
        get() = _recycleAllMoneyResult

    val transferResult: LiveData<BlankResult>
        get() = _transferResult

    val queryTransfersResult: LiveData<QueryTransfersResult>
        get() = _queryTransfersResult

    val userMoney: LiveData<Double?>
        get() = _userMoney


    private val _userMoney = MutableLiveData<Double?>()
    private var _allBalanceResultList = MutableLiveData<List<GameData>>()
    private var _thirdGamesResult = MutableLiveData<ThirdGamesResult>()
    private var _recycleAllMoneyResult = MutableLiveData<BlankResult>()
    private var _transferResult = MutableLiveData<BlankResult>()
    private var _queryTransfersResult = MutableLiveData<QueryTransfersResult>()


    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }

            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun getAllBalance() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getAllBalance()
            }?.let { result ->

                val resultList = mutableListOf<GameData>()
                for ((key, value) in result.resultMap ?: mapOf()) {
                    value?.apply {
                        val gameData = GameData(money, remark, transRemaining).apply {
                            code = key
                            showName = gameNameMap[key] ?: key
                        }

                        resultList.add(gameData)
                    }
                }

                _allBalanceResultList.postValue(resultList)
            }
        }
    }

    fun addPlatMoneyItem() {
        val platItem = GameData().apply {
            code = "CG"
            showName = androidContext.getString(R.string.plat_money)
        }
    }

    fun recycleAllMoney() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.allTransferOut()
            }?.let { result ->
                _recycleAllMoneyResult.value = result
            }
        }
    }

    fun transfer(outPlat: String, inPlat: String, amount: Long?) {
        if (amount == null) return
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.transfer(outPlat, inPlat, amount)
            }?.let { result ->
                _transferResult.value = result
            }
        }
    }

    fun queryTransfers(page: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.queryTransfers(page, PAGE_SIZE)
            }?.let { result ->
                _queryTransfersResult.value = result
            }
        }
    }

    fun getThirdGames() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                _thirdGamesResult.value = result
            }
        }
    }


}