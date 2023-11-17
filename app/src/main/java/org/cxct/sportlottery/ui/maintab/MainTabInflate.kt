package org.cxct.sportlottery.ui.maintab

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.opensource.svgaplayer.SVGAImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
import org.cxct.sportlottery.util.getMarketSwitch

class MainTabInflate(private val parent: LinearLayout, val onClick: (Int) -> Boolean) {

    private var currentItem: LinearLayout? = null
    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val selColor = parent.context.getColor(R.color.color_025BE8)
    private val unSelColor = parent.context.getColor(R.color.color_6C7BA8)

    init {
        val lp = LinearLayout.LayoutParams(0, -1, 1f)
        val lpIcon = 32.dp.let { LinearLayout.LayoutParams(it, it) }
        addItem(lp, lpIcon, "svga/home_tab_menu.svga", R.drawable.ic_tab_menu_nor, R.string.menu)
        addItem(lp, lpIcon,"svga/home_tab_sports.svga", R.drawable.ic_tab_sport_nor, R.string.main_tab_sport)
        if (!getMarketSwitch()) {
            addItem(lp, lpIcon, "svga/home_tab_game.svga", R.drawable.ic_tab_game_nor, R.string.news_tab_game)
        }
        addItem(lp, lpIcon, "svga/home_tab_chat.svga", R.drawable.ic_tab_chat_nor, R.string.N984)
        addItem(lp, lpIcon,"svga/home_tab_mine.svga", R.drawable.ic_tab_user_nor, R.string.main_tab_mine)
    }

    private fun addItem(lpItem: LinearLayout.LayoutParams,
                        lpIcon: LinearLayout.LayoutParams,
                        icon: String,
                        @DrawableRes norDrawableRes: Int,
                        @StringRes name: Int) {

        val item = LinearLayout(parent.context)
        item.orientation= LinearLayout.VERTICAL
        item.gravity = Gravity.CENTER

        val imageView = SVGAImageView(parent.context)
        imageView.loops = 1
        imageView.clearsAfterStop = false
        imageView.id = iconId
        imageView.tag = Pair(norDrawableRes,icon)
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
    }

    private fun setSelected(item: LinearLayout) {
        setChangeSelected(item, true)
        currentItem?.let { setChangeSelected(it, false) }
        currentItem = item
    }

    private fun setChangeSelected(item: LinearLayout, isSelected: Boolean) {
        item.findViewById<SVGAImageView>(iconId).apply {
            if (isSelected){
                setAssetSvgIcon((tag as Pair<Int,String>).second,true)
            }else{
                setImageResource((tag as Pair<Int,String>).first)
            }
        }
        item.findViewById<TextView>(nameId).setTextColor(if (isSelected) selColor else unSelColor)
        item.isSelected = isSelected
    }

    fun clearSelected() {
        currentItem?.let { setChangeSelected(it, false) }
        currentItem = null
    }
    fun selected(position:Int) {
        currentItem?.let { setChangeSelected(it, false) }
        currentItem = parent.getChildAt(position) as LinearLayout
        currentItem?.let { setChangeSelected(it, true) }
    }

}