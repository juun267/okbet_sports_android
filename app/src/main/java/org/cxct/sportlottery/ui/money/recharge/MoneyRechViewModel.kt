package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.*
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.bet.list.BetInfoListDialog
import org.cxct.sportlottery.util.MoneyManager

class MoneyRechViewModel(
    private val androidContext: Context,
    private val moneyRepository: MoneyRepository
) : BaseViewModel() {


    val rechargeConfigs: LiveData<MoneyRechCfgData>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData>()

    val onlinePayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _onlinePayList
    private var _onlinePayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    val transferPayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _transferPayList
    private var _transferPayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    val apiResult: LiveData<MoneyAddResult>
        get() = _apiResult
    private var _apiResult = MutableLiveData<MoneyAddResult>()

    //獲取充值的基礎配置
    fun getRechCfg() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                moneyRepository.getRechCfg()
            }
            _rechargeConfigs.value = result?.rechCfg

            result?.rechCfg?.rechCfgs?.let { filterBankList(it) }
        }
    }

    //篩選List要顯示的資料
    private fun filterBankList(rechConfigList: List<MoneyRechCfg.RechConfig>) {
        try {

            val onlineData: MutableList<MoneyPayWayData> = mutableListOf()
            val transferData: MutableList<MoneyPayWayData> = mutableListOf()

            val dataList: MutableList<MoneyPayWayData> = mutableListOf()
            MoneyManager.getMoneyPayWayList()?.forEach { moneyPayWay ->
                if (rechConfigList.firstOrNull {
                        it.rechType == "onlinePayment" && it.onlineType == moneyPayWay.onlineType
                                || it.rechType != "onlinePayment" && it.rechType == moneyPayWay.rechType
                    } != null) {
                    dataList.add(moneyPayWay)
                }
            }


            dataList.forEach {
                when (it.rechType) {
                    "onlinePayment" -> onlineData.add(it)
                    else -> transferData.add(it)
                }
            }

            _onlinePayList.value = onlineData
            _transferPayList.value = transferData

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //轉帳支付充值
    fun rechargeAdd(moneyAddRequest: MoneyAddRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                moneyRepository.rechargeAdd(moneyAddRequest)
            }.let {
                it?.result = moneyAddRequest.depositMoney.toString()//金額帶入result
                _apiResult.value = it
            }
        }
    }

    //在線支付
    fun rechargeOnlinePay(moneyAddRequest: MoneyAddRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                moneyRepository.rechargeOnlinePay(moneyAddRequest)
            }.let {
                doNetwork(androidContext) {
                    moneyRepository.rechargeAdd(moneyAddRequest)
                }.let {
                    it?.result = moneyAddRequest.depositMoney.toString()//金額帶入result
                    _apiResult.value = it
                }
            }
        }
    }
}
