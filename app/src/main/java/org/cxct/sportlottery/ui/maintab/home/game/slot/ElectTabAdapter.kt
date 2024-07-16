package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class ElectTabAdapter(private val onSelected:(OKGamesCategory) -> Unit)
    : BaseQuickAdapter<OKGamesCategory, BaseViewHolder>(0), OnItemClickListener {

    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val dividerId = View.generateViewId()
    private val selColor = MultiLanguagesApplication.appContext.getColor(R.color.color_025BE8)
    private val norColor = MultiLanguagesApplication.appContext.getColor(R.color.color_6D7693)
    private val lp = LinearLayout.LayoutParams(-1, -1)
    private val iconLp = 32.dp.let { LinearLayout.LayoutParams(it, it)}
    private val nameLp = LinearLayout.LayoutParams(-2, -2).apply { topMargin = 4.dp }
    private var currentPosition = 0
    private var currentItem: OKGamesCategory? = null

    init {
        setOnItemClickListener(this)
    }

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
        nameText.maxLines = 2
        nameText.ellipsize = TextUtils.TruncateAt.END
        lin.addView(nameText, nameLp)

        val divider = View(context)
        divider.id = dividerId
        divider.setBackgroundColor(context.getColor(R.color.color_CCCBD3F0))
        val dividerLp = FrameLayout.LayoutParams(-1, 1.dp)
        dividerLp.gravity = Gravity.BOTTOM
        dividerLp.leftMargin = 12.dp
        dividerLp.rightMargin = dividerLp.leftMargin

        val root = FrameLayout(context)
        root.layoutParams = LinearLayout.LayoutParams(-1, 73.dp)
        root.addView(divider, dividerLp)
        root.addView(lin)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: OKGamesCategory) {
        item.bindLabelIcon(holder.getView(iconId))
        val nameText = holder.getView<TextView>(nameId)
        nameText.text = item.categoryName
        val position = holder.bindingAdapterPosition
        val isSelected = currentPosition == position
        val isLast = position == itemCount - 1
        holder.setGone(dividerId, isSelected || position + 1 == currentPosition || isLast)
        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_sportvenue_type)
            nameText.typeface = AppFont.helvetica_bold
            nameText.setTextColor(selColor)
        } else {
            holder.itemView.background = null
            nameText.typeface = AppFont.helvetica
            nameText.setTextColor(norColor)
        }

    }

    override fun convert(holder: BaseViewHolder, item: OKGamesCategory, payloads: List<Any>) {
        val position = holder.bindingAdapterPosition
        holder.setGone(dividerId, position + 1 == currentPosition || position == itemCount - 1)
    }

    override fun setNewInstance(list: MutableList<OKGamesCategory>?) {
        changeSelected(list?.first(), 0)
        super.setNewInstance(list)
    }

    private fun changeSelected(item: OKGamesCategory?, position: Int) {
        currentItem = item
        currentPosition = position
    }

    fun setSelected(position:Int): OKGamesCategory? {
        if (position == currentPosition) {
            return null
        }
        val item = getItemOrNull(position) ?: return null
        val lastPosition = currentPosition
        val lastPre = currentPosition - 1
        val lastNext = currentPosition + 1
        changeSelected(item, position)

        val currentPre = position - 1
        val currentNext = position + 1
        val count = data.size
        val dividerChangedPosition = mutableSetOf<Int>()
        if (lastPre >= 0) {
            dividerChangedPosition.add(lastPre)
        }
        if (lastNext < count) {
            dividerChangedPosition.add(lastNext)
        }
        if (currentPre >= 0 && currentPre != lastPre && currentPre != lastPosition && currentPre != lastNext) {
            dividerChangedPosition.add(currentPre)
        }
        if (currentNext < count && currentNext != lastPre && currentNext != lastPosition && currentNext != lastNext) {
            dividerChangedPosition.add(currentNext)
        }

        notifyItemChanged(lastPosition)
        notifyItemChanged(position)
        dividerChangedPosition.forEach { notifyItemChanged(it, it) }

        return item
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        setSelected(position)?.let(onSelected)
    }

}