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
    favoriteRepository: MyFavoriteRepository,
    private val intentRepository: IntentRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val thirdGameCategory: LiveData<Event<ThirdGameCategory?>>
        get() = _thirdGameCategory

    val intentClass: LiveData<Event<Class<*>>>
        get() = intentRepository.intentClass

    val showShoppingCart: LiveData<Boolean>
        get() = _showShoppingCart

    val nowTransNum: LiveData<Int?> get() = loginRepository.transNum
    val navPublicityPage: LiveData<Event<Boolean>>
        get() = _navPublicityPage

    private val _thirdGameCategory = MutableLiveData<Event<ThirdGameCategory?>>()
    private val _showShoppingCart = MutableLiveData<Boolean>()
    private val _navPublicityPage = MutableLiveData<Event<Boolean>>()

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
                if (sConfigData?.thirdOpen != FLAG_OPEN) {
                    _navPublicityPage.postValue(Event(true))
                    null
                } else {
                    thirdGameCategory
                }
            )
        )
    }

    fun navGame() {
        intentRepository.setIntentClassLiveData(GameActivity::class.java)
    }

    fun navMyFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        intentRepository.setIntentClassLiveData(MyFavoriteActivity::class.java)
    }

    fun navAccountHistory() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        intentRepository.setIntentClassLiveData(AccountHistoryActivity::class.java)
    }

    fun navTranStatus() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        intentRepository.setIntentClassLiveData(TransactionStatusActivity::class.java)
    }

    fun navShoppingCart() {
        _showShoppingCart.postValue(
//            betInfoRepository.betInfoList.value?.peekContent()?.isNotEmpty() //注單為0時，不可以打開投注單
            true //2022/1/11新需求，注單為0時可以開啟投注單，並且顯示特定UI by Bill
        )
    }
}