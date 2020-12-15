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