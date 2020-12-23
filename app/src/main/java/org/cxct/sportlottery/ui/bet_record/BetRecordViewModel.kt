package org.cxct.sportlottery.ui.bet_record

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import java.text.SimpleDateFormat
import java.util.*


class BetRecordViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    val betListRequest: LiveData<BetListRequest>
        get() = _betListRequest

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    private val _betListRequest = MutableLiveData<BetListRequest>()
    private val _betRecordResult = MutableLiveData<BetListResult>()


    val statusNameMap =
        mapOf(0 to "未确认", 1 to "未结算", 2 to "全赢", 3 to "赢半", 4 to "全输", 5 to "输半", 6 to "和", 7 to "已取消")

    fun chooseBetStatus() {
        Log.e(">>>", "chooseBetStatus")
    }
/*

    private fun updateStartDate(timeStamp: Long?) {

        _betListRequest.value?.timeRangeParams?.startTime = timeStamp
        _betListRequest.postValue(_betListRequest.value)

        _betListRequest.value = _betListRequest.value?.also {
            it.timeRangeParams?.startTime = timeStamp
        }

        _betListRequest.value =
            BetListRequest(timeRangeParams = TimeRangeParams(startTime = timeStamp))
    }


    private fun updateEndDate(timeStamp: Long?) {
        _betListRequest.value =
            BetListRequest(timeRangeParams = TimeRangeParams(endTime = timeStamp))
    }

    fun getStartDate(): String {
        return timeStampToDate(_betListRequest.value?.timeRangeParams?.startTime) ?: ""
    }
*/

    private fun timeStampToDate(time: Long?): String {
        if (time == null) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(time)
    }

    fun test(): String {
        return "asdf"
    }

    /*

        fun setToday() {
            //get today
            updateDate()
        }
        fun setYesterday() {
            updateDate(1)
        }

        fun setPast30Days() {
            updateDate(18)
        }

        private fun updateDate(minusDays: Int ?= null) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DATE)

            //set format
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val endTimeStamp = formatter.parse("$year-$month-$day 23:59:59")?.time
            var startTimeStamp = formatter.parse("$year-$month-$day 00:00:00")?.time
            if (minusDays != null)
                startTimeStamp = endTimeStamp?.minus((1000 * 60 * 60 * 24 * minusDays))

            //update data
            updateStartDate(startTimeStamp)
            updateEndDate(endTimeStamp)
        }
    */
    fun dateToTimeStamp(date: String, isStartTime: Boolean = true): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeStamp = formatter.parse("$date 00:00:00")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59")?.time

        return if (isStartTime) startTimeStamp else endTimeStamp
    }

    fun searchBetRecordHistory(betListRequest: BetListRequest) {
        viewModelScope.launch {
            getBetList(betListRequest)
        }
    }

    private suspend fun getBetList(betListRequest: BetListRequest) {
        val betListResponse = OneBoSportApi.betService.getBetList(betListRequest)

        if (betListResponse.isSuccessful) {
          _betRecordResult.postValue(betListResponse.body())
        } else {
            val apiError = ErrorUtils.parseError(betListResponse)
            apiError?.let {
                if (it.success != null && it.code != null && it.msg != null) {
                    _betRecordResult.postValue(BetListResult(success = it.success, msg = it.msg, code = it.code, rows = listOf(), total = 0))
                }
            }
        }
    }
}