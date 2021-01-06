package org.cxct.sportlottery.ui.infoCenter

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.InfoCenter.InfoCenterData
import org.cxct.sportlottery.network.InfoCenter.InfoCenterResult
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class InfoCenterViewModel(private val infoCenterRepository: InfoCenterRepository) :
    BaseViewModel() {

    val userMsgList: LiveData<MutableList<InfoCenterData>>
        get() = _userMsgList
    private var _userMsgList = MutableLiveData<MutableList<InfoCenterData>>()

    private var mNextRequestPage = 1
    private var pageSize = 20 //預設每次載入20筆資料
    private var mIsGettingData = false //判斷請求任務是否進行中
    private var mNeedMoreLoading = false //判斷滑到底是否需要繼續加載

    fun getUserMsgList(isReload: Boolean, currentTotalCount: Int) {
        try {
            if (mIsGettingData)
                return
            mIsGettingData = true

            var mCurrentTotalCount = currentTotalCount

            if (isReload) {//重新載入
                mNextRequestPage = 1
                _userMsgList.value = mutableListOf()
                mCurrentTotalCount = 0
                mNeedMoreLoading = true
            }

            if (mNeedMoreLoading) {
                viewModelScope.launch {
                    val result = doNetwork {
                        infoCenterRepository.getUserNoticeList(mNextRequestPage, pageSize)
                    }
                    _userMsgList.value = result?.infoCenterData

                    //判斷是不是可以再加載
                    mNeedMoreLoading =
                        (mCurrentTotalCount + result?.infoCenterData!!.size) < result.total ?: 0

                    mNextRequestPage++

                }
            }
            mIsGettingData = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}