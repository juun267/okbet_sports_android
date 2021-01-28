package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import retrofit2.Response

class BetInfoRepository {

    var betList: MutableList<BetInfoListData> = mutableListOf()

    suspend fun getBetInfo(oddsList: List<Odd>): Response<BetInfoResult> {
        val result = getBetUrl(oddsList)
        result.body()?.success.let {
            result.body()?.betInfoData.let { data ->
                if (data != null) {
                    for (i in data.matchOdds.indices) {
                        betList.add(BetInfoListData(data.matchOdds[i], data.parlayOdds[i]).apply { matchType = oddsList[0].matchType })
                    }
                }
            }
        }
        return result
    }

    private suspend fun getBetUrl(oddsList: List<Odd>): Response<BetInfoResult> {
        val request = BetInfoRequest("EU", oddsList)
        return if (oddsList[0].matchType == MatchType.OUTRIGHT) {
            OneBoSportApi.outrightService.getOutrightBetInfo(request)
        } else {
            OneBoSportApi.betService.getBetInfo(request)
        }
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