package org.cxct.sportlottery.common.enums

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R

/**
 * 0:未认证 1:已认证 2:待审核 3:验证失败 4:验证等待中 5:待提交复审资料 6:复审中 7:拒绝
 */
enum class VerifiedType(val value: Int,@StringRes val nameResId: Int,@ColorRes val colorResId:Int) {
    NOT_YET(0,R.string.kyc_unverified,R.color.color_A5B2D0),
    PASSED(1,R.string.kyc_passed,R.color.color_1CD219),
    VERIFYING(2,R.string.kyc_unverifing,R.color.color_FFA800),
    VERIFIED_FAILED(3,R.string.kyc_unverified,R.color.color_F23C3B),
    VERIFIED_WAIT(4,R.string.kyc_unverifing,R.color.color_FFA800),
    REVERIFIED_NEED(5, R.string.P211,R.color.color_FFA800),
    REVERIFYING(6,R.string.P196,R.color.color_FFA800),
    REJECT(7,R.string.N417,R.color.color_F23C3B);

    companion object{
        fun getVerifiedType(value: Int?):VerifiedType{
           return when(value){
                NOT_YET.value-> NOT_YET
                PASSED.value-> PASSED
                VERIFYING.value-> VERIFYING
                VERIFIED_FAILED.value-> VERIFIED_FAILED
                VERIFIED_WAIT.value-> VERIFIED_WAIT
                REVERIFIED_NEED.value-> REVERIFIED_NEED
                REVERIFYING.value-> REVERIFYING
                REJECT.value-> REJECT
                else-> NOT_YET
            }
        }
    }
}

