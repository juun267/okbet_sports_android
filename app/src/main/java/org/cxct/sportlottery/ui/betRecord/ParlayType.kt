package org.cxct.sportlottery.ui.betRecord

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class ParlayType(
    val key: String,
    @StringRes val stringRes: Int? = null,
    @StringRes val ruleStringRes: Int? = null
) {
    SINGLE("1C1", R.string.bet_list_single, R.string.bet_list_single_warn),
    FOLD_2("2C1", R.string.parlay_2_fold, R.string.parlay_2_fold_rule),
    FOLD_3("3C1", R.string.parlay_3_fold, R.string.parlay_3_fold_rule),
    FOLD_4("4C1", R.string.parlay_4_fold, R.string.parlay_4_fold_rule),
    FOLD_5("5C1", R.string.parlay_5_fold, R.string.parlay_5_fold_rule),
    FOLD_6("6C1", R.string.parlay_6_fold, R.string.parlay_6_fold_rule),
    FOLD_7("7C1", R.string.parlay_7_fold, R.string.parlay_7_fold_rule),
    FOLD_8("8C1", R.string.parlay_8_fold, R.string.parlay_8_fold_rule),
    FOLD_9("9C1", R.string.parlay_9_fold, R.string.parlay_9_fold_rule),
    FOLD_10("10C1", R.string.parlay_10_fold, R.string.parlay_10_fold_rule),
    BLOCK_3("3C4", R.string.parlay_3_block, R.string.parlay_3_block_rule),
    BLOCK_4("4C11", R.string.parlay_4_block, R.string.parlay_4_block_rule),
    BLOCK_5("5C26", R.string.parlay_5_block, R.string.parlay_5_block_rule),
    BLOCK_6("6C57", R.string.parlay_6_block, R.string.parlay_6_block_rule),
    BLOCK_7("7C120", R.string.parlay_7_block, R.string.parlay_7_block_rule),
    BLOCK_8("8C247", R.string.parlay_8_block, R.string.parlay_8_block_rule),
    BLOCK_9("9C502", R.string.parlay_9_block, R.string.parlay_9_block_rule),
    BLOCK_10("10C1013", R.string.parlay_10_block, R.string.parlay_10_block_rule),
    OUTRIGHT("OUTRIGHT"),
    OTHER("");

    companion object {
        fun getParlayStringRes(parlayType: String): Int? {
            return ParlayType.values().find { it.key == parlayType }?.stringRes
        }
    }
}

