package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuota
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.MatchOddUtil
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil

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


    fun addInBetInfoParlay(sendList: MutableList<MatchOdd>) {
        val sportType = when (betList[0].matchOdd.gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }

        sportType?.let {
            matchOddList.clear()
            parlayOddList.clear()

            //切換串關後只保留串關項目
            matchOddList.addAll(sendList)
            parlayOddList.addAll(getParlayOdd(MatchType.PARLAY, it, sendList))
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
        playCateName: String,
        playName: String,
        matchOdd: org.cxct.sportlottery.network.odds.list.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType, sportType.code, playCateName, playName, matchOdd, odd
        )

        betInfoMatchOdd?.let {

            if (matchType == MatchType.PARLAY) {
                betList.remove(betList.find { data -> it.matchId == data.matchOdd.matchId })
            }

            betList.add(
                BetInfoListData(
                    betInfoMatchOdd,
                    getParlayOdd(matchType, sportType, mutableListOf(it)).first()
                ).apply {
                    this.matchType = matchType
                }
            )

            _betInfoList.postValue(betList)
        }
    }

    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String?,
        playName: String?,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            sportType.code, playCateName, playName, matchOdd, odd
        )

        betInfoMatchOdd?.let {

            if (matchType == MatchType.PARLAY) {
                betList.remove(betList.find { data -> it.matchId == data.matchOdd.matchId })
            }

            betList.add(
                BetInfoListData(
                    betInfoMatchOdd,
                    getParlayOdd(matchType, sportType, mutableListOf(it)).first()
                ).apply {
                    this.matchType = matchType
                }
            )

            _betInfoList.postValue(betList)
        }
    }

    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.odds.detail.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.detail.Odd
    ) {
        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType, sportType.code, playCateName, matchOdd, odd
        )

        betInfoMatchOdd?.let {

            if (matchType == MatchType.PARLAY) {
                betList.remove(betList.find { data -> it.matchId == data.matchOdd.matchId })
            }

            betList.add(
                BetInfoListData(
                    betInfoMatchOdd,
                    getParlayOdd(matchType, sportType, mutableListOf(it)).first()
                ).apply {
                    this.matchType = matchType
                }
            )

            _betInfoList.postValue(betList)
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


    fun saveOddsHasChanged(matchOdd: MatchOdd) {
        val hasChanged = betList.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.oddsHasChanged = true
    }
}