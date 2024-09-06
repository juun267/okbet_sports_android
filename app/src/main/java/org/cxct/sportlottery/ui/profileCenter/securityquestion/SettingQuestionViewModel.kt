package org.cxct.sportlottery.ui.profileCenter.securityquestion

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.SafeQuestion
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.SingleLiveEvent

class SettingQuestionViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    val safeQuestionEvent = SingleLiveEvent<List<SafeQuestion>>()
    val setQuestionEvent = SingleLiveEvent<ApiResult<String>>()


    /**
     * 获取密保问题列表
     */
    fun querySafeQuestionType() {
        callApi({ UserRepository.querySafeQuestionType()}){
            if (it.succeeded()){
                safeQuestionEvent.postValue(it.getData())
            }else{
                toast(it.msg)
            }
        }
    }

    fun setSafeQuestion(safeQuestionType: Int,answer: String, password: String) {
        callApi({ UserRepository.setSafeQuestion(safeQuestionType,answer, MD5Util.MD5Encode(password))}){
            setQuestionEvent.postValue(it)
        }
    }
    fun getUserInfo() {
        viewModelScope.launch {
            runWithCatch { UserInfoRepository.getUserInfo() }
        }
    }
}