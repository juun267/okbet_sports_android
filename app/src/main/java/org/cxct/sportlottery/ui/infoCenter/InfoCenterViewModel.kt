package org.cxct.sportlottery.ui.infoCenter

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

    val setMsgReadResult: LiveData<InfoCenterResult>
        get() = _setMsgReadResult
    private var _setMsgReadResult = MutableLiveData<InfoCenterResult>()

    fun getUserMsgList(page: Int, pageSize: Int) {
        viewModelScope.launch {
            try {
                val result = doNetwork {
                    infoCenterRepository.getUserNoticeList(page, pageSize)
                }
                _userMsgList.value = result.infoCenterData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setMsgRead(msgId: String) {
        try {
            viewModelScope.launch {
                val result = doNetwork {
                    infoCenterRepository.setMsgReaded(msgId)
                }
                _setMsgReadResult.value = result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}