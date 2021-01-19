package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import retrofit2.Response

class BetInfoRepository {

    var betList: MutableList<BetInfoListData> = mutableListOf()

    suspend fun getBetInfoList(oddsList: List<Odd>): Response<BetInfoResult> {
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

    fun removeItem(oddId: String) {
        betList.remove(betList.find {
            it.matchOdd.oddsId == oddId
        })
    }

}