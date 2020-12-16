package org.cxct.sportlottery.ui.menu.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cxct.sportlottery.repository.LoginRepository

class SettlementViewModel (private val loginRepository: LoginRepository): ViewModel() {
    val settlementFilter: LiveData<SettlementFilter>
    get() = _settlementFilter

    private var _settlementFilter = MutableLiveData<SettlementFilter>()

    //test
    val settlementData: LiveData<List<SettlementItem>>
    get() = _settlementData
    private var _settlementData = MutableLiveData<List<SettlementItem>>(listOf(SettlementItem("FT",false), SettlementItem("BM",false), SettlementItem("KK",false), SettlementItem("DD",true),SettlementItem("KK",false),SettlementItem("KK",false),SettlementItem("KK",false),SettlementItem("KK",false),SettlementItem("KK",false),SettlementItem("KK",false))) //TODO Dean : 串接api資料
}