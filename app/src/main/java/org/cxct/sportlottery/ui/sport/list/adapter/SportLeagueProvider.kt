package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setLeagueLogo


class SportLeagueProvider(
    private val adapter: SportLeagueAdapter2,
    override val itemViewType: Int = 1,
    override val layoutId: Int = 0): BaseNodeProvider() {

    private val tvLeagueNameId = View.generateViewId()
    private val ivCountryId = View.generateViewId()
    private val ivArrowId = View.generateViewId()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val root = FrameLayout(context)
        root.layoutParams = ViewGroup.LayoutParams(-1, -2)
        root.setBackgroundResource(R.color.color_0D025BE8)
        6.dp.let { root.setPadding(0, it, 0, it) }

        val wh20 = 20.dp

        val ivCountry = AppCompatImageView(context)
        ivCountry.id = ivCountryId
        ivCountry.scaleType = ImageView.ScaleType.CENTER_CROP
        ivCountry.layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
            gravity = Gravity.CENTER_VERTICAL
            leftMargin = 12.dp
            topMargin = 2.dp
        }
        root.addView(ivCountry)

        val tvLeagueName = AppCompatTextView(context).apply {
            id = tvLeagueNameId
            maxLines = 2
            typeface = Typeface.DEFAULT_BOLD
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            setTextColor(context.getColor(R.color.color_535D76))
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = 40.dp
                rightMargin = 56.dp
            }
        }
        root.addView(tvLeagueName)

        val ivArrow = AppCompatImageView(context).apply {
            id = ivArrowId
            setImageResource(R.drawable.ic_arrow_gray_up1)
            layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                rightMargin = 12.dp
            }
        }

        root.addView(ivArrow)
        return BaseViewHolder(root)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val leagueOdd = item as LeagueOdd
        helper.setText(tvLeagueNameId, leagueOdd.league.name)
        helper.getView<ImageView>(ivCountryId).setLeagueLogo(item.league.categoryIcon)
        setArrowSpin(helper.getView(ivArrowId), leagueOdd, false)
    }

    override fun onClick(helper: BaseViewHolder, view: View, item: BaseNode, position: Int) {
        val position = adapter.getItemPosition(item)
        adapter.expandOrCollapse(item, parentPayload = position)
        val league = item as LeagueOdd
        setArrowSpin(helper.getView(ivArrowId), league, true)
    }

    private fun setArrowSpin(ivArrow: ImageView, data: LeagueOdd, isAnimate: Boolean) {

        var rotation = 180f
        if (data.isExpanded) {
            rotation = 0f
        }

        if (isAnimate) {
            ivArrow.rotationAnimation(rotation)
        } else {
            ivArrow.rotation = rotation
        }
    }
}
