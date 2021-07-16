package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddErrorData
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.BetAddError
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.getOdds


abstract class BaseOddButtonViewModel(
    val androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseSocketViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val oddsType: LiveData<OddsType> = loginRepository.mOddsType

    val betAddResult: LiveData<Event<BetAddResult?>>
        get() = _betAddResult

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    private val _betAddResult = MutableLiveData<Event<BetAddResult?>>()

    fun getMoney() {
        if (isLogin.value == false) return

        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun saveOddsType(oddsType: OddsType) {
        loginRepository.sOddsType = oddsType.code
        loginRepository.mOddsType.postValue(oddsType)
    }

    fun getOddsType() {
        loginRepository.mOddsType.postValue(
            when (loginRepository.sOddsType) {
                OddsType.EU.code -> OddsType.EU
                OddsType.HK.code -> OddsType.HK
                else -> OddsType.EU
            }
        )
    }

    fun updateMatchOddForParlay(matchOdd: MatchOddsChangeEvent) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
            mutableListOf()
        for ((_, value) in matchOdd.odds ?: mapOf()) {
            value.odds?.forEach { odd ->
                odd?.let { o ->
                    newList.add(o)
                }
            }
        }
        updateBetInfoListByMatchOddChange(newList)
    }

    fun updateMatchOddForParlay(
        betAddErrorDataList: List<BetAddErrorData>,
        betAddError: BetAddError
    ) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> = mutableListOf()
        betAddErrorDataList.forEach { betAddErrorData ->
            betAddErrorData.let { data ->
                data.status?.let { status ->
                    val newOdd = org.cxct.sportlottery.network.odds.detail.Odd(
                        null,
                        data.id,
                        null,
                        data.odds,
                        data.hkOdds,
                        data.producerId,
                        data.spread,
                        status,
                    )
                    newList.add(newOdd)
                }
            }
        }

        betInfoRepository.matchOddList.value?.forEach {
            updateItemForBetAddError(it, newList, betAddError)
        }

        updateBetInfoListByMatchOddChange(newList)
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds?.forEach { map ->
                    val value = map.value
                    value.forEach { odd ->
                        odd?.let {
                            val newOdd = org.cxct.sportlottery.network.odds.detail.Odd(
                                null,
                                odd.id,
                                null,
                                odd.odds,
                                odd.hkOdds,
                                odd.producerId,
                                odd.spread,
                                odd.status,
                            )
                            newList.add(newOdd)
                        }
                    }
                }
            }

            is MatchOddsChangeEvent -> {
                for ((_, value) in changeEvent.odds ?: mapOf()) {
                    value.odds?.forEach { odd ->
                        odd?.let { o ->
                            newList.add(o)
                        }
                    }
                }
            }
        }

        betInfoRepository.betInfoList.value?.peekContent()?.forEach {
            updateItem(it.matchOdd, newList)
        }
        betInfoRepository.notifyBetInfoChanged()
    }

    fun updateMatchOdd(betAddErrorDataList: List<BetAddErrorData>, betAddError: BetAddError) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> = mutableListOf()
        betAddErrorDataList.forEach { betAddErrorData ->
            betAddErrorData.let { data ->
                data.status?.let { status ->
                    val newOdd = org.cxct.sportlottery.network.odds.detail.Odd(
                        null,
                        data.id,
                        null,
                        data.odds,
                        data.hkOdds,
                        data.producerId,
                        data.spread,
                        status,
                    )
                    newList.add(newOdd)
                }
            }
        }

        betInfoRepository.betInfoList.value?.peekContent()?.forEach {
            updateItemForBetAddError(it.matchOdd, newList, betAddError)
        }
        betInfoRepository.notifyBetInfoChanged()
    }

    fun addBet(betAddRequest: BetAddRequest, matchType: MatchType?) {
        viewModelScope.launch {
            val result = getBetApi(matchType, betAddRequest)
            _betAddResult.postValue(Event(result))
            Event(result).getContentIfNotHandled()?.success?.let {
                if (it) {
                    afterBet(matchType, result)
                }
            }
        }
    }


    fun addBetSingle(stake: Double, betInfoListData: BetInfoListData) {
        val parlayType =
            if (betInfoListData.matchType == MatchType.OUTRIGHT) MatchType.OUTRIGHT.postValue else betInfoListData.parlayOdds?.parlayType

        val request = BetAddRequest(
            listOf(
                Odd(
                    betInfoListData.matchOdd.oddsId,
                    getOdds(betInfoListData.matchOdd, oddsType.value ?: OddsType.EU),
                    stake
                )
            ),
            listOf(Stake(parlayType ?: "", stake)),
            1,
            oddsType.value?.code ?: OddsType.EU.code
        )

        viewModelScope.launch {
            val result = getBetApi(betInfoListData.matchType, request)
            _betAddResult.postValue(Event(result))
            Event(result).getContentIfNotHandled()?.success?.let {
                if (it) {
                    afterBet(betInfoListData.matchType, result)
                }
            }
        }
    }

    fun saveOddsHasChanged(matchOdd: org.cxct.sportlottery.network.bet.info.MatchOdd) {
        betInfoRepository.saveOddsHasChanged(matchOdd)
    }

    fun removeBetInfoItem(oddId: String?) {
        betInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoItemAndRefresh(oddId: String) {
        removeBetInfoItem(oddId)
        if (betInfoRepository.betInfoList.value?.peekContent()?.size != 0) {
            getBetInfoListForParlay()
        }
    }

    fun removeBetInfoAll() {
        betInfoRepository.clear()
    }

    fun getBetInfoListForParlay() {
        betInfoRepository.addInBetInfoParlay()
    }

    protected fun getOddState(
        oldItemOdds: Double,
        newOdd: org.cxct.sportlottery.network.odds.detail.Odd
    ): Int {
        val odds = when (loginRepository.mOddsType.value) {
            OddsType.EU -> newOdd.odds
            OddsType.HK -> newOdd.hkOdds
            else -> null
        }
        val newOdds = odds ?: 0.0
        return when {
            newOdds == oldItemOdds -> OddState.SAME.state
            newOdds > oldItemOdds -> OddState.LARGER.state
            newOdds < oldItemOdds -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }

    private fun getSpreadState(oldSpread: String, newSpread: String): Int =
        when {
            newSpread != oldSpread -> SpreadState.DIFFERENT.state
            else -> SpreadState.SAME.state
        }


    private fun updateBetInfoListByMatchOddChange(newListFromSocket: List<org.cxct.sportlottery.network.odds.detail.Odd>) {
        betInfoRepository.matchOddList.value?.forEach {
            updateItem(it, newListFromSocket)
        }
        getBetInfoListForParlay()
    }

    private fun updateItem(
        oldItem: org.cxct.sportlottery.network.bet.info.MatchOdd,
        newList: List<org.cxct.sportlottery.network.odds.detail.Odd>
    ) {
        for (newItem in newList) {
            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        oldItem.oddState = getOddState(
                            getOdds(
                                oldItem,
                                loginRepository.mOddsType.value ?: OddsType.EU
                            ), newItem
                        )

                        oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                        newItem.status.let { status -> oldItem.status = status }

                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                            newItem.spread.let { spread -> oldItem.spread = spread ?: "" }
                        }

                        //從socket獲取後 賠率有變動並且投注狀態開啟時 需隱藏錯誤訊息
                        if (oldItem.oddState != org.cxct.sportlottery.network.bet.info.MatchOdd.OddState.SAME.state &&
                            oldItem.status == BetStatus.ACTIVATED.code
                        ) {
                            oldItem.betAddError = null
                        }

                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateItemForBetAddError(
        oldItem: org.cxct.sportlottery.network.bet.info.MatchOdd,
        newList: List<org.cxct.sportlottery.network.odds.detail.Odd>,
        betAddError: BetAddError
    ) {
        for (newItem in newList) {
            //每次都先把字串清空
            oldItem.betAddError = null

            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        if (betAddError == BetAddError.ODDS_HAVE_CHANGED) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem,
                                    loginRepository.mOddsType.value ?: OddsType.EU
                                ), newItem
                            )

                            oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                            newItem.spread.let { spread -> oldItem.spread = spread ?: "" }
                        }

                        newItem.status.let { status -> oldItem.status = status }
                        oldItem.betAddError = betAddError
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getBetApi(
        matchType: MatchType?,
        betAddRequest: BetAddRequest
    ): BetAddResult? {
        //冠軍的投注要使用不同的api
        return if (matchType == MatchType.OUTRIGHT) {
            doNetwork(androidContext) {
                OneBoSportApi.outrightService.addOutrightBet(betAddRequest)
            }
        } else {
            doNetwork(androidContext) {
                OneBoSportApi.betService.addBet(betAddRequest)
            }
        }
    }

    private fun afterBet(matchType: MatchType?, result: BetAddResult?) {
        if (matchType != MatchType.PARLAY) {
            result?.rows?.let { rowList ->
                removeBetInfoItem(rowList[0].matchOdds[0].oddsId)
            }
        } else {
            betInfoRepository.clear()
        }
    }
}