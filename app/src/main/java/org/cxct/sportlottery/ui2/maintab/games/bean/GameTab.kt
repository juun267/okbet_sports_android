package org.cxct.sportlottery.ui2.maintab.games.bean

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.ALL
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.FAVORITES
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.RECENTLY
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.SEARCH


class GameTab private constructor(@GameTabIds val id: Int,
                                  @DrawableRes val selectedIcon: Int,
                                  @DrawableRes val unSelectedIcon: Int,
                                  @DrawableRes val labelIcon: Int,
                                  @StringRes val name: Int): OKGameTab {
    override fun getKey() = id

    override fun bindNameText(textView: TextView) {
        textView.setText(name)
    }

    override fun bindTabIcon(imageView: ImageView, isSelected: Boolean) {
        imageView.setImageResource(if (isSelected) selectedIcon else unSelectedIcon)
    }

    override fun bindLabelIcon(imageView: ImageView) {
        imageView.setImageResource(labelIcon)
    }

    override fun bindLabelName(textView: TextView) {
        textView.setText(name)
    }

    companion object {
        val TAB_ALL = GameTab(ALL,
            R.drawable.ic_okgame_tab_all_1,
            R.drawable.ic_okgame_tab_all_0,
            R.drawable.ic_okgame_tab_all_0,
            R.string.label_all)

        val TAB_FAVORITES = GameTab(FAVORITES,
            R.drawable.ic_okgame_tab_favorite_1,
            R.drawable.ic_okgame_tab_favorite_0,
            R.drawable.ic_okgame_label_favorite,
            R.string.N873)

        val TAB_RECENTLY = GameTab(RECENTLY,
            R.drawable.ic_okgame_tab_recently_1,
            R.drawable.ic_okgame_tab_recently_0,
            R.drawable.ic_okgame_label_recently,
            R.string.N874)

        val TAB_SEARCH = GameTab(SEARCH,
            R.drawable.ic_okgame_label_search,
            R.drawable.ic_okgame_label_search,
            R.drawable.ic_okgame_label_search,
            R.string.N882)
    }

}





