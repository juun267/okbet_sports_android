package org.cxct.sportlottery.ui.maintab.home

import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SPUtil

object MainHomeItemHelper {

    private data class HomeItem(
        @StringRes val name: Int,
        @DrawableRes val icon: Int,
        @DrawableRes val background: Int = R.drawable.bg_tab_open_unselector,
        @ColorRes val nameColor: Int = R.color.color_535D76,
        val nameSize: Int = 10,
    )

    private val items = mutableListOf<HomeItem>().apply {
        add(
            HomeItem(R.string.home_recommend,
            R.drawable.icon_recommend,
            R.drawable.bg_tab_open_selector,
            R.color.color_E9F4FA,
            11)
        )
        // 推荐
        add(HomeItem(R.string.home_sports, R.drawable.sport0)) //体育
        add(HomeItem(R.string.home_in_play, R.drawable.icon_in_play0)) //滚球
        if (!SPUtil.getMarketSwitch()) {
            add(HomeItem(R.string.home_on_game, R.drawable.icon_on_game0)) //棋牌
            add(HomeItem(R.string.promo, R.drawable.ic_home_promo_nor)) // 优惠
        }
        add(HomeItem(R.string.home_live, R.drawable.live0)) //直播
    }


    fun fillingItems(parent: LinearLayout, itemClick: (Int) -> Unit) {

        val itemParams = LinearLayout.LayoutParams(0, 60.dp, 1F)
        val iconParams = LinearLayout.LayoutParams(30.dp, 30.dp).apply { gravity = Gravity.CENTER_HORIZONTAL }
        val nameParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 6.dp }


        items.forEach { addItem(parent, it, itemParams, iconParams, nameParams, itemClick) }
    }

    private fun addItem(parent: LinearLayout,
                        itemBean: HomeItem,
                        itemParams: LinearLayout.LayoutParams,
                        iconParams: LinearLayout.LayoutParams,
                        nameParams: LinearLayout.LayoutParams,
                        itemClick: (Int) -> Unit) {

        val context = parent.context
        val item = LinearLayout(context)
        item.gravity = Gravity.CENTER
        item.orientation = LinearLayout.VERTICAL
        item.setBackgroundResource(itemBean.background)
        item.setOnClickListener { itemClick.invoke(itemBean.name) }

        val icon = ImageView(context)
        icon.setImageResource(itemBean.icon)

        val name = AppCompatTextView(context)
        name.gravity = Gravity.CENTER
        name.setText(itemBean.name)
        name.setTextColor(context.getColor(itemBean.nameColor))
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemBean.nameSize.toFloat())

        item.addView(icon, iconParams)
        item.addView(name, nameParams)
        parent.addView(item, itemParams)
    }
}