package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.util.Event

abstract class BaseBottomNavViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    val intentClass: LiveData<Event<Class<*>>>
        get() = _intentClass
    private val _intentClass = MutableLiveData<Event<Class<*>>>()

    val showShoppingCart: LiveData<Event<Boolean>>
        get() = _showShoppingCart

    val navPublicityPage: LiveData<Event<Boolean>>
        get() = _navPublicityPage
    val settlementNotificationMsg
        get() = betInfoRepository.settlementNotificationMsg

    private val _showShoppingCart = MutableLiveData<Event<Boolean>>()
    private val _navPublicityPage = MutableLiveData<Event<Boolean>>()


}