package org.cxct.sportlottery.ui.profileCenter.nickname

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.TYPE)
@Retention(RetentionPolicy.SOURCE)
@IntDef(*[ModifyType.RealName, ModifyType.QQNumber, ModifyType.Email, ModifyType.WeChat, ModifyType.PhoneNumber, ModifyType.NickName])
annotation class ModifyType {
    companion object {
        const val RealName = 100
        const val QQNumber = 200
        const val Email = 300
        const val WeChat = 400
        const val PhoneNumber = 500
        const val NickName = 600
    }
}


