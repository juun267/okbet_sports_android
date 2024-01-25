package org.cxct.sportlottery.ui.profileCenter.authbind

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event

class AuthViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext,
) {
    val bindGoogleResult: LiveData<Event<AuthBindResult>>
        get() = _bindGoogleResult
    private val _bindGoogleResult = MutableLiveData<Event<AuthBindResult>>()

    val bindFacebookResult: LiveData<Event<AuthBindResult>>
        get() = _bindFacebookResult
    private val _bindFacebookResult = MutableLiveData<Event<AuthBindResult>>()

    fun getUserInfo() {
        viewModelScope.launch {
            runWithCatch { UserInfoRepository.getUserInfo() }
        }
    }

    fun bindGoogle(token: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                LoginRepository.bindGoogle(token)
            }?.let {
                _bindGoogleResult.postValue(Event(it))
            }
        }
    }

    fun bindFacebook(token: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                LoginRepository.bindFaceBook(token)
            }?.let {
                _bindFacebookResult.postValue(Event(it))
            }
        }
    }
}