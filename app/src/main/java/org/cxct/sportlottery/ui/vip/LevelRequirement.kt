package org.cxct.sportlottery.ui.vip

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class LevelRequirement(
    var levelId: Int? = null, //修正為0~5 跟IOS對過，IOS沒有用config.id當作levelId
    @StringRes
    val level: Int,
    var levelName: String? = null,
    @DrawableRes
    val levelIcon: Int,
    @DrawableRes
    val levelTitleIcon: Int,
    var growthRequirement: Int? = null
)