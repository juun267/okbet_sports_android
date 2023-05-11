package org.cxct.sportlottery.ui.sport.filter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.VerifyConstUtil
import java.text.Collator
import java.util.*

class LeagueSelectViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {

    val leagueListResult: LiveData<Event<LeagueListResult?>>
        get() = _leagueListResult
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()

    val leagueList: LiveData<MutableList<League>>
        get() = _leagueList
    private val _leagueList = MutableLiveData<MutableList<League>>()


    fun getLeagueList(
        gameType: String,
        matchType: String,
        startTime: String,
        endTime: String?,
        date: String? = null,
        leagueIdList: List<String>? = null,
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.leagueService.getLeagueList(
                    LeagueListRequest(
                        gameType,
                        matchType,
                        startTime = startTime,
                        endTime = endTime,
                        date = date
                    )
                )
            }

            var leagueData = mutableListOf<League>()
            result?.rows?.forEach { row ->
                row.list.forEach {
                    it.firstCap = Pinyin.toPinyin(it.name.first()).first().toString()
                    it.icon = row.icon
                    it.isSelected =
                        if (leagueIdList.isNullOrEmpty()) true else leagueIdList.contains(it.id)
                }
                leagueData.addAll(row.list)

            }
            _leagueListResult.value = (Event(result))

            val compar = Collator.getInstance(Locale.CHINESE)
            Collections.sort(leagueData, kotlin.Comparator { o1, o2 ->
                compar.compare(o1.name, o2.name)
            })
            leagueData.filter { !VerifyConstUtil.isValidEnglishWord(it.firstCap) }?.forEach {
                it.firstCap = "#"
            }
            _leagueList.postValue(leagueData)

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    fun clearSelectedLeague() {
        _leagueList.postValue(mutableListOf())
    }
}