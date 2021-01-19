package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.network.money.MoneyRechCfgData
import org.cxct.sportlottery.network.money.MoneyRechCfgResult
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.MoneyManager

class MoneyRechViewModel(private val androidContext: Context, private val moneyRepository: MoneyRepository) : BaseViewModel() {


    val rechargeConfigs: LiveData<MoneyRechCfgData>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData>()

    val onlinePayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _onlinePayList
    private var _onlinePayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    val transferPayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _transferPayList
    private var _transferPayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    //獲取充值的基礎配置
    fun getRechCfg() {
        try {
            viewModelScope.launch {
                val result = doNetwork(androidContext) {
                    moneyRepository.getRechCfg("zh")
                }
                _rechargeConfigs.value = result?.rechCfg

                result?.rechCfg?.rechCfgs?.let { filterBankList(it) }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //篩選List要顯示的資料
    private fun filterBankList(rechConfigList: MutableList<MoneyRechCfg.RechConfig>) {
        try {

            _onlinePayList.value = null
            _transferPayList.value = null

            var onlineData: MutableList<MoneyPayWayData>? = mutableListOf()
            var transferData: MutableList<MoneyPayWayData>? = mutableListOf()

            val dataList: MutableList<MoneyPayWayData> = mutableListOf()
            MoneyManager.getMoneyPayWayList()?.forEach { moneyPayWay ->
                if (rechConfigList?.firstOrNull {
                        it.rechType == "onlinePayment" && it.onlineType == moneyPayWay.onlineType
                                || it.rechType != "onlinePayment" && it.rechType == moneyPayWay.rechType
                    } != null) {
                    dataList.add(moneyPayWay)
                }
            }


            dataList.forEach {
                when (it.rechType) {
                    "onlinePayment" -> onlineData?.add(it)
                    else -> transferData?.add(it)
                }
            }

            _onlinePayList.value = onlineData
            _transferPayList.value = transferData

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}