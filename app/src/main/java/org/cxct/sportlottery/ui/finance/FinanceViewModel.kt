package org.cxct.sportlottery.ui.finance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoneyResult.postValue(userMoneyResult)
        }
    }

    fun getRecordList() {
        val recordStrList = androidContext.resources.getStringArray(R.array.finance_array)
        val recordImgList = androidContext.resources.obtainTypedArray(R.array.finance_img_array)

        val recordList = recordStrList.map {
            it to recordImgList.getResourceId(recordStrList.indexOf(it), -1)
        }
        recordImgList.recycle()

        _recordList.postValue(recordList)
    }
}