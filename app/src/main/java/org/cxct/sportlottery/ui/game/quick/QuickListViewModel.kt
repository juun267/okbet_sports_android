package org.cxct.sportlottery.ui.game.quick

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.odds.quick.QuickListResult
import org.cxct.sportlottery.ui.base.BaseViewModel2
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount

class QuickListViewModel(val context: Context) : BaseViewModel2() {

    val mQuickOddsListGameHallResult = MutableLiveData<Event<QuickListResult>>()//MutableLiveData<Event<OddsListResult?>>()

    fun apiGetQuickList(matchId: String) {
        context?.let { coxt ->
            viewModelScope.launch {
                val result = doNetwork(coxt) {
                    OneBoSportApi.oddsService.getQuickList(
                        QuickListRequest(matchId)
                    )
                }

                result?.quickListData?.let {
                    val discount = 1.0F
                    it.quickOdds?.forEach { (_key, quickOddsValue) ->
                        quickOddsValue.forEach { (key, value) ->
                            value?.forEach { odd ->
                                if (!key.contains(PlayCate.LCS.value)) {
                                    odd?.odds = odd?.odds?.applyDiscount(discount)
                                    odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)
                                }
                                if (key == QuickPlayCate.QUICK_EPS.value) {
                                    odd?.extInfo = odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                                }

                                mQuickOddsListGameHallResult.value?.peekContent()?.quickListData?.quickOdds?.forEach { (_, map) ->
                                    map.forEach { (_, oddList) ->
                                        oddList?.forEach { oldOdd ->
                                            if(odd?.id == oldOdd?.id) {
                                                when {
                                                    odd?.odds ?: 0.0 > oldOdd?.odds ?: 0.0 -> {
                                                        odd?.oddState = OddState.LARGER.state
                                                    }
                                                    odd?.odds ?: 0.0 < oldOdd?.odds ?: 0.0 -> {
                                                        odd?.oddState = OddState.SMALLER.state
                                                    }
                                                    odd?.odds == oldOdd?.odds ?: 0 -> {
                                                        odd.oddState = OddState.SAME.state
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    mQuickOddsListGameHallResult.postValue(Event(result))
                    resetOddState()
                }
            }
        }
    }

    private fun resetOddState() {
        mQuickOddsListGameHallResult.value?.peekContent()?.quickListData?.quickOdds?.forEach { (_, map) ->
            map.forEach { (_, oddList) ->
                oddList?.forEach { odd ->
                    odd?.oddState = OddState.SAME.state
                }
            }
        }
    }

}