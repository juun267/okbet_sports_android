package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
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
    val isCreditAccount: LiveData<Boolean> = loginRepository.isCreditAccount

    val thirdGameCategory: LiveData<Event<ThirdGameCategory?>>
        get() = _thirdGameCategory

    val intentClass: LiveData<Event<Class<*>>>
        get() = _intentClass

    val showShoppingCart: LiveData<Boolean>
        get() = _showShoppingCart

    val nowTransNum by lazy { loginRepository.transNum}

    private val _thirdGameCategory = MutableLiveData<Event<ThirdGameCategory?>>()
    private val _intentClass = MutableLiveData<Event<Class<*>>>()
    private val _showShoppingCart = MutableLiveData<Boolean>()

    fun getTransNum() {
        if (isLogin.value == true) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    loginRepository.getTransNum()
                }
            }
        }
    }

    fun navMainPage(thirdGameCategory: ThirdGameCategory) {
        _thirdGameCategory.postValue(
            Event(
                if (isCreditAccount.value == true) {
                    null
                } else {
                    thirdGameCategory
                }
            )
        )
    }

    fun navGame() {
        _intentClass.postValue(Event(GameActivity::class.java))
    }

    fun navMyFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        _intentClass.postValue(Event(MyFavoriteActivity::class.java))
    }

    fun navAccountHistory() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        _intentClass.postValue(Event(AccountHistoryActivity::class.java))
    }

    fun navTranStatus() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
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