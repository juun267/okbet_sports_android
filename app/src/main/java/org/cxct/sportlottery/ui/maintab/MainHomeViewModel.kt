package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.SportMenuFilter
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.sport.publicityRecommend.RecommendResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.isTimeAtStart
import org.cxct.sportlottery.util.TimeUtil.isTimeToday
import java.text.SimpleDateFormat
import java.util.*

class MainHomeViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
) {
    private val _publicityRecommend = MutableLiveData<Event<RecommendResult>>()
    val publicityRecommend: LiveData<Event<RecommendResult>>
        get() = _publicityRecommend

    private val _sportMenuFilterList =
        MutableLiveData<Event<MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?>>()

    fun getRecommend() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val currentTimeMillis = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentTimeMillis
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val startTimeStamp = timeFormat.parse("$year-$month-$day 00:00:00").time

                OneBoSportApi.sportService.getPublicityRecommend(
                    PublicityRecommendRequest(
                        currentTimeMillis.toString(),
                        startTimeStamp.toString()
                    )
                )
            }?.let { result ->
                if (result.success) {
                    result.result.recommendList.forEach { recommend ->
                        with(recommend) {
                            /*recommend.matchInfo = MatchInfo(
                                gameType = gameType,
                                awayName = awayName,
                                homeName = homeName,
                                playCateNum = matchNum,
                                startTime = startTime,
                                status = status,
                                leagueId = leagueId,
                                leagueName = leagueName,
                                id = id,
                                endTime = 0
                            ).apply {
                                setupMatchType(this)
                                setupMatchTime(this)
                            }*/
                        }
                    }

                    _publicityRecommend.postValue(Event(result.result))
                }
            }
        }
    }

    //獲取體育篩選菜單
    fun getSportMenuFilter() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportListFilter()
            }

            result?.let {
                PlayCateMenuFilterUtils.filterList = it.t?.sportMenuList
                _sportMenuFilterList.postValue(Event(it.t?.sportMenuList))
            }
        }
    }

    //region 宣傳頁推薦賽事資料處理
    /**
     * 設置賽事類型參數(滾球、即將、今日、早盤)
     */
    private fun Recommend.setupMatchType(matchInfo: MatchInfo) {
        matchType = when (status) {
            1 -> {
                matchInfo.isInPlay = true
                MatchType.IN_PLAY
            }
            else -> {
                when {
                    isTimeAtStart(startTime) -> {
                        matchInfo.isAtStart = true
                        MatchType.AT_START
                    }
                    isTimeToday(startTime) -> {
                        MatchType.TODAY
                    }
                    else -> {
                        MatchType.EARLY
                    }
                }
            }
        }
    }

    /**
     * 設置賽事時間參數
     */
    private fun setupMatchTime(matchInfo: MatchInfo) {
        matchInfo.startDateDisplay = TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

        matchInfo.startTimeDisplay = TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

        matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)
    }
    //endregion
}