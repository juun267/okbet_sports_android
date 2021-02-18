package org.cxct.sportlottery.ui.feedback

import android.content.Context
import android.provider.SyncStateContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.feedback.*
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.FeedbackRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import timber.log.Timber

class FeedbackViewModel(
    private val androidContext: Context,
    private val feedbackRepository: FeedbackRepository,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    //API回傳成功
    val viewStatus: LiveData<FeedBackBaseResult>
        get() = _viewStatus
    private var _viewStatus = MutableLiveData<FeedBackBaseResult>()

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    //意見反饋清單
    val feedbackList: LiveData<MutableList<FeedBackRows>?>
        get() = _feedbackList
    private var _feedbackList = MutableLiveData<MutableList<FeedBackRows>?>()

    //意見詳細資料
    val feedbackDetail: LiveData<MutableList<FeedBackRows>?>
        get() = _feedbackDetail
    private var _feedbackDetail = MutableLiveData<MutableList<FeedBackRows>?>()

    //使用者ID
    var userID: Long? = null

    //當前列表的ID
    var dataID: Long? = null

    //feedbackCode
    var feedbackCode: String? = null

    //API
    fun getFbQueryList() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = doNetwork(androidContext) {
                feedbackRepository.getFbQueryList(FeedbackListRequest())
            }
            if (result?.rows?.size ?: 0 > 0)
                _feedbackList.value = result?.rows

            _isLoading.value = false
        }

    }

    fun fbSave(content: String) { //目前只確定使用者會傳意見
        viewModelScope.launch {
            _isLoading.value = true
            var feedbackSaveRequest = FeedbackSaveRequest(content, 3, 0)
            val result = doNetwork(androidContext) {
                feedbackRepository.fbSave(feedbackSaveRequest)
            }
            _isLoading.value = false
        }
    }

    fun fbReply(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            var feedbackReplyRequest = FeedbackReplyRequest(content, feedbackCode.toString(), 0, 6)
            val result = doNetwork(androidContext) {
                feedbackRepository.fbReply(feedbackReplyRequest)
            }
            _isLoading.value = false
        }
    }

    fun fbQueryDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = doNetwork(androidContext) {
                feedbackRepository.fbQueryDetail(dataID.toString())
            }

            if (result?.rows?.size ?: 0 > 0)
                _feedbackDetail.value = result?.rows

            _isLoading.value = false
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
}