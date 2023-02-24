package org.cxct.sportlottery.ui.maintab

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
import org.cxct.sportlottery.util.isCreditSystem

object MainHomeItemHelper {

    private data class HomeItem(
        @StringRes val name: Int,
        @DrawableRes val icon: Int,
        @DrawableRes val background: Int = R.drawable.bg_tab_open_unselector,
        @ColorRes val nameColor: Int = R.color.color_535D76,
        val nameSize: Int = 10,
    )

    private val items = arrayOf(
        HomeItem(R.string.home_sports, R.drawable.sport0), //体育
        HomeItem(R.string.home_in_play, R.drawable.icon_in_play0), //滚球
        HomeItem(R.string.home_on_game, R.drawable.icon_on_game0), //棋牌
        HomeItem(R.string.tiger_machine, R.drawable.ic_home_okgame_nor), //老虎机
        HomeItem(R.string.promo, R.drawable.ic_home_promo_nor), // 优惠
        HomeItem(R.string.home_live, R.drawable.live0), //直播
    )

    private val items1 = arrayOf(
        HomeItem(R.string.home_recommend,
            R.drawable.icon_recommend,
            R.drawable.bg_tab_open_selector,
            R.color.color_E9F4FA,
            11), // 推荐
        HomeItem(R.string.home_live, R.drawable.live0), //直播
//        HomeItem(R.string.home_word_cup, R.drawable.word_cup0), //世界杯
        HomeItem(R.string.home_in_play, R.drawable.icon_in_play0), //滚球
        HomeItem(R.string.home_sports, R.drawable.sport0), //体育
        HomeItem(R.string.live, R.drawable.icon_on_live0), //真人
        HomeItem(R.string.home_on_game, R.drawable.icon_on_game0), //棋牌
        HomeItem(R.string.lottery, R.drawable.icon_on_lottery0), //彩票
    )


    fun fillingItems(parent: LinearLayout, itemClick: (Int) -> Unit) {

        val itemParams = LinearLayout.LayoutParams(0, 60.dp, 1.0f)
        val iconParams = LinearLayout.LayoutParams(30.dp, 30.dp).apply { gravity = Gravity.CENTER_HORIZONTAL }
        val nameParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 6.dp }

        if (isCreditSystem()) {
            items1.forEach { addItem(parent, it, itemParams, iconParams, nameParams, itemClick) }
        } else {
            items.forEach { addItem(parent, it, itemParams, iconParams, nameParams, itemClick) }
        }
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