package org.cxct.sportlottery.ui.finance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.list.RechargeListRequest
import org.cxct.sportlottery.network.money.list.RechargeListResult
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.TimeUtil

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult

    val userRechargeListResult: LiveData<RechargeListResult?>
        get() = _userRechargeResult

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    val recordType: LiveData<String>
        get() = _recordType

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _userRechargeResult = MutableLiveData<RechargeListResult?>()
    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()
    private val _recordType = MutableLiveData<String>()


    fun setRecordType(recordType: String) {
        _recordType.postValue(recordType)
    }

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

    fun getUserRechargeList() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.moneyService.getUserRechargeList(
                    RechargeListRequest()
                )
            }

            result?.rows?.map {
                it.rechState = when (it.status) {
                    RechargeState.SUCCESS.code -> androidContext.getString(R.string.recharge_state_success)
                    RechargeState.FAILED.code -> androidContext.getString(R.string.recharge_state_failed)
                    RechargeState.PROCESSING.code -> androidContext.getString(R.string.recharge_state_processing)
                    RechargeState.RECHARGING.code -> androidContext.getString(R.string.recharge_state_recharging)
                    else -> ""
                }

                it.rechDateStr = TimeUtil.timeFormat(it.rechTime, "yyyy-MM-dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.rechTime, "HH:mm:ss")
            }

            _userRechargeResult.postValue(result)
        }
    }
}