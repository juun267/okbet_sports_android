package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class MoneyRechViewModel(private val moneyRepository: MoneyRepository) : BaseViewModel() {

    @SuppressLint("LogNotTimber")
    fun getRechCfg() {
        try {
            viewModelScope.launch {
                val result = doNetwork {
                    moneyRepository.getRechCfg("zh")
                }
                Log.v(
                    "Bill", Gson().toJson(result)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}