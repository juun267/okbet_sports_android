package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import timber.log.Timber
import java.util.*

class MoneyTransferViewModel(
        private val androidContext: Context,
        private val userInfoRepository: UserInfoRepository,
        private val loginRepository: LoginRepository,
        betInfoRepo: BetInfoRepository
) : BaseViewModel() {

}