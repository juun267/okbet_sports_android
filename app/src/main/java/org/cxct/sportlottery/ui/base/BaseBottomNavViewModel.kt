package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusActivity
import org.cxct.sportlottery.util.Event

abstract class BaseBottomNavViewModel(
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

    val intentClass: LiveData<Event<Class<*>>>
        get() = _intentClass
    private val _intentClass = MutableLiveData<Event<Class<*>>>()

    val showShoppingCart: LiveData<Boolean>
        get() = _showShoppingCart
    private val _showShoppingCart = MutableLiveData<Boolean>()

    fun navGame() {
        _intentClass.postValue(Event(GameActivity::class.java))
    }

    fun navMyFavorite() {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        _intentClass.postValue(Event(MyFavoriteActivity::class.java))
    }

    fun navAccountHistory() {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        _intentClass.postValue(Event(AccountHistoryActivity::class.java))
    }

    fun navTranStatus() {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        _intentClass.postValue(Event(TransactionStatusActivity::class.java))
    }

    fun navShoppingCart() {
        _showShoppingCart.postValue(
            betInfoRepository.betInfoList.value?.peekContent()?.isNotEmpty()
        )
    }
}