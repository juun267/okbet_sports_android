package org.cxct.sportlottery.net

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.asyncApi
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.util.JsonUtil
import timber.log.Timber

class ApiCallSimple: ViewModel() {

    fun testCallApi() {
        // 调用会跟组件生命周期绑定
        callApi({ UserRepository.sendEmailForget("email", "validCodeIdentity", "validCode") }) {
            Timber.d("=====>>> sendEmailCode testCallApi result ${JsonUtil.toJson(it)}")
        }
    }

    fun testCallApiNoCancel() {
        // 不会跟组件生命周期绑定
        callApiWithNoCancel({ UserRepository.sendEmailForget("email", "validCodeIdentity", "validCode") }) {
            Timber.d("=====>>> sendEmailCode testCallApiNoCancel result ${JsonUtil.toJson(it)}")
        }
    }

    //并发的接口调用，方式1
    fun testParallelCallApi1() {

        viewModelScope.launch {

            val deferred1 = asyncApi { UserRepository.sendEmailForget("email", "validCodeIdentity", "validCode") }
            val deferred2 = asyncApi { UserRepository.sendEmailForget("email", "validCodeIdentity", "validCode") }

            val result1 = deferred1.await()
            val result2 = deferred2.await()
            if (result1.succeeded() && result2.succeeded()) {

            }
        }
    }


}

