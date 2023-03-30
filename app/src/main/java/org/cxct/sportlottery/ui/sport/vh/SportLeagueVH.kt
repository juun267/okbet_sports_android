package org.cxct.sportlottery.ui.sport.vh

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setLeagueLogo
import org.cxct.sportlottery.view.expandablerecyclerview.ExpandableAdapter

class SportLeagueVH(val ivCountry: ImageView,
                    val tvLeagueName: TextView,
                    val ivArrow: ImageView,
                    val lifecycle: LifecycleOwner,
                    root: View): ExpandableAdapter.ViewHolder(root) {

    companion object {
        fun of(context: Context, lifecycle: LifecycleOwner): SportLeagueVH {
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
                setImageResource(R.drawable.selector_arrow_gray)
                layoutParams = FrameLayout.LayoutParams(wh20, wh20).apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                    rightMargin = 12.dp
                    topMargin = dividerH
                    bottomMargin = dividerH
                }
            }

            root.addView(ivArrow)
            root.addView(getDivider(context), dividerParams)

            return SportLeagueVH(ivCountry, tvLeagueName, ivArrow, lifecycle, root)
        }

        private fun getDivider(context: Context): View {
            val divider = View(context)
            divider.setBackgroundResource(R.color.color_D3DEF5)
            return divider
        }
    }

    fun bind(position: Int, item: LeagueOdd, payloads: List<Any>) {
        tvLeagueName.text = item.league.name
        ivCountry.setLeagueLogo(item.league.categoryIcon)
        ivArrow.isSelected = item.unfoldStatus == FoldState.FOLD.code
    }

    fun onExpandChange(expand: Boolean, item: LeagueOdd) {
        item.unfoldStatus = if (expand) FoldState.UNFOLD.code else FoldState.FOLD.code
        ivArrow.isSelected = !expand
    }
}