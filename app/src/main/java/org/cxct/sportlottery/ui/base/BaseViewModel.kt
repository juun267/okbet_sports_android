package org.cxct.sportlottery.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cxct.sportlottery.network.common.BaseResult


abstract class BaseViewModel : ViewModel() {
    val baseResult: LiveData<BaseResult>
        get() = mBaseResult

    protected val mBaseResult = MutableLiveData<BaseResult>()
}