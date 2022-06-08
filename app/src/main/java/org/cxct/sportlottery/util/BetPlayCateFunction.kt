package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate

object BetPlayCateFunction {

    /**
     * 取得投注玩法名稱
     * @since 在原畫面呈現時，playCode被特殊處理後，無法直接與BetPlayCateNameMap配對
     */
    fun MutableMap<String?, Map<String?, String?>?>?.getNameMap(
        gameType: String,
        playCode: String
    ): Map<String?, String?>? {
        return when {
            gameType == GameType.TN.key && playCode.isNumPlatType() -> {
                getNumPlatBetPlayCateName()
            }
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
     * 判斷是否為网球的特定第几局的玩法(1X2_SEG?_GAMES:#), 其中 ?=1~5, #=1~6
     */
    private fun String.isNumPlatType(): Boolean {
        return (this.contains("1X2_SEG") && this.contains("_GAMES"))
    }

    /**
     * 取得 下个得分, 下个得分 加时赛 的投注玩法名稱
     */
    private fun MutableMap<String?, Map<String?, String?>?>?.getNGOALBetPlayCateName(): Map<String?, String?>? {
        val nGOALMap = this?.filter { map ->
            map.key?.isNOGALType() ?: false
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

    /**
     * 取得 网球的特定第几局的玩法 的投注玩法名稱
     */
    private fun MutableMap<String?, Map<String?, String?>?>?.getNumPlatBetPlayCateName(): Map<String?, String?>? {
        val numPlatMap = this?.filter { map ->
            map.key?.isNumPlatType() ?: false
        }

        return numPlatMap?.minByOrNull { map ->
            val platNumber: String = map.key?.split(":")?.let { list ->
                if (list.isNotEmpty()) {
                    list[1]
                } else {
                    "0"
                }
            } ?: "0" //取":"後的數字

            try {
                platNumber.toIntOrNull() ?: 0
            } catch (e: Exception) {
                0
            }
        }?.value
    }
}