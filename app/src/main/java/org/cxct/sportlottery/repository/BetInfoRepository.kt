package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _betInfoList = MutableLiveData<MutableList<BetInfoListData>>().apply {
        value = mutableListOf()
    }
    val betInfoList: LiveData<MutableList<BetInfoListData>>
        get() = _betInfoList

    private val _matchOddList = MutableLiveData<MutableList<MatchOdd>>()
    val matchOddList: LiveData<MutableList<MatchOdd>>
        get() = _matchOddList

    private val _parlayList = MutableLiveData<MutableList<ParlayOdd>>()
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = _parlayList

    val _isParlayPage = MutableLiveData<Boolean>()
    val isParlayPage: LiveData<Boolean>
        get() = _isParlayPage

    private val _removeItem = MutableLiveData<String>()
    val removeItem: LiveData<String>
        get() = _removeItem

    var playQuotaComData: PlayQuotaComData? = null
        set(value) {
            field = value
            field?.let {
                notifyBetInfoChanged()
            }
        }


    fun addInBetInfoParlay() {
        val betList = _betInfoList.value ?: mutableListOf()

        if (betList.size >= org.cxct.sportlottery.ui.game.BET_INFO_MAX_COUNT || betList.size == 0) {
            return
        }

        val sportType = when (betList[0].matchOdd.gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }

        sportType?.let {
            val groupList = groupBetInfoByMatchId()

            _matchOddList.value = groupList

            _parlayList.value = updateParlayOddOrder(
                getParlayOdd(MatchType.PARLAY, it, groupList).toMutableList()
            )

            betList.filter { betInfoListData ->
                _matchOddList.value?.any { matchOdd ->
                    matchOdd.oddsId == betInfoListData.matchOdd.oddsId
                } ?: false
            }
            _betInfoList.value = betList
        }
    }

    private fun groupBetInfoByMatchId(): MutableList<MatchOdd> {
        val betList = _betInfoList.value ?: mutableListOf()

        val groupList = betList.groupBy { data ->
            betList.find { d ->
                data.matchOdd.matchId == d.matchOdd.matchId
            }
        }

        return groupList.mapNotNull {
            it.key?.matchOdd
        }.toMutableList()
    }

    private fun updateParlayOddOrder(parlayOddList: MutableList<ParlayOdd>): MutableList<ParlayOdd> {
        //將串起來的數量賠率移至第一項
        val pOdd = parlayOddList.find {
            matchOddList.value?.size.toString() + "C1" == it.parlayType
        }

        parlayOddList.remove(pOdd)

        pOdd?.let { po ->
            parlayOddList.add(0, po)
        }

        return parlayOddList
    }

    fun getCurrentBetInfoList() {
        val betList = _betInfoList.value ?: mutableListOf()

        _betInfoList.postValue(betList)
    }


    fun removeItem(oddId: String?) {
        val betList = _betInfoList.value ?: mutableListOf()

        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)
        _removeItem.postValue(item?.matchOdd?.matchId)
        _betInfoList.postValue(betList)
    }


    fun clear() {
        val betList = _betInfoList.value ?: mutableListOf()

        betList.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

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
        val betList = _betInfoList.value ?: mutableListOf()

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
        val betList = _betInfoList.value ?: mutableListOf()

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
        val betList = _betInfoList.value ?: mutableListOf()

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
        val hasChanged = _betInfoList.value?.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.oddsHasChanged = true
    }

    private fun notifyBetInfoChanged() {
        val updateBetInfoList = _betInfoList.value

        if (updateBetInfoList.isNullOrEmpty()) return

        val sportType = when (updateBetInfoList[0].matchOdd.gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }

        when (_isParlayPage.value) {
            true -> {
                sportType?.let {
                    matchOddList.value?.let {
                        _parlayList.value =
                            getParlayOdd(MatchType.PARLAY, sportType, it).toMutableList()
                    }
                }
            }
            false -> {
                val newList = mutableListOf<BetInfoListData>()

                updateBetInfoList.forEach { betInfoListData ->
                    betInfoListData.matchType?.let { matchType ->
                        sportType?.let {

                            val newBetInfoListData = BetInfoListData(
                                betInfoListData.matchOdd,
                                getParlayOdd(
                                    matchType,
                                    sportType,
                                    mutableListOf(betInfoListData.matchOdd)
                                ).first()
                            )

                            newList.add(newBetInfoListData)
                        }
                    }
                }
                _betInfoList.value = newList
            }
        }
    }
}