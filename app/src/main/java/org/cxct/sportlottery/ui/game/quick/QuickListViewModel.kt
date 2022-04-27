package org.cxct.sportlottery.ui.game.quick

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.odds.quick.QuickListResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.ui.base.BaseViewModel2
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount

class QuickListViewModel(val context: Context): BaseViewModel2() {

    val _quickOddsListGameHallResult = MutableLiveData<Event<QuickListResult>>()//MutableLiveData<Event<OddsListResult?>>()

    fun apiGetQuickList(matchId: String) {
        context?.let { coxt ->
            viewModelScope.launch {
                val result = doNetwork(coxt) {
                    OneBoSportApi.oddsService.getQuickList(
                        QuickListRequest(matchId)
                    )
                }

                result?.quickListData?.let {
                    // val discount = userInfo.value?.discount ?: 1.0F
                    val discount = 1.0F
                    it.quickOdds?.forEach { (_key, quickOddsValue) ->
                        quickOddsValue.forEach { (key, value) ->
                            value?.forEach { odd ->
                                odd?.odds = odd?.odds?.applyDiscount(discount)
                                odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)

                                if (key == QuickPlayCate.QUICK_EPS.value) {
                                    odd?.extInfo = odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                                }
                            }
                        }
                        Log.d("apiGetQuickList", "$_key => $quickOddsValue")
                    }

                    _quickOddsListGameHallResult.postValue(Event(result))
                }
            }
        }

    }

    private fun Map<String, List<Odd?>?>.toMutableFormat(): MutableMap<String, MutableList<Odd?>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

}