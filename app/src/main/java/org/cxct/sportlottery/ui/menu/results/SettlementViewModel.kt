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
    private var _settlementData = MutableLiveData<List<SettlementItem>>(
        listOf(
            SettlementItem("FT", false),
            SettlementItem("BK", false),
            SettlementItem("TN", false),
            SettlementItem("BM", true),
            SettlementItem("VB", false)
        )
    ) //TODO Dean : 串接api資料

    fun setGameTypeFilter(gameType: String) {
        //TODO Dean : 篩選後更新_settlementData
    }
}