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
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import timber.log.Timber

class InfoCenterViewModel(
    private val androidContext: Context,
    infoCenterRepository: InfoCenterRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    enum class DataType { UNREAD, READ }//未讀,已讀

    //未讀資料
    val userUnreadMsgList: LiveData<MutableList<InfoCenterData>?>
        get() = _userUnreadMsgList
    private var _userUnreadMsgList = MutableLiveData<MutableList<InfoCenterData>?>()

    //已讀資料
    val userReadMsgList: LiveData<MutableList<InfoCenterData>?>
        get() = _userReadMsgList
    private var _userReadMsgList = MutableLiveData<MutableList<InfoCenterData>?>()

    //未讀總數目
    val totalUnreadMsgCount: LiveData<Int>
        get() = _totalUnreadMsgCount
    private var _totalUnreadMsgCount = MutableLiveData<Int>()

    //已讀總數目
    val totalReadMsgCount: LiveData<Int>
        get() = _totalReadMsgCount
    private var _totalReadMsgCount = MutableLiveData<Int>()

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    private var mUnReadNextRequestPage = 1//未讀
    private var mIsReadNextRequestPage = 1//已讀

    private var pageSize = 20 //預設每次載入20筆資料
    private var mIsGettingData = false //判斷請求任務是否進行中

    private var mNeedMoreLoadingRead = false //已讀資料判斷滑到底是否需要繼續加載
    private var mNeedMoreLoadingUnRead = false //未讀資料判斷滑到底是否需要繼續加載


    fun getUserMsgList(isReload: Boolean = true, currentTotalCount: Int = 0, dataType: DataType) {
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
                DataType.READ -> {
                    mIsReadNextRequestPage = 1
                    _userReadMsgList.value = mutableListOf()
                    mCurrentTotalCount = 0
                    mNeedMoreLoadingRead = true
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
                        _totalUnreadMsgCount.value = result?.total?: 0

                        //判斷是不是可以再加載
                        mNeedMoreLoadingUnRead =
                            (mCurrentTotalCount + (result?.infoCenterData?.size
                                ?: 0)) < result?.total ?: 0
                        mUnReadNextRequestPage++
                        getResult()
                    }
                }else{
                    getResult()
                }
            }
            DataType.READ -> {
                if (mNeedMoreLoadingRead) {
                    viewModelScope.launch {
                        val result = doNetwork(androidContext) {
                            val infoCenterRequest =
                                InfoCenterRequest(mIsReadNextRequestPage, pageSize, 1)
                            infoCenterRepository.getUserNoticeList(infoCenterRequest)
                        }

                        _userReadMsgList.value = result?.infoCenterData
                        _totalReadMsgCount.value = result?.total?: 0

                        //判斷是不是可以再加載
                        mNeedMoreLoadingRead =
                            (mCurrentTotalCount + (result?.infoCenterData?.size
                                ?: 0)) < result?.total ?: 0
                        mIsReadNextRequestPage++
                        getResult()
                    }
                }else{
                    getResult()
                }
            }
        }
    }

    fun getResult(){
        mIsGettingData = false
        _isLoading.value = false
    }

    //只取得資料總筆數(Tab顯示要用)
    fun getMsgCount(dataType: DataType) {
        viewModelScope.launch {
            _isLoading.value = true
            val isRead = when (dataType) {
                DataType.UNREAD -> 0
                DataType.READ -> 1
            }
            val result = doNetwork(androidContext) {
                val infoCenterRequest = InfoCenterRequest(1, pageSize, isRead)
                infoCenterRepository.getUserNoticeList(infoCenterRequest)
            }
            when (dataType) {
                DataType.UNREAD -> {
                    _totalUnreadMsgCount.value = result?.total?: 0
                }
                DataType.READ -> {
                    _totalReadMsgCount.value = result?.total?: 0
                }
            }
            _isLoading.value = false
            getResult()
        }
    }

    fun setDataRead(msgId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            doNetwork(androidContext) {
                infoCenterRepository.setMsgRead(msgId)
            }
            _isLoading.value = false
        }
    }

}