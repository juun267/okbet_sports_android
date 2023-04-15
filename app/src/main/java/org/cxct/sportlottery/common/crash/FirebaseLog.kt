package org.cxct.sportlottery.common.crash

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase


object FirebaseLog {

    // 添加自定义日志信息与firebase崩溃信息关联
    fun addLogInfo(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
}