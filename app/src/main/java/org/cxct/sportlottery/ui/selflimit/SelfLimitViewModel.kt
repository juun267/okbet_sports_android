package org.cxct.sportlottery.ui.selflimit

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.user.selflimit.FrozeRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeResult
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitRequest
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class SelfLimitViewModel(
    androidContext: Application,
    private val repository: SelfLimitRepository,
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

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    val isShowToolbar: LiveData<Int>
        get() = _isShowToolbar
    private var _isShowToolbar = MutableLiveData<Int>().apply { this.value = View.VISIBLE }

    val toolbarName: LiveData<String>
        get() = _toolbarName
    val frozeResult:LiveData<FrozeResult>
        get() = _frozeResult
    val perBetLimitResult:LiveData<PerBetLimitResult>
        get() = _perBetLimitResult
    private val _toolbarName = MutableLiveData<String>()
    private val _frozeResult = MutableLiveData<FrozeResult>()
    private val _perBetLimitResult = MutableLiveData<PerBetLimitResult>()

    val isBetEditTextError: LiveData<Boolean>
        get() = _isBetEditTextError
    private val _isBetEditTextError = MutableLiveData<Boolean>()

    val isFrozeEditTextError: LiveData<Boolean>
        get() = _isFrozeEditTextError
    private val _isFrozeEditTextError = MutableLiveData<Boolean>()


    //使用者ID
    var userID: Long? = null


    fun setToolbarName(name: String) {
        _toolbarName.value = name
    }


    fun setPerBetLimit(mount:Int) {
        var request = PerBetLimitRequest(mount)

        viewModelScope.launch {
            doNetwork(androidContext) {
                repository.setPerBetLimit(request)
            }.let { result ->
                _perBetLimitResult.value = result
            }
        }
    }




    fun setFroze(day:Int) {
        var frozeRequest = FrozeRequest(day)

        viewModelScope.launch {
            doNetwork(androidContext) {
                repository.froze(frozeRequest)
            }.let { result ->
                _frozeResult.value = result
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                userInfoRepository.getUserInfo()
            }
            userID = result?.userInfoData?.userId
        }
    }

    fun showToolbar(isShow: Boolean) {
        if (isShow) _isShowToolbar.value = View.VISIBLE
        else _isShowToolbar.value = View.GONE
    }

    fun setBetEditTextError(boolean: Boolean){
        _isBetEditTextError.postValue(boolean)
    }

    fun setFrozeEditTextError(boolean: Boolean){
        _isFrozeEditTextError.postValue(boolean)
    }

}