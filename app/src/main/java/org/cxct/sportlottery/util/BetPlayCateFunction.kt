package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.common.PlayCate

object BetPlayCateFunction {

    /**
     * 取得投注玩法名稱
     * @since 在原畫面呈現時，playCode被特殊處理後，無法直接與BetPlayCateNameMap配對
     */
    fun MutableMap<String?, Map<String?, String?>?>?.getNameMap(playCode: String): Map<String?, String?>? {
        return when {
            playCode.isNOGALType() -> {
                getNGOALBetPlayCateName()
            }
            else -> this?.get(playCode)
        }
    }

    fun String.isCombination(): Boolean {
        return this.contains(PlayCate.SINGLE_OU.value) || this.contains(PlayCate.SINGLE_BTS.value)
    }

    fun String.isNOGALType(): Boolean {
        return (this.contains(PlayCate.NGOAL.value) || this.contains(PlayCate.NGOAL_OT.value)) && !this.isCombination()
    }

    /**
     * 取得 下个得分 的投注玩法名稱
     */
    private fun MutableMap<String?, Map<String?, String?>?>?.getNGOALBetPlayCateName(): Map<String?, String?>? {
        val nGOALMap = this?.filter { map ->
            map.key?.contains(PlayCate.NGOAL.value) ?: false
        }

        return nGOALMap?.maxByOrNull { map ->
            val goalNumber: String = map.key?.split(":")?.let { list ->
                if (list.isNotEmpty()) {
                    list[1]
                } else {
                    "0"
                }
            } ?: "0" //取":"後的數字
            try {
                goalNumber.toIntOrNull() ?: 0
            } catch (e: Exception) {
                0
            }
        }?.value
    }
}