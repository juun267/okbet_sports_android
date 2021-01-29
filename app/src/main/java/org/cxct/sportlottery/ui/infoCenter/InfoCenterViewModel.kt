package org.cxct.sportlottery.ui.infoCenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class InfoCenterViewModel(
    private val androidContext: Context,
    private val infoCenterRepository: InfoCenterRepository,
    betInfoRepo: BetInfoRepository
) : BaseViewModel() {

    init {
        betInfoRepository = betInfoRepo
    }

    enum class DataType { UNREAD, READED }//未讀,已讀

    //未讀資料
    val userUnreadMsgList: LiveData<MutableList<InfoCenterData>?>
        get() = _userUnreadMsgList
    private var _userUnreadMsgList = MutableLiveData<MutableList<InfoCenterData>?>()

    //已讀資料
    val userReadedMsgList: LiveData<MutableList<InfoCenterData>?>
        get() = _userReadedMsgList
    private var _userReadedMsgList = MutableLiveData<MutableList<InfoCenterData>?>()

    //未讀總數目
    val totalUnreadMsgCount: LiveData<Int>
        get() = _totalUnreadMsgCount
    private var _totalUnreadMsgCount = MutableLiveData<Int>()

    //已讀總數目
    val totalReadedMsgCount: LiveData<Int>
        get() = _totalReadedMsgCount
    private var _totalReadedMsgCount = MutableLiveData<Int>()

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    private var mUnReadNextRequestPage = 1//未讀
    private var mIsReadNextRequestPage = 1//已讀

    private var pageSize = 20 //預設每次載入20筆資料
    private var mIsGettingData = false //判斷請求任務是否進行中
    private var mNeedMoreLoadingReaded = false //已讀資料判斷滑到底是否需要繼續加載
    private var mNeedMoreLoadingUnRead = false //未讀資料判斷滑到底是否需要繼續加載


    fun getUserMsgList(isReload: Boolean, currentTotalCount: Int, dataType: DataType) {
        _isLoading.value = true
        if (mIsGettingData) {
            _isLoading.value = false
            return
        }
        mIsGettingData = true

        var mCurrentTotalCount = currentTotalCount
        if (isReload) {//重新載入
            when (dataType) {
                DataType.UNREAD -> {
                    mUnReadNextRequestPage = 1
                    _userUnreadMsgList.value = mutableListOf()
                    mCurrentTotalCount = 0
                    mNeedMoreLoadingUnRead = true
                }
                DataType.READED -> {
                    mIsReadNextRequestPage = 1
                    _userReadedMsgList.value = mutableListOf()
                    mCurrentTotalCount = 0
                    mNeedMoreLoadingReaded = true
                }
            }
            mIsReadNextRequestPage = 1
        }

        when (dataType) {
            DataType.UNREAD -> {
                if (mNeedMoreLoadingUnRead) {
                    viewModelScope.launch {
                        val result = doNetwork(androidContext) {
                            val infoCenterRequest =
                                InfoCenterRequest(mUnReadNextRequestPage, pageSize, 0)
                            infoCenterRepository.getUserNoticeList(infoCenterRequest)
                        }

                        _userUnreadMsgList.value = result?.infoCenterData
                        _totalUnreadMsgCount.value = result?.total

                        //判斷是不是可以再加載
                        mNeedMoreLoadingUnRead =
                            (mCurrentTotalCount + (result?.infoCenterData?.size
                                ?: 0)) < result?.total ?: 0
                        mUnReadNextRequestPage++
                    }
                }
            }
            DataType.READED -> {
                if (mNeedMoreLoadingReaded) {
                    viewModelScope.launch {
                        val result = doNetwork(androidContext) {
                            val infoCenterRequest =
                                InfoCenterRequest(mIsReadNextRequestPage, pageSize, 1)
                            infoCenterRepository.getUserNoticeList(infoCenterRequest)
                        }

                        _userReadedMsgList.value = result?.infoCenterData
                        _totalReadedMsgCount.value = result?.total

                        //判斷是不是可以再加載
                        mNeedMoreLoadingReaded =
                            (mCurrentTotalCount + (result?.infoCenterData?.size
                                ?: 0)) < result?.total ?: 0
                        mIsReadNextRequestPage++
                    }
                }
            }
        }

        mIsGettingData = false
        _isLoading.value = false
    }

    //只取得資料總筆數(Tab顯示要用)
    fun getMsgCount(dataType: DataType) {
        viewModelScope.launch {
            _isLoading.value = true
            var isRead = when (dataType) {
                DataType.UNREAD -> 0
                DataType.READED -> 1
            }
            val result = doNetwork(androidContext) {
                val infoCenterRequest =
                    InfoCenterRequest(1, 20, isRead)
                infoCenterRepository.getUserNoticeList(infoCenterRequest)
            }
            when (dataType) {
                DataType.UNREAD -> {
                    _totalUnreadMsgCount.value = result?.total
                }
                DataType.READED -> {
                    _totalReadedMsgCount.value = result?.total
                }
            }
            _isLoading.value = false
        }
    }

    fun setDataReaded(msgId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            doNetwork(androidContext) {
                infoCenterRepository.setMsgReaded(msgId)
            }
            _isLoading.value = false
        }
    }

}