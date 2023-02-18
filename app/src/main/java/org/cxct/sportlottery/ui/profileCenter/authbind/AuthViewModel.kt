package org.cxct.sportlottery.ui.profileCenter.authbind

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event

class AuthViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val avatarRepository: AvatarRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val bindGoogleResult: LiveData<Event<AuthBindResult>>
        get() = _bindGoogleResult
    private val _bindGoogleResult = MutableLiveData<Event<AuthBindResult>>()

    val bindFacebookResult: LiveData<Event<AuthBindResult>>
        get() = _bindFacebookResult
    private val _bindFacebookResult = MutableLiveData<Event<AuthBindResult>>()

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoRepository.getUserInfo()
        }
    }

    fun bindGoogle(token: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.bindGoogle(token)
            }?.let {
                _bindGoogleResult.postValue(Event(it))
            }
        }
    }

    fun bindFacebook(token: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.bindFaceBook(token)
            }?.let {
                _bindFacebookResult.postValue(Event(it))
            }
        }
    }
}