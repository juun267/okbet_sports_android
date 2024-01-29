package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType

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
     * 是否末位比分玩法，包含小节比分
     */
    fun String?.isEndScoreType(): Boolean {
        return this?.contains(PlayCate.FS_LD_CS.value)==true
    }

    /**
     * 末位比分玩法翻译太长用本地翻译
     */
    fun String?.getEndScorePlatCateName(context: Context): String {
        return when(this){
            PlayCate.FS_LD_CS.value-> context.getString(R.string.P161)
            PlayCate.FS_LD_CS_SEG1.value-> context.getString(R.string.P162)
            PlayCate.FS_LD_CS_SEG2.value-> context.getString(R.string.P163)
            PlayCate.FS_LD_CS_SEG3.value-> context.getString(R.string.P164)
            PlayCate.FS_LD_CS_SEG4.value-> context.getString(R.string.P165)
            else-> this?:""
        }
    }
    fun String?.getEndScoreNameByTab(context: Context): String {
        return when(this){
            PlayCate.FS_LD_CS.value-> context.getString(R.string.J254)
            PlayCate.FS_LD_CS_SEG1.value-> context.getString(R.string.J245)
            PlayCate.FS_LD_CS_SEG2.value-> context.getString(R.string.J246)
            PlayCate.FS_LD_CS_SEG3.value-> context.getString(R.string.J247)
            PlayCate.FS_LD_CS_SEG4.value-> context.getString(R.string.J248)
            else-> this?:""
        }
    }

    /**
     * 板球玩法特殊处理
     */
    fun String.isW_METHOD_1ST(): Boolean {
        return (this.contains(PlayCate.W_METHOD_1ST.value))
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
                list.getOrElse(1) { "0" }
            } ?: "0" //取":"後的數字

            try {
                platNumber.toIntOrNull() ?: 0
            } catch (e: Exception) {
                0
            }
        }?.value
    }

    /**
     * 玩法判斷
     * */
    fun String.isCSType(): Boolean {
        return this.contains(PlayCate.CS.value) && !this.isCombination()
    }

    fun String.isOUType(): Boolean {
        return this.contains(PlayCate.OU.value) && !this.isCombination()
    }

    fun String.isSingleType(): Boolean {
        return this.contains(PlayCate.SINGLE.value) && !this.isCombination()
    }

    fun String.isOEType(): Boolean {
        return (this.contains(PlayCate.OE.value) || this.contains(PlayCate.Q_OE.value)) && !this.isCombination()
    }

    fun String.isBTSType(): Boolean {
        return this.contains(PlayCate.BTS.value) && !this.isCombination()
    }

    /**
     * 後端回傳文字需保留完整文字, 文字顯示縮減由前端自行處理
     */
    fun String.abridgeOddsName(): String {
        return this.replace("Over", "O").replace("Under", "U")
    }

    /**
     * 足球：下個進球玩法會使用到
     */
    fun getOrdinalNumbers(number: String): String {
        return when (number) {
            "1" -> "1st"
            "2" -> "2nd"
            "3" -> "3rd"
            else -> "${number}th"
        }
    }

}