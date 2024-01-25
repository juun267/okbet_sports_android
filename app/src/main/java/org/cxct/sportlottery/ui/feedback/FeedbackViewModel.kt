package org.cxct.sportlottery.ui.feedback

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.feedback.FeedBackRows
import org.cxct.sportlottery.network.feedback.FeedbackListRequest
import org.cxct.sportlottery.network.feedback.FeedbackReplyRequest
import org.cxct.sportlottery.network.feedback.FeedbackSaveRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil

class FeedbackViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val allStatusTag = "ALL_STATUS"

    //API回傳成功
    val feedBackBaseResult: LiveData<Event<NetResult>>
        get() = _feedBackBaseResult
    private var _feedBackBaseResult = MutableLiveData<Event<NetResult>>()

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    val isShowToolbar: LiveData<Int>
        get() = _isShowToolbar
    private var _isShowToolbar = MutableLiveData<Int>().apply { this.value = View.VISIBLE }

    val toolbarName: LiveData<String>
        get() = _toolbarName
    private val _toolbarName = MutableLiveData<String>()

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

    //最後頁
    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage
    private val _isFinalPage = MutableLiveData<Boolean>().apply { value = false }

    companion object {
        private const val PAGE_SIZE = 20 //預設每次載入20筆資料
    }

    private var mNextRequestPage = 1//未讀
    private var mIsGettingData = false //判斷請求任務是否進行中
    private var mNeedMoreLoading = false //資料判斷滑到底是否需要繼續加載

    fun setToolbarName(name: String) {
        _toolbarName.value = name
    }

    //API
    fun getFbQueryList(
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        status: String? = null,
        isReload: Boolean,
        currentTotalCount: Int
    ) {
//        _isLoading.value = true
        if (mIsGettingData) {
//            _isLoading.value = false
            return
        }
        mIsGettingData = true

        var mCurrentTotalCount = currentTotalCount
        viewModelScope.launch {

            if (isReload) {//重新載入
                mNextRequestPage = 1
//                _feedbackList.value = mutableListOf()
                mCurrentTotalCount = 0
                mNeedMoreLoading = true
                mNextRequestPage = 1
            }

            val filter = { firm: String? -> if (firm == allStatusTag) null else firm?.toIntOrNull() }

            if (mNeedMoreLoading) {
//                _isLoading.value = true
                val result = doNetwork(androidContext) {
                    val feedbackListRequest = FeedbackListRequest(pageSize = PAGE_SIZE,
                        page = mNextRequestPage,
                        startTime = startTime,
                        endTime = endTime,
                        status = filter(status))
                    FeedbackRepository.getFbQueryList(feedbackListRequest)
                }
                //判斷是不是可以再加載
                mNeedMoreLoading = (mCurrentTotalCount + (result?.rows?.size
                    ?: 0)) < result?.total ?: 0
                mNextRequestPage++

                if (result?.rows?.size ?: 0 > 0) {
                    _feedbackList.value = result?.rows
                } else if (isReload) {
                    _feedbackList.value = mutableListOf()
                }

                if (!mNeedMoreLoading) _isFinalPage.postValue(true)
            }
//            _isLoading.value = false
        }
        mIsGettingData = false
    }

    fun fbSave(content: String) { //目前只確定使用者會傳意見
        _isLoading.value = true
        val feedbackSaveRequest = FeedbackSaveRequest(content = content)
        viewModelScope.launch {
            doNetwork(androidContext) {
                FeedbackRepository.fbSave(feedbackSaveRequest)
            }?.let { result ->
                _feedBackBaseResult.value = Event(result)
            }
            _isLoading.value = false
        }
    }

    fun fbReply(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val feedbackReplyRequest = FeedbackReplyRequest(content, feedbackCode.toString(), 0, 6)
            doNetwork(androidContext) {
                FeedbackRepository.fbReply(feedbackReplyRequest)
            }.let {
                if (it?.success == true) fbQueryDetail()
            }
            _isLoading.value = false
        }
    }

    fun fbQueryDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            doNetwork(androidContext) {
                FeedbackRepository.fbQueryDetail(dataID.toString())
            }.let { result ->
                if (result?.rows?.size ?: 0 > 0) _feedbackDetail.value = result?.rows
            }
            _isLoading.value = false
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                UserInfoRepository.getUserInfo()
            }
            userID = result?.userInfoData?.userId
        }
    }

    fun showToolbar(isShow: Boolean) {
        if (isShow) _isShowToolbar.value = View.VISIBLE
        else _isShowToolbar.value = View.GONE
    }

}