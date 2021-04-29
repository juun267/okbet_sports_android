package org.cxct.sportlottery.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuota
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.INPLAY
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil
import retrofit2.Response

const val BET_INFO_MAX_COUNT = 10

class BetInfoRepository {


    //每個畫面都要觀察
    val _betInfoList = MutableLiveData<MutableList<BetInfoListData>>()
    val betInfoList: LiveData<MutableList<BetInfoListData>>
        get() = _betInfoList


    val _isParlayPage = MutableLiveData<Boolean>()
    val isParlayPage: LiveData<Boolean>
        get() = _isParlayPage

    private val _removeItem = MutableLiveData<String>()
    val removeItem: LiveData<String>
        get() = _removeItem

    var betList: MutableList<BetInfoListData> = mutableListOf()
    var matchOddList: MutableList<MatchOdd> = mutableListOf()
    var parlayOddList: MutableList<ParlayOdd> = mutableListOf()

    var playQuotaComData: PlayQuotaComData? = null


    suspend fun getBetInfo(oddsList: List<Odd>): Response<BetInfoResult> {
        val result = getBetUrl(oddsList)
        result.body()?.success.let {
            result.body()?.betInfoData.let { data ->
                if (data != null) {
                    if (_isParlayPage.value == true) {
                        data.matchOdds.forEach {
                            removeSameMatchIdItem(it.matchId)
                        }
                    }
                    for (i in data.matchOdds.indices) {
                        betList.add(BetInfoListData(data.matchOdds[i], data.parlayOdds[i]).apply { matchType = oddsList[0].matchType })
                    }


                }
            }
        }
        return result
    }


    private suspend fun getBetUrl(oddsList: List<Odd>): Response<BetInfoResult> {
        val request = BetInfoRequest(oddsList)
        return if (oddsList[0].matchType == MatchType.OUTRIGHT) {
            OneBoSportApi.outrightService.getOutrightBetInfo(request)
        } else {
            OneBoSportApi.betService.getBetInfo(request)
        }
    }


    suspend fun getBetInfoList(oddsList: List<Odd>): Response<BetInfoResult> {
        val result = OneBoSportApi.betService.getBetInfo(BetInfoRequest(oddsList))
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

    fun addInBetInfoParlay(){
        matchOddList.clear()
        parlayOddList.clear()

        betList.forEach {
            matchOddList.add(it.matchOdd)
        }

        betList.forEach {
            it.parlayOdds?.let { parlay ->
                parlayOddList.add(parlay) }
        }

        val sportType = when (matchOddList[0].gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }
        sportType?.let {
            parlayOddList.addAll(getParlayOdd(MatchType.PARLAY, it, matchOddList))
        }
    }

    fun getCurrentBetInfoList() {
        _betInfoList.postValue(betList)
    }


    fun removeItem(oddId: String?) {
        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)
        _removeItem.postValue(item?.matchOdd?.matchId)
        _betInfoList.postValue(betList)
    }


    fun clear() {
        betList.clear()
        matchOddList.clear()
        parlayOddList.clear()
        _betInfoList.postValue(betList)
    }

    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        gameType: String,
        playCateName: String,
        playName: String,
        matchOdd: org.cxct.sportlottery.network.odds.list.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        matchOdd.matchInfo?.id?.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
                        odd.producerId?.let { producerId ->

                            val betInfoMatchOdd = MatchOdd(
                                awayName = matchOdd.matchInfo.awayName,
                                homeName = matchOdd.matchInfo.homeName,
                                inplay = if (matchType == MatchType.IN_PLAY) INPLAY else 0,
                                leagueId = "",
                                leagueName = "",
                                matchId = matchId,
                                odds = odds,
                                hkOdds = hkOdds,
                                oddsId = oddsId,
                                playCateId = 0,
                                playCateName = playCateName,
                                playCode = "",
                                playId = 0,
                                playName = playName,
                                producerId = producerId,
                                spread = odd.spread ?: "",
                                startTime = matchOdd.matchInfo.startTime.toLong(),
                                status = odd.status,
                                gameType = gameType,
                                homeScore = matchOdd.matchInfo.homeScore ?: 0,
                                awayScore = matchOdd.matchInfo.awayScore ?: 0
                            )

                            val mList = mutableListOf<MatchOdd>()
                            mList.add(betInfoMatchOdd)
                            betList.add(BetInfoListData(betInfoMatchOdd, getParlayOdd(matchType, sportType, mList)[0]).apply {
                                this.matchType = matchType
                            })
                            _betInfoList.postValue(betList)
                        }
                    }
                }
            }
        }
    }

    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        gameType: String,
        playCateName: String?,
        playName: String?,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        odd.id?.let { oddsId ->
            odd.odds?.let { odds ->
                odd.hkOdds?.let { hkOdds ->
                    odd.producerId?.let { producerId ->

                        val betInfoMatchOdd = MatchOdd(
                            awayName = matchOdd.matchInfo.awayName ?: "",
                            homeName = matchOdd.matchInfo.homeName ?: "",
                            inplay = 0,
                            leagueId = "",
                            leagueName = "",
                            matchId = matchOdd.matchInfo.id,
                            odds = odds,
                            hkOdds = hkOdds,
                            oddsId = oddsId,
                            playCateId = 0,
                            playCateName = playCateName ?: "",
                            playCode = "",
                            playId = 0,
                            playName = playName ?: "",
                            producerId = producerId,
                            spread = odd.spread ?: "",
                            startTime = matchOdd.matchInfo.startTime,
                            status = odd.status,
                            gameType = gameType,
                            homeScore = 0,
                            awayScore = 0
                        )

                        val mList = mutableListOf<MatchOdd>()
                        mList.add(betInfoMatchOdd)
                        betList.add(BetInfoListData(betInfoMatchOdd, getParlayOdd(matchType, sportType, mList)[0]).apply {
                            this.matchType = matchType
                        })
                        _betInfoList.postValue(betList)
                    }
                }
            }
        }
    }

    fun addInBetInfo(
        matchType: MatchType,
        gameType: String,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.odds.detail.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.detail.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        matchOdd.matchInfo.id.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
                        odd.producerId?.let { producerId ->
                            val betInfoMatchOdd = MatchOdd(
                                awayName = matchOdd.matchInfo.awayName,
                                homeName = matchOdd.matchInfo.homeName,
                                inplay = if (matchType == MatchType.IN_PLAY) INPLAY else 0,
                                leagueId = "",
                                leagueName = "",
                                matchId = matchId,
                                odds = odds,
                                hkOdds = hkOdds,
                                oddsId = oddsId,
                                playCateId = 0,
                                playCateName = playCateName,
                                playCode = "",
                                playId = 0,
                                playName = odd.name ?: "",
                                producerId = producerId,
                                spread = odd.spread ?: "",
                                startTime = matchOdd.matchInfo.startTime.toLong(),
                                status = odd.status,
                                gameType = gameType,
                                homeScore = matchOdd.matchInfo.homeScore ?: 0,
                                awayScore = matchOdd.matchInfo.awayScore ?: 0
                            )

                            betList.add(BetInfoListData(betInfoMatchOdd, null))
                            _betInfoList.postValue(betList)

                        }
                    }
                }
            }
        }
    }


    private fun getParlayOdd(
        matchType: MatchType,
        sportType: SportType,
        matchOddList: MutableList<MatchOdd>
    ): List<ParlayOdd> {

        val playQuota: PlayQuota? = when (matchType) {
            MatchType.OUTRIGHT -> {
                when (sportType) {
                    SportType.FOOTBALL -> playQuotaComData?.oUTRIGHTFT
                    SportType.BASKETBALL -> playQuotaComData?.oUTRIGHTBK
                    SportType.TENNIS -> playQuotaComData?.oUTRIGHTTN
                    SportType.VOLLEYBALL -> playQuotaComData?.oUTRIGHTVB
                    SportType.BADMINTON -> playQuotaComData?.oUTRIGHTBM
                }
            }

            MatchType.PARLAY -> {
                when (sportType) {
                    SportType.FOOTBALL -> playQuotaComData?.pARLAYFT
                    SportType.BASKETBALL -> playQuotaComData?.pARLAYBK
                    SportType.TENNIS -> playQuotaComData?.pARLAYTN
                    SportType.VOLLEYBALL -> playQuotaComData?.pARLAYVB
                    SportType.BADMINTON -> playQuotaComData?.pARLAYBM
                }
            }
            else -> {
                when (sportType) {
                    SportType.FOOTBALL -> playQuotaComData?.sINGLEFT
                    SportType.BASKETBALL -> playQuotaComData?.sINGLEBK
                    SportType.TENNIS -> playQuotaComData?.sINGLETN
                    SportType.VOLLEYBALL -> playQuotaComData?.sINGLEVB
                    SportType.BADMINTON -> playQuotaComData?.sINGLEBM
                }
            }
        }

        val oddsList = matchOddList.map {
            it.odds.toBigDecimal()
        }

        val oddsIndexList = oddsList.map {
            oddsList.indexOf(it)
        }

        val parlayComList = ParlayLimitUtil.getCom(oddsIndexList.toIntArray())

        val parlayBetLimitMap = ParlayLimitUtil.getParlayLimit(
            oddsList,
            parlayComList,
            playQuota?.max?.toBigDecimal(),
            playQuota?.min?.toBigDecimal()
        )

        return parlayBetLimitMap.map {
            ParlayOdd(
                parlayType = it.key,
                max = it.value.max.toInt(),
                min = it.value.min.toInt(),
                num = it.value.num,
                odds = it.value.odds.toDouble(),
                hkOdds = it.value.hdOdds.toDouble(),
            )
        }
    }


    private fun removeSameMatchIdItem(matchId: String) {
        val item = betList.find { betInfo ->
            betInfo.matchOdd.matchId == matchId
        }
        removeItem(item?.matchOdd?.oddsId)
    }


    fun saveOddsHasChanged(matchOdd: MatchOdd) {
        val hasChanged = betList.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.oddsHasChanged = true
    }


}