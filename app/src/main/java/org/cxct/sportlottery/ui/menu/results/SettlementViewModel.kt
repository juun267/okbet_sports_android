package org.cxct.sportlottery.ui.menu.results

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cxct.sportlottery.ui.login.LoginViewModel

class SettlementViewModel (private val application: Application): ViewModel() {
    val settlementFilter: LiveData<SettlementFilter>
    get() = _settlementFilter

    private var _settlementFilter = MutableLiveData<SettlementFilter>()

    //test
    val settlementData: LiveData<List<SettlementItem>>
    get() = _settlementData
    private var _settlementData = MutableLiveData<List<SettlementItem>>(listOf(SettlementItem("FT"), SettlementItem("BM"))) //TODO Dean : 串接api資料
    //

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettlementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettlementViewModel(application) as T
            }
            throw IllegalAccessException("Unable to construct view model")
        }
    }
}