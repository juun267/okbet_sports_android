package org.cxct.sportlottery.ui.transactionStatus

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class ParlayType(val key: String, @StringRes val stringRes: Int? = null) {
    SINGLE("1C1"),
    FOLD_2("2C1", R.string.parlay_2_fold),
    FOLD_3("3C1", R.string.parlay_3_fold),
    FOLD_4("4C1", R.string.parlay_4_fold),
    FOLD_5("5C1", R.string.parlay_5_fold),
    FOLD_6("6C1", R.string.parlay_6_fold),
    FOLD_7("7C1", R.string.parlay_7_fold),
    FOLD_8("8C1", R.string.parlay_8_fold),
    FOLD_9("9C1", R.string.parlay_9_fold),
    FOLD_10("10C1", R.string.parlay_10_fold),
    BLOCK_3("3C4", R.string.parlay_3_block),
    BLOCK_4("4C11", R.string.parlay_4_block),
    BLOCK_5("5C26", R.string.parlay_5_block),
    BLOCK_6("6C57", R.string.parlay_6_block),
    BLOCK_7("7C120", R.string.parlay_7_block),
    BLOCK_8("8C247", R.string.parlay_8_block),
    BLOCK_9("9C502", R.string.parlay_9_block),
    BLOCK_10("10C1013", R.string.parlay_10_block),
    OUTRIGHT("OUTRIGHT"),
    OTHER("");

    companion object{
        fun getParlayStringRes(parlayType: String): Int?{
            return ParlayType.values().find { it.key == parlayType }?.stringRes
        }
    }
}

