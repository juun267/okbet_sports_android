package org.cxct.sportlottery.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuota
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil


const val BET_INFO_MAX_COUNT = 10


class BetInfoRepository {


    private val _showBetInfoSingle = MutableLiveData<Event<Boolean?>>()


    val showBetInfoSingle: LiveData<Event<Boolean?>>
        get() = _showBetInfoSingle


    //每個畫面都要觀察
    private val _betInfoList = MutableLiveData<Event<MutableList<BetInfoListData>>>().apply {
        value = Event(mutableListOf())
    }
    val betInfoList: LiveData<Event<MutableList<BetInfoListData>>>
        get() = _betInfoList


    private val _matchOddList = MutableLiveData<MutableList<MatchOdd>>()
    val matchOddList: LiveData<MutableList<MatchOdd>>
        get() = _matchOddList


    private val _parlayList = MutableLiveData<MutableList<ParlayOdd>>()
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = _parlayList


    val _isParlayPage = MutableLiveData<Boolean>().apply {
        value = false
    }
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
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size == 0) {
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
            val parlayMatchOddList = betList.map { betInfoListData ->
                betInfoListData.matchOdd
            }.toMutableList()

            _matchOddList.value = parlayMatchOddList

            _parlayList.value = updateParlayOddOrder(
                getParlayOdd(MatchType.PARLAY, it, parlayMatchOddList).toMutableList()
            )
        }
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
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        _betInfoList.postValue(Event(betList))
    }


    fun removeItem(oddId: String?) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)
        _removeItem.postValue(item?.matchOdd?.matchId)
        _betInfoList.postValue(Event(betList))
    }


    fun clear() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        betList.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        _betInfoList.postValue(Event(betList))
    }


    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String,
        playName: String,
        matchOdd: org.cxct.sportlottery.network.odds.list.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType, sportType.code, playCateName, playName, matchOdd, odd
        )

        betInfoMatchOdd?.let {
            val data = BetInfoListData(
                betInfoMatchOdd,
                getParlayOdd(matchType, sportType, mutableListOf(it)).first()
            ).apply {
                this.matchType = matchType
            }

            if (betList.size == 0) {
                _showBetInfoSingle.postValue(Event(true))
            }

            betList.add(data)
            _betInfoList.postValue(Event(betList))
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
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            sportType.code, playCateName, playName, matchOdd, odd
        )

        betInfoMatchOdd?.let {
            val data = BetInfoListData(
                betInfoMatchOdd,
                getParlayOdd(matchType, sportType, mutableListOf(it)).first()
            ).apply {
                this.matchType = matchType
            }

            if (betList.size == 0) {
                _showBetInfoSingle.postValue(Event(true))
            }

            betList.add(data)
            _betInfoList.postValue(Event(betList))
        }
    }


    fun addInBetInfo(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.odds.detail.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.detail.Odd
    ) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size >= BET_INFO_MAX_COUNT) return

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType, sportType.code, playCateName, matchOdd, odd
        )

        betInfoMatchOdd?.let {
            val data = BetInfoListData(
                betInfoMatchOdd,
                getParlayOdd(matchType, sportType, mutableListOf(it)).first()
            ).apply {
                this.matchType = matchType
            }

            if (betList.size == 0) {
                _showBetInfoSingle.postValue(Event(true))
            }

            betList.add(data)
            _betInfoList.postValue(Event(betList))
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
        val hasChanged = _betInfoList.value?.peekContent()?.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.matchOdd?.oddsHasChanged = true
        hasChanged?.matchOdd?.oddState = OddState.SAME.state
    }


    private fun getSportType(gameType: String): SportType? {
        return when (gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }
    }


    fun notifyBetInfoChanged() {
        val updateBetInfoList = _betInfoList.value?.peekContent()

        if (updateBetInfoList.isNullOrEmpty()) return

        when (_isParlayPage.value) {
            true -> {
                val sportType = getSportType(updateBetInfoList[0].matchOdd.gameType)
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
                        val sportType = getSportType(betInfoListData.matchOdd.gameType)
                        sportType?.let {
                            val newBetInfoListData = BetInfoListData(
                                betInfoListData.matchOdd,
                                getParlayOdd(
                                    matchType,
                                    sportType,
                                    mutableListOf(betInfoListData.matchOdd)
                                ).first()
                            )
                            newBetInfoListData.matchType = betInfoListData.matchType
                            newBetInfoListData.input = betInfoListData.input
                            newList.add(newBetInfoListData)
                        }
                    }
                }
                _betInfoList.value = Event(newList)
            }
        }
    }


}