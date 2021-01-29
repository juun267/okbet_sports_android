package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import retrofit2.Response

class BetInfoRepository {

    //每個畫面都要觀察
    val _betInfoList = MutableLiveData<MutableList<BetInfoListData>>()
    val betInfoList: LiveData<MutableList<BetInfoListData>>
        get() = _betInfoList

    val _isParlayPage = MutableLiveData<Boolean>()
    val isParlayPage: LiveData<Boolean>
        get() = _isParlayPage


    var betList: MutableList<BetInfoListData> = mutableListOf()

    var matchOddList: MutableList<MatchOdd> = mutableListOf()

    var parlayOddList: MutableList<ParlayOdd> = mutableListOf()

    suspend fun getBetInfo(oddsList: List<Odd>): Response<BetInfoResult> {
        val result = OneBoSportApi.betService.getBetInfo(BetInfoRequest("EU", oddsList))
        result.body()?.success.let {
            result.body()?.betInfoData.let { data ->
                if (data != null) {
                    for (i in data.matchOdds.indices) {
                        betList.add(BetInfoListData(data.matchOdds[i], data.parlayOdds[i]))
                    }
                }
            }
        }
        return result
    }

    suspend fun getBetInfoList(oddsList: List<Odd>): Response<BetInfoResult> {

        val result = OneBoSportApi.betService.getBetInfo(BetInfoRequest("EU", oddsList))
        result.body()?.success.let {
            result.body()?.betInfoData.let { data ->


                data?.matchOdds?.isNotEmpty()?.let { boolean ->
                    if (boolean) {
                        matchOddList.clear()
                        parlayOddList.clear()

                        data.matchOdds.let { list ->
                            matchOddList.addAll(list)
                        }
                        data.parlayOdds.let { list ->
                            parlayOddList.addAll(list)
                        }
                    }
                }
            }
        }
        return result
    }

    fun removeItem(oddId: String) {
        betList.remove(betList.find {
            it.matchOdd.oddsId == oddId
        })
    }

}