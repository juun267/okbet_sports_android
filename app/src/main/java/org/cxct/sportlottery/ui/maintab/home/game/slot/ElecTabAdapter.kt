package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class ElecTabAdapter: BaseQuickAdapter<OKGamesCategory, BaseViewHolder>(0) {

    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val lp = LinearLayout.LayoutParams(-1, 68.dp)
    private val iconLp = LinearLayout.LayoutParams(32.dp, 32.dp).apply { topMargin = 12.dp }
    private val nameLp = LinearLayout.LayoutParams(-2, -2).apply { topMargin = 5.dp }
    private var selectPosition = 0
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val lin = LinearLayout(parent.context)
        lin.orientation = LinearLayout.VERTICAL
        lin.gravity = Gravity.CENTER
        lin.layoutParams = lp

        val gameIcon = AppCompatImageView(parent.context)
        gameIcon.id = iconId
        lin.addView(gameIcon, iconLp)

        val nameText = AppCompatTextView(parent.context)
        nameText.id = nameId
        nameText.textSize = 10f
        nameText.typeface = AppFont.helvetica
        nameText.gravity = Gravity.CENTER_HORIZONTAL
        nameText.maxLines = 1
        nameText.ellipsize = TextUtils.TruncateAt.END
        lin.addView(nameText, nameLp)

        return BaseViewHolder(lin)
    }

    override fun convert(holder: BaseViewHolder, item: OKGamesCategory) {
        holder.getView<ImageView>(iconId).let {
            item.bindLabelIcon(it)
        }
        holder.getView<TextView>(nameId).let {
            item.bindNameText(it)
        }
        if (selectPosition == holder.layoutPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_sportvenue_type)
        } else {
            holder.itemView.background = null
        }
    }

    fun setSelected(position: Int) {
       if (selectPosition == position){
           return
       }
        selectPosition = position
        notifyDataSetChanged()
    }

}