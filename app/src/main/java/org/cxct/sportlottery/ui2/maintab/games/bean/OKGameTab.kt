package org.cxct.sportlottery.ui2.maintab.games.bean

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntDef
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.ALL
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.FAVORITES
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.RECENTLY
import org.cxct.sportlottery.ui2.maintab.games.bean.GameTabIds.Companion.SEARCH
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

interface OKGameTab: OKGameLabel {

    fun bindNameText(textView: TextView)
    fun bindTabIcon(imageView: ImageView, isSelected: Boolean)

    fun isAll() = getKey() == ALL

    fun isRecent() = getKey() == RECENTLY

    fun isFavorites() = getKey() == FAVORITES

}

interface OKGameLabel {

    fun getKey(): Int

    fun bindLabelIcon(imageView: ImageView)

    fun bindLabelName(textView: TextView)
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef(*[ALL, FAVORITES, RECENTLY, SEARCH])
annotation class GameTabIds {

    companion object {
        const val ALL = -10
        const val FAVORITES = -20
        const val RECENTLY = -30
        const val SEARCH = -40
    }

}

