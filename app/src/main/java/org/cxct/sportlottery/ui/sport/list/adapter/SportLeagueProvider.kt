package org.cxct.sportlottery.ui.sport.list.adapter

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
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.views.lines


class SportLeagueProvider(
    private val adapter: SportLeagueAdapter2,
    override val itemViewType: Int = 1,
    override val layoutId: Int = 0): BaseNodeProvider() {

    private val tvLeagueNameId = View.generateViewId()
    private val ivCountryId = View.generateViewId()
    private val ivArrowId = View.generateViewId()
    private val tvNumId = View.generateViewId()
    private val dividerId = View.generateViewId()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val root = FrameLayout(context)
        root.layoutParams = ViewGroup.LayoutParams(-1, 44.dp)
        root.setBackgroundResource(R.color.color_FCFDFF)
        root.foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)

        val topDivider = View(context)
        topDivider.id = dividerId
        topDivider.layoutParams = ViewGroup.LayoutParams(-1, 4.dp)
        topDivider.setBackgroundColor(ContextCompat.getColor(context, R.color.color_E1EDFF))
        root.addView(topDivider)

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
            lines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            setTextColor(context.getColor(R.color.color_000000))
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = 40.dp
                rightMargin = 56.dp
            }
        }
        root.addView(tvLeagueName)

        val ivArrow = AppCompatImageView(context).apply {
            id = ivArrowId
            layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                rightMargin = 12.dp
            }
        }
        root.addView(ivArrow)
        val tvNum = AppCompatTextView(context).apply {
            id = tvNumId
            background = ContextCompat.getDrawable(context,R.drawable.bg_blue_radius_10_stroke)
            setTextColor(ContextCompat.getColor(context,R.color.color_025BE8))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(-2, wh20).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                rightMargin = 12.dp
                setPadding(4.dp,0,4.dp,0)
                minWidth = 26.dp
            }
        }
        root.addView(tvNum)

        val divider = View(context)
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.color_D4E1F1))
        divider.layoutParams = FrameLayout.LayoutParams(-2, 0.5f.dp).apply {
            gravity = Gravity.BOTTOM
        }

        root.addView(divider)
        return BaseViewHolder(root)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val leagueOdd = item as LeagueOdd
        helper.setText(tvLeagueNameId, leagueOdd.league.name)
        helper.setGone(dividerId, helper.bindingAdapterPosition == 0)
        helper.getView<ImageView>(ivCountryId).setLeagueLogo(item.league.categoryIcon)
        setExpandArrow(helper.getView(ivArrowId), leagueOdd.isExpanded)
        helper.setText(tvNumId,leagueOdd.matchOdds.size.toString())
        helper.setVisible(ivArrowId,leagueOdd.isExpanded)
        helper.setGone(tvNumId,leagueOdd.isExpanded)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        val leagueOdd = item as LeagueOdd
        setExpandArrow(helper.getView(ivArrowId), leagueOdd.isExpanded)
        helper.setVisible(ivArrowId, leagueOdd.isExpanded)
        helper.setGone(tvNumId,leagueOdd.isExpanded)
    }

    override fun onClick(helper: BaseViewHolder, view: View, item: BaseNode, position: Int) {
        val position = adapter.getItemPosition(item)
        adapter.nodeExpandOrCollapse(item, parentPayload = position)
        val league = item as LeagueOdd
        helper.getView<ImageView>(ivArrowId).apply {
            setArrowSpin(league.isExpanded, true) {
                setExpandArrow(this, league.isExpanded)
                helper.setVisible(ivArrowId, league.isExpanded)
                helper.setGone(tvNumId,league.isExpanded)
            }
        }
    }

}
