package org.cxct.sportlottery.ui.maintab

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import com.opensource.svgaplayer.SVGAImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
import org.cxct.sportlottery.util.getMarketSwitch
import org.cxct.sportlottery.util.isOpenChatRoom

class MainTabInflate(lifecycleOwner: LifecycleOwner,
                     private val parent: LinearLayout, val onClick: (Int) -> Boolean) {

    private var currentItem: LinearLayout? = null
    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val selColor = parent.context.getColor(R.color.color_025BE8)
    private val unSelColor = parent.context.getColor(R.color.color_6C7BA8)
    private val INDEX_HOME = 0
    private val INDEX_SPORT = 1
    private val INDEX_GAMES = 2
    private val INDEX_CHAT = 3
    private var chatRoomEnable = true

    init {

        chatRoomEnable = isOpenChatRoom()
        val lp = LinearLayout.LayoutParams(0, -1, 1f)
        val lpIcon = 32.dp.let { LinearLayout.LayoutParams(it, it) }
//        addItem(lp, lpIcon, "svga/home_tab_menu.svga", R.drawable.ic_tab_menu_nor, R.string.menu)
        addItem(lp, lpIcon, "svga/home_tab_home.svga", R.drawable.ic_tab_home_nor, R.string.bottom_nav_home)
        addItem(lp, lpIcon,"svga/home_tab_sports.svga", R.drawable.ic_tab_sport_nor, R.string.main_tab_sport)
        addItem(lp, lpIcon, "svga/home_tab_game.svga", R.drawable.ic_tab_game_nor, R.string.B23).isGone = getMarketSwitch()
        addItem(lp, lpIcon, "svga/home_tab_chat.svga", R.drawable.ic_tab_chat_nor, R.string.N984)
        addItem(lp, lpIcon,"svga/home_tab_mine.svga", R.drawable.ic_tab_user_nor, R.string.main_tab_mine)

        if (!chatRoomEnable) {
            disableChatRoom()
        }
        ConfigRepository.onNewConfig(lifecycleOwner) {
            if (chatRoomEnable == isOpenChatRoom()) {
                return@onNewConfig
            }

            if (chatRoomEnable) {
                enableChatRoom()
            } else {
                disableChatRoom()
            }
        }
    }

    private fun addItem(lpItem: LinearLayout.LayoutParams,
                        lpIcon: LinearLayout.LayoutParams,
                        icon: String,
                        @DrawableRes norDrawableRes: Int,
                        @StringRes name: Int): LinearLayout {

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

        return item
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

    fun selectedHome() {
        changeSelected(INDEX_HOME)
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

    private fun disableChatRoom() {
        changeItem(parent.getChildAt(INDEX_CHAT) as LinearLayout, R.string.J748, R.drawable.ic_tab_promo)
    }

    private fun enableChatRoom() {
        changeItem(parent.getChildAt(INDEX_CHAT) as LinearLayout, R.string.N984, R.drawable.ic_tab_chat_nor)
    }

    private fun changeItem(item: LinearLayout, @StringRes name: Int, @DrawableRes icon: Int) {
        item.findViewById<SVGAImageView>(iconId).setImageResource(icon)
        item.findViewById<TextView>(nameId).setText(name)
        item.setOnClickListener {
            if (currentItem != item && onClick.invoke(name)) {
                setSelected(item)
            }
        }
    }

}