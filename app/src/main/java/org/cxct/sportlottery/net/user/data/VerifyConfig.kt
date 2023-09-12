package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class VerifyConfig(val requiredSelfiePicture: Int, val requiredWealthProof: Int)