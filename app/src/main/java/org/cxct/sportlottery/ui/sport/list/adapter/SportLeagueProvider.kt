package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
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

    private fun getDivider(context: Context): View {
        val divider = View(context)
        divider.setBackgroundResource(R.color.color_D3DEF5)
        return divider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val root = FrameLayout(context)
        root.layoutParams = ViewGroup.LayoutParams(-1, 31.dp)
        root.setBackgroundResource(R.color.color_bbbbbb_ffffff)
        val dividerH = 0.5f.dp
        val dividerParams = FrameLayout.LayoutParams(-1, dividerH)
        root.addView(getDivider(context), dividerParams)

        val img = AppCompatImageView(context)
        img.layoutParams = FrameLayout.LayoutParams(4.dp, 30.dp).apply {
            gravity = Gravity.CENTER_VERTICAL
            topMargin = dividerH
            bottomMargin = dividerH
        }
        img.adjustViewBounds = true
        root.addView(img)

        val wh20 = 20.dp

        val ivCountry = AppCompatImageView(context)
        ivCountry.id = ivCountryId
        ivCountry.scaleType = ImageView.ScaleType.CENTER_CROP
        ivCountry.setImageResource(R.drawable.ic_earth)
        ivCountry.layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
            gravity = Gravity.CENTER_VERTICAL
            leftMargin = 13.dp
            topMargin = dividerH
            bottomMargin = dividerH
        }
        root.addView(ivCountry)

        val tvLeagueName = AppCompatTextView(context).apply {
            id = tvLeagueNameId
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            setTextColor(context.getColor(R.color.color_535D76))
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = 37.dp
                rightMargin = 42.dp
                topMargin = dividerH
                bottomMargin = dividerH
            }
        }
        root.addView(tvLeagueName)

        val ivArrow = AppCompatImageView(context).apply {
            id = ivArrowId
            setImageResource(R.drawable.ic_arrow_gray_up1)
            layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                rightMargin = 12.dp
                topMargin = dividerH
                bottomMargin = dividerH
            }
        }

        root.addView(ivArrow)
        root.addView(getDivider(context), dividerParams)

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
