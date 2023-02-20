package org.cxct.sportlottery.net

import androidx.lifecycle.ViewModel
import org.cxct.sportlottery.extentions.callApi
import org.cxct.sportlottery.extentions.callApiWithNoCancel
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
}

