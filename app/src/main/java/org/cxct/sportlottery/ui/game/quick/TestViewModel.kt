package org.cxct.sportlottery.ui.game.quick

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.feedback.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.odds.quick.QuickListResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.TimeUtil

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
                    //Event(_quickOddsListGameHallResult.value?.peekContent()?.updateQuickPlayCate(matchId, it, it.playCateNameMap))
                }
            }
        }

    }

    private fun OddsListResult.updateQuickPlayCate(
        matchId: String,
        quickListData: QuickListData,
        quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ): OddsListResult {
        this.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    val quickOddsApi = when (quickPlayCate.code) {
                        QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                        else -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                    }?.apply {
                        quickPlayCate.code?.let {
                            setupQuickPlayCate(quickPlayCate.code)
                            sortQuickPlayCate(quickPlayCate.code)
                        }
                    }

                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds.putAll(
                        quickOddsApi?.toMutableFormat() ?: mutableMapOf()
                    )
                }
                matchOdd.quickPlayCateNameMap = quickPlayCateNameMap
            }
        }
        return this
    }

    /**
     * 設置大廳所需顯示的快捷玩法 (api未回傳的玩法需以“—”表示)
     * 2021.10.25 發現可能會回傳但是是傳null, 故新增邏輯, 該玩法odd為null時也做處理
     */
    private fun MutableMap<String, List<Odd?>?>.setupQuickPlayCate(playCate: String) {
        val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")

        playCateSort?.forEach {
            if (!this.keys.contains(it) || this[it] == null)
                this[it] = mutableListOf(null, null, null)
        }
    }

    /**
     * 根據QuickPlayCate的rowSort將盤口重新排序
     */
    private fun MutableMap<String, List<Odd?>?>.sortQuickPlayCate(playCate: String) {
        val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")
        val sortedList = this.toSortedMap(compareBy<String> {
            val oddsIndex = playCateSort?.indexOf(it)
            oddsIndex
        }.thenBy { it })

        this.clear()
        this.putAll(sortedList)
    }

    private fun Map<String, List<Odd?>?>.toMutableFormat(): MutableMap<String, MutableList<Odd?>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }
}