package org.cxct.sportlottery.ui.infoCenter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class InfoCenterViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    enum class DataType { UNREAD, READ }//未讀,已讀

    //未讀資料
    val userUnreadMsgList = InfoCenterRepository.unreadList

    //已讀資料
    val userReadMsgList = InfoCenterRepository.readedList

    //未讀總數目
    val totalUnreadMsgCount = InfoCenterRepository.totalUnreadMsgCount


    //已讀總數目
    val totalReadMsgCount = InfoCenterRepository.totalReadMsgCount

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

    //Loading
    val onMessageReaded: LiveData<InfoCenterData>
        get() = _onMessageReaded
    private var _onMessageReaded = MutableLiveData<InfoCenterData>()

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
                    mCurrentTotalCount = 0
                    mNeedMoreLoadingUnRead = true
                }
                DataType.READ -> {
                    mIsReadNextRequestPage = 1
                    mCurrentTotalCount = 0
                    mNeedMoreLoadingRead = true
                }
            }
            mIsReadNextRequestPage = 1
            InfoCenterRepository.clearList()
        }
        when (dataType) {
            DataType.UNREAD -> {
                if (mNeedMoreLoadingUnRead) {
                    viewModelScope.launch {
                        val result = doNetwork(androidContext) {
                            val infoCenterRequest =
                                InfoCenterRequest(mUnReadNextRequestPage, pageSize, 0)
                            InfoCenterRepository.getUserNoticeList(infoCenterRequest)
                        }
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
                            InfoCenterRepository.getUserNoticeList(infoCenterRequest)
                        }
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
    fun getMsgCount(dataType: MsgType) {
        viewModelScope.launch {
            try {
                InfoCenterRepository.getMsgCount(dataType.code)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDataRead(bean: InfoCenterData) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = doNetwork(androidContext) {
                InfoCenterRepository.setMsgRead(bean.id.toString())
                //原本的邏輯是"未讀的資料打開要變成已讀" 並且更新Tab，2022/06/16要求修改，先註解保留原本邏輯
//                val infoCenterRequest =
//                    InfoCenterRequest(mUnReadNextRequestPage, pageSize, 0)
//                InfoCenterRepository.getUserNoticeList(infoCenterRequest)
            }

            if (response?.success == true) {
                _onMessageReaded.postValue(bean)
            }
//            InfoCenterRepository.getMsgCount(MsgType.NOTICE_READED.code)
            _isLoading.value = false
        }
    }

}