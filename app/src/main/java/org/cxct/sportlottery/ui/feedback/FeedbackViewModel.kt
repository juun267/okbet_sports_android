package org.cxct.sportlottery.ui.feedback

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.feedback.*
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.FeedbackRepository
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.base.BaseViewModel
import timber.log.Timber

class FeedbackViewModel(
    private val androidContext: Context,
    private val feedbackRepository: FeedbackRepository,
    betInfoRepo: BetInfoRepository
) : BaseViewModel() {

    init {
        betInfoRepository = betInfoRepo
    }

    //API回傳成功
    val viewStatus: LiveData<FeedBackBaseResult>
        get() = _viewStatus
    private var _viewStatus = MutableLiveData<FeedBackBaseResult>()

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    //意見反饋清單
    val feedbackList: LiveData<FeedbackListResult?>
        get() = _feedbackList
    private var _feedbackList = MutableLiveData<FeedbackListResult?>()


    //API
    fun getFbQueryList() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = doNetwork(androidContext) {
                feedbackRepository.getFbQueryList(FeedbackListRequest())
            }
            Timber.e(">>>input = ${FeedbackListRequest()}, result = ${result}, url = ${Constants.FEEDBACK_QUERYLIST}")
            _feedbackList.value = result
        }
        _isLoading.value = false
    }

    fun fbSave(content: String) { //目前只確定使用者會傳意見
        viewModelScope.launch {
            _isLoading.value = true
            var feedbackSaveRequest = FeedbackSaveRequest(content, 3, 0)
            val result = doNetwork(androidContext) {
                feedbackRepository.fbSave(feedbackSaveRequest)
            }
            Timber.e(">>>input = ${feedbackSaveRequest}, result = ${result}, url = ${Constants.FEEDBACK_SAVE}")
        }
        _isLoading.value = false
    }

    fun fbReply() {
        viewModelScope.launch {
            _isLoading.value = true
            var feedbackReplyRequest = FeedbackReplyRequest("", "", 0, 0)
            val result = doNetwork(androidContext) {
                feedbackRepository.fbReply(feedbackReplyRequest)
            }
            Timber.e(">>>input = ${feedbackReplyRequest}, result = ${result}, url = ${Constants.FEEDBACK_REPLY}")
        }
        _isLoading.value = false
    }

    fun fbQueryDetail() {
        _isLoading.value = true
        viewModelScope.launch {
            var id = "123"
            val result = doNetwork(androidContext) {
                feedbackRepository.fbQueryDetail(id)
            }
            Timber.e(">>>input = ${id}, result = ${result}, url = ${Constants.FEEDBACK_REPLY}")
        }
        _isLoading.value = false
    }

}