package org.cxct.sportlottery.ui.feedback

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.feedback.*
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel

class FeedbackViewModel(
    private val androidContext: Context,
    private val feedbackRepository: FeedbackRepository,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

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

    //最後頁
    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage
    private val _isFinalPage = MutableLiveData<Boolean>().apply { value = false }

    //Bottomsheet Data
    val typeMap = mapOf(0 to "充值问题",1 to "提款问题",2 to "其他问题",3 to "提交建议",4 to "我要投诉",5 to "客服反馈",6 to "玩家回复")
    val statusMap = mapOf(0 to "待反馈",1 to "已反馈")

    //API Input
    var feedbackListRequest = FeedbackListRequest()


    private var mNextRequestPage = 1//未讀
    private val pageSize = 20 //預設每次載入20筆資料
    private var mIsGettingData = false //判斷請求任務是否進行中
    private var mNeedMoreLoading = false //資料判斷滑到底是否需要繼續加載


    //API
    fun getFbQueryList(
        isReload: Boolean,
        currentTotalCount: Int
    ) {
        _isLoading.value = true
        if (mIsGettingData) {
            _isLoading.value = false
            return
        }
        mIsGettingData = true

        var mCurrentTotalCount = currentTotalCount

        viewModelScope.launch {

            if (isReload) {//重新載入
                mNextRequestPage = 1
                _feedbackList.value = mutableListOf()
                mCurrentTotalCount = 0
                mNeedMoreLoading = true
                mNextRequestPage = 1
            }
            if(mNeedMoreLoading){
                _isLoading.value = true
                val result = doNetwork(androidContext) {
                    feedbackListRequest.page = mNextRequestPage
                    feedbackListRequest.pageSize = pageSize
                    feedbackRepository.getFbQueryList(feedbackListRequest)
                }
                //判斷是不是可以再加載
                mNeedMoreLoading =
                    (mCurrentTotalCount + (result?.rows?.size
                        ?: 0)) < result?.total ?: 0
                mNextRequestPage++

                if (result?.rows?.size ?: 0 > 0)
                    _feedbackList.value = result?.rows

                if(!mNeedMoreLoading)
                    _isFinalPage.postValue(true)
            }
            _isLoading.value = false
        }
        mIsGettingData = false
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