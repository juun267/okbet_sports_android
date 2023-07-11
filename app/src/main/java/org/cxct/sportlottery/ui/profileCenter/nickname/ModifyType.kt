package org.cxct.sportlottery.ui.profileCenter.nickname

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef(
    *[ModifyType.RealName, ModifyType.QQNumber, ModifyType.Email, ModifyType.WeChat,
        ModifyType.PhoneNumber, ModifyType.NickName, ModifyType.PlaceOfBirth, ModifyType.Address,ModifyType.AddressP,
        ModifyType.ZipCode,ModifyType.ZipCodeP]
)
annotation class ModifyType {
    companion object {
        const val RealName = 100
        const val QQNumber = 200
        const val Email = 300
        const val WeChat = 400
        const val PhoneNumber = 500
        const val NickName = 600
        const val PlaceOfBirth = 700
        const val Address = 800
        const val ZipCode = 900
        const val AddressP = 1000
        const val ZipCodeP = 1100
    }
}


