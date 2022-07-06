package org.cxct.sportlottery.ui.game.quick

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.odds.quick.QuickListResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount

class TestViewModel(
    private var context: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseViewModel(
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
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