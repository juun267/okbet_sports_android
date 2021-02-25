package org.cxct.sportlottery.ui.vip

import org.cxct.sportlottery.R

enum class Level(val levelRequirement: LevelRequirement) {
    ONE(LevelRequirement(R.string.level_one, R.string.member_name_level_one, R.drawable.ic_card_level_01, null)),
    TWO(LevelRequirement(R.string.level_two, R.string.member_name_level_one, R.drawable.ic_card_level_02, null)),
    THREE(LevelRequirement(R.string.level_three, R.string.member_name_level_one, R.drawable.ic_card_level_03, null)),
    FOUR(LevelRequirement(R.string.level_four, R.string.member_name_level_one, R.drawable.ic_card_level_04, null)),
    FIVE(LevelRequirement(R.string.level_five, R.string.member_name_level_one, R.drawable.ic_card_level_05, null)),
    SIX(LevelRequirement(R.string.level_six, R.string.member_name_level_one, R.drawable.ic_card_level_06, null)),
}