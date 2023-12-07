package org.cxct.sportlottery.ui.maintab

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import com.opensource.svgaplayer.SVGAImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.getMarketSwitch

class MainTabInflate2(private val parent: LinearLayout, val onClick: (Int) -> Boolean) {

    private var currentItem: LinearLayout? = null
    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val selColor = parent.context.getColor(R.color.color_025BE8)
    private val unSelColor = parent.context.getColor(R.color.color_6C7BA8)
    private val INDEX_SPORT = 1
    private val INDEX_GAMES = 2

    init {
        val lp = LinearLayout.LayoutParams(0, -1, 1f)
        val lpIcon = 24.dp.let { LinearLayout.LayoutParams(it, it) }
        addItem(lp, lpIcon, R.drawable.ic_tab_menu_nor_chris, R.drawable.ic_tab_menu_nor_chris, R.string.menu)
        addItem(lp, lpIcon,R.drawable.ic_tab_sport_nor_chris, R.drawable.ic_tab_sport_sle_chris, R.string.main_tab_sport)
        addItem(lp, lpIcon, R.drawable.ic_tab_game_nor_chris, R.drawable.ic_tab_game_sle_chris, R.string.news_tab_game).isGone = getMarketSwitch()
        addItem(lp, lpIcon, R.drawable.ic_tab_chat_nor_chris, R.drawable.ic_tab_chat_sle_chris, R.string.N984)
        addItem(lp, lpIcon,R.drawable.ic_tab_user_nor_chris, R.drawable.ic_tab_user_sle_chris, R.string.main_tab_mine)
    }

    private fun addItem(lpItem: LinearLayout.LayoutParams,
                        lpIcon: LinearLayout.LayoutParams,
                        @DrawableRes norDrawableRes: Int,
                        @DrawableRes selctedDrawableRes: Int,
                        @StringRes name: Int): LinearLayout {

        val item = LinearLayout(parent.context)
        item.orientation= LinearLayout.VERTICAL
        item.gravity = Gravity.CENTER

        val imageView = SVGAImageView(parent.context)
        imageView.loops = 1
        imageView.clearsAfterStop = false
        imageView.id = iconId
        imageView.tag = Pair(norDrawableRes, selctedDrawableRes)
//        imageView.setAssetSvgIcon(icon)
        imageView.setImageResource(norDrawableRes)
        item.addView(imageView, lpIcon)

        val textView = TextView(parent.context)
        textView.id = nameId
        textView.setTextColor(unSelColor)
        textView.maxLines = 1
        textView.typeface = AppFont.helvetica
        textView.textSize = 10f
        textView.gravity = Gravity.CENTER_HORIZONTAL
        textView.setText(name)
        item.addView(textView)

        parent.addView(item, lpItem)
        item.setOnClickListener {
            if (currentItem != item && onClick.invoke(name)) {
                setSelected(item)
            }
        }

        return item
    }

    private fun setSelected(item: LinearLayout) {
        setChangeSelected(item, true)
        currentItem?.let { setChangeSelected(it, false) }
        currentItem = item
    }

    private fun setChangeSelected(item: LinearLayout, isSelected: Boolean) {
        item.findViewById<SVGAImageView>(iconId).apply {
            setImageResource((tag as Pair<Int,Int>).second)
            if (isSelected){
                setImageResource((tag as Pair<Int,Int>).second)
            }else{
                setImageResource((tag as Pair<Int,Int>).first)
            }
        }
        item.findViewById<TextView>(nameId).apply {
            if (isSelected) {
                typeface = AppFont.inter_bold
                setTextColor(selColor)
            } else {
                typeface = AppFont.helvetica
                setTextColor(unSelColor)
            }
        }
        item.isSelected = isSelected
    }

    private fun changeSelected(tabIndex: Int) {
        if (currentItem != null && parent.indexOfChild(currentItem) == tabIndex) {
            return
        }
        clearSelected()
        setSelected(parent.getChildAt(tabIndex) as LinearLayout)
    }

    fun clearSelected() {
        currentItem?.let { setChangeSelected(it, false) }
        currentItem = null
    }

    fun selectedSport() {
        changeSelected(INDEX_SPORT)
    }

    fun selectedGames() {
        changeSelected(INDEX_GAMES)
        if (currentItem?.isGone == true) {
            currentItem!!.visible()
        }
    }

    fun selectedProfile() {
        changeSelected(parent.childCount - 1)
    }



}