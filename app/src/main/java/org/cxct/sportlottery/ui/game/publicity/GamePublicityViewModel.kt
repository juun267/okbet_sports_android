package org.cxct.sportlottery.ui.game.publicity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.RecommendResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil.isTimeAtStart
import org.cxct.sportlottery.util.TimeUtil.isTimeToday
import java.text.SimpleDateFormat
import java.util.*

class GamePublicityViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    private val _publicityRecommend = MutableLiveData<Event<RecommendResult>>()
    val publicityRecommend: LiveData<Event<RecommendResult>>
        get() = _publicityRecommend

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
                    _publicityRecommend.postValue(Event(result.result.setupMatchType()))
                }
            }
        }
    }

    fun RecommendResult.setupMatchType(): RecommendResult {
        recommendList.forEach { data ->
            data.matchType = when (data.status) {
                1 -> MatchType.IN_PLAY
                else -> {
                    when {
                        isTimeAtStart(data.startTime) -> MatchType.AT_START
                        isTimeToday(data.startTime) -> MatchType.TODAY
                        else -> MatchType.EARLY
                    }
                }
            }
        }
        return this
    }
}