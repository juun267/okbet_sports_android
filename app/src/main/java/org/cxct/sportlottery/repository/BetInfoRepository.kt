package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import retrofit2.Response

class BetInfoRepository {

    var betList: MutableList<BetInfoListData> = mutableListOf()

    suspend fun getBetInfo(oddsList: List<Odd>, isOutright: Boolean): Response<BetInfoResult> {
        val result = if (!isOutright) {
            OneBoSportApi.betService.getBetInfo(BetInfoRequest("EU", oddsList))
        } else {
            OneBoSportApi.outrightService.getOutrightBetInfo(BetInfoRequest("EU", oddsList))
        }
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
        return OneBoSportApi.betService.getBetInfo(BetInfoRequest("EU", oddsList))

    }

    fun removeItem(oddId: String) {
        betList.remove(betList.find {
            it.matchOdd.oddsId == oddId
        })
    }

}