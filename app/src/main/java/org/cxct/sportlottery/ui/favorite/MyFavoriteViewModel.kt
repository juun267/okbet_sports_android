package org.cxct.sportlottery.ui.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
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

    val gameTypeList: LiveData<Event<List<Item>?>>
        get() = _gameTypeList

    private val _gameTypeList = MutableLiveData<Event<List<Item>?>>()


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

                _gameTypeList.postValue(Event(
                    it.items?.map { item ->
                        Item(
                            code = item.code ?: "",
                            name = item.name ?: "",
                            num = item.num ?: 0,
                            play = null,
                            sortNum = item.sortNum ?: 0
                        )
                    }.apply {
                        this?.firstOrNull()?.isSelected = true
                    })
                )

            }
        }
    }

    fun switchGameType(item: Item) {
        _gameTypeList.postValue(
            Event(
                _gameTypeList.value?.peekContent()?.updateGameTypeSelected(item)
            )
        )
    }

    private fun List<Item>.updateGameTypeSelected(item: Item): List<Item> {
        this.forEach {
            it.isSelected = (it.code == item.code)
        }
        return this
    }
}