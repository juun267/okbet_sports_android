package org.cxct.sportlottery.ui.maintab.games.bean

import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.maintab.games.bean.GameTabIds.Companion.ALL
import org.cxct.sportlottery.ui.maintab.games.bean.GameTabIds.Companion.FAVORITES
import org.cxct.sportlottery.ui.maintab.games.bean.GameTabIds.Companion.HOTGAMES
import org.cxct.sportlottery.ui.maintab.games.bean.GameTabIds.Companion.SEARCH
import org.cxct.sportlottery.ui.maintab.games.bean.GameTabIds.Companion.SLOTSGAMES
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class GameTab private constructor(@GameTabIds val id: Int, @DrawableRes val icon: Int, @StringRes val name: Int) {

    companion object {

        fun getGameTabs(): Array<GameTab> {
            return arrayOf(GameTab(ALL, R.drawable.selector_tab_home, R.string.bottom_nav_home),
                GameTab(FAVORITES, R.drawable.selector_tab_sport, R.string.main_tab_sport),
                GameTab(HOTGAMES, R.drawable.selector_tab_betlist, R.string.main_tab_betlist),
                GameTab(SLOTSGAMES, R.drawable.selector_tab_fav, R.string.main_tab_favorite),
                GameTab(SEARCH, R.drawable.selector_tab_user, R.string.main_tab_mine),
            )
        }
    }

    fun isAll() = id == ALL
    fun isSearch() = id == SEARCH
}



@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef(*[ALL, FAVORITES, HOTGAMES, SLOTSGAMES, SEARCH])
annotation class GameTabIds {

    companion object {
        const val ALL = 10
        const val FAVORITES = 20
        const val HOTGAMES = 30
        const val SLOTSGAMES = 40
        const val SEARCH = 50
    }

}