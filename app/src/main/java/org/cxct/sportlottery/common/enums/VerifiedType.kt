package org.cxct.sportlottery.common.enums

enum class VerifiedType(val value: Int) {
    NOT_YET(0),
    PASSED(1),
    VERIFYING(2),
    VERIFIED_FAILED(3),
    VERIFIED_WAIT(4),
    REVERIFIED_NEED(5),
    REVERIFYING(6),
    REJECT(7)
}
