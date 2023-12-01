package org.cxct.sportlottery.ui.maintab.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class HomeMenuBean(@DrawableRes val iconRes: Int, @StringRes var title: Int) {
    HOME(R.drawable.selector_home_menu_home, R.string.bottom_nav_home),
    HOT(R.drawable.selector_home_menu_hot, R.string.home_recommend),
    SPORT(R.drawable.selector_home_menu_sport, R.string.main_tab_sport),
    CASINO(R.drawable.selector_home_menu_casino, R.string.P230),
    LIVE(R.drawable.selector_home_menu_live, R.string.home_live),
    ESPORT(R.drawable.selector_home_menu_esport, R.string.esports),
    PROMOTION(R.drawable.selector_home_menu_promotion, R.string.promotion),
    SERVICE(R.drawable.selector_home_menu_service, R.string.btn_service),
}