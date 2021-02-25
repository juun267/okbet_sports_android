package org.cxct.sportlottery.ui.vip

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class LevelRequirement(
    @StringRes val level: Int,
    @StringRes val levelName: Int,
    @DrawableRes val levelIcon: Int,
    var growthRequirement: String? = null
)