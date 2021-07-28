package org.cxct.sportlottery.ui.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseFavoriteViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil

class MyFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseFavoriteViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
    val sportQueryData: LiveData<Event<SportQueryData?>>
        get() = _sportQueryData
    private val _sportQueryData = MutableLiveData<Event<SportQueryData?>>()

    val curPlay: LiveData<Play>
        get() = _curPlay
    private val _curPlay = MutableLiveData<Play>()

    fun getSportQuery() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        MatchType.MY_EVENT.postValue
                    )
                )
            }

            result?.sportQueryData?.let {

                _sportQueryData.postValue(Event(
                    it.apply {
                        it.items?.firstOrNull()?.apply {
                            this.isSelected = true
                            this.play?.firstOrNull()?.isSelected = true
                        }
                    }
                ))

            }
        }
    }

    fun switchGameType(item: Item) {
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updateGameTypeSelected(item).apply {
                    val curPlayList = this?.items?.find { it.isSelected }?.play

                    curPlayList?.forEach {
                        it.isSelected = (curPlayList.indexOf(it) == 0)
                    }
                }
            )
        )
    }

    fun switchPlay(play: Play) {
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updatePlaySelected(play).apply {
                    val curPlayCate =
                        this?.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList

                    curPlayCate?.forEach {
                        it.isSelected =
                            (curPlayCate.indexOf(it) == 0)
                                    && (this?.items?.find { item -> item.isSelected }?.play?.find { play -> play.isSelected }?.selectionType == SelectionType.SELECTABLE.code)
                    }
                }
            )
        )

        if (play.selectionType == SelectionType.SELECTABLE.code) {
            _curPlay.postValue(play)
        }
    }

    fun switchPlayCategory(playCateCode: String?) {
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updatePlayCateSelected(playCateCode)
            )
        )
    }

    private fun SportQueryData.updateGameTypeSelected(item: Item): SportQueryData {
        this.items?.forEach {
            it.isSelected = (it.code == item.code)
        }
        return this
    }

    private fun SportQueryData.updatePlaySelected(play: Play): SportQueryData {
        this.items?.find { it.isSelected }?.play?.forEach {
            it.isSelected = (it == play)
        }
        return this
    }

    private fun SportQueryData.updatePlayCateSelected(playCateCode: String?): SportQueryData {
        this.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList?.forEach {
            it.isSelected = (it.code == playCateCode)
        }
        return this
    }
}