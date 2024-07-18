package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class VerifyConfig(private val requiredSelfiePicture: Int,
                        private val requiredWealthProof: Int,
                        private val requiredBackOfID: Int) {

    fun idbackRequired() = 1 == requiredBackOfID
    fun selfiePictureRequired() = 1 == requiredSelfiePicture
    fun wealthProofRequired() = 1 == requiredWealthProof
}