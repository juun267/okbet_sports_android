package org.cxct.sportlottery.ui.infoCenter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
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

    val userUnReadMsgResult: LiveData<InfoCenterResult>
        get() = _userUnReadMsgResult
    private val _userUnReadMsgResult = MutableLiveData<InfoCenterResult>()

    val userReadMsgResult: LiveData<InfoCenterResult>
        get() = _userReadMsgResult
    private val _userReadMsgResult = MutableLiveData<InfoCenterResult>()

    //未讀總數目
    val totalUnreadMsgCount = InfoCenterRepository.totalUnreadMsgCount

    //已讀總數目
    val totalReadMsgCount = InfoCenterRepository.totalReadMsgCount

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    private var pageSize = 20 //預設每次載入20筆資料

    //Loading
    val onMessageReaded: LiveData<InfoCenterData>
        get() = _onMessageReaded
    private var _onMessageReaded = MutableLiveData<InfoCenterData>()

    fun getUserMsgList(pageNum: Int = 1, dataType: DataType) {
        _isLoading.value = true
        when (dataType) {
            DataType.UNREAD -> {
                viewModelScope.launch {
                    doNetwork(androidContext) {
                        val infoCenterRequest = InfoCenterRequest(pageNum, pageSize, 0)
                        InfoCenterRepository.getUserNoticeList(infoCenterRequest)
                    }?.let {
                        _userUnReadMsgResult.postValue(it)
                    }
                }
            }
            DataType.READ -> {
                viewModelScope.launch {
                    doNetwork(androidContext) {
                        val infoCenterRequest = InfoCenterRequest(pageNum, pageSize, 1)
                        InfoCenterRepository.getUserNoticeList(infoCenterRequest)
                    }?.let {
                        _userReadMsgResult.postValue(it)
                    }
                }
            }
        }
        _isLoading.value = false
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