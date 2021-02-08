package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.AllTransferOutResult
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
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


    val allBalanceResultList: LiveData<List<GameData>> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _allBalanceResultList

    val thirdGamesResult: LiveData<ThirdGamesResult> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _thirdGamesResult

    val recycleAllMoneyResult: LiveData<AllTransferOutResult> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _recycleAllMoneyResult

    val userMoney: LiveData<Double?>
        get() = _userMoney


    private val _userMoney = MutableLiveData<Double?>()
    private var _allBalanceResultList = MutableLiveData<List<GameData>>()
    private var _thirdGamesResult = MutableLiveData<ThirdGamesResult>()
    private var _recycleAllMoneyResult = MutableLiveData<AllTransferOutResult>()


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

    fun recycleAllMoney() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.allTransferOut()
            }?.let { result ->
                _recycleAllMoneyResult.value = result
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