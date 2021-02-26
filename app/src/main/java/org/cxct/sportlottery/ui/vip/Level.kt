package org.cxct.sportlottery.ui.vip

import org.cxct.sportlottery.R

enum class Level(val levelRequirement: LevelRequirement) {
    ONE(LevelRequirement(level = R.string.level_one, levelIcon = R.drawable.ic_card_level_01, levelTitleIcon = R.drawable.ic_vip_level_01)),
    TWO(LevelRequirement(level = R.string.level_two, levelIcon = R.drawable.ic_card_level_02, levelTitleIcon = R.drawable.ic_vip_level_02)),
    THREE(LevelRequirement(level = R.string.level_three, levelIcon = R.drawable.ic_card_level_03, levelTitleIcon = R.drawable.ic_vip_level_03)),
    FOUR(LevelRequirement(level = R.string.level_four, levelIcon = R.drawable.ic_card_level_04, levelTitleIcon = R.drawable.ic_vip_level_04)),
    FIVE(LevelRequirement(level = R.string.level_five, levelIcon = R.drawable.ic_card_level_05, levelTitleIcon = R.drawable.ic_vip_level_05)),
    SIX(LevelRequirement(level = R.string.level_six, levelIcon = R.drawable.ic_card_level_06, levelTitleIcon = R.drawable.ic_vip_level_06))
}