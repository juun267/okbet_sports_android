package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class MatchTableAdapter(private val onSelected:(Pair<Int, Sport>) -> Unit)
    : BaseQuickAdapter<Pair<Int, Sport>, BaseViewHolder>(0), OnItemClickListener {

    private val selColor = MultiLanguagesApplication.appContext.getColor(R.color.color_025BE8)
    private val norColor = MultiLanguagesApplication.appContext.getColor(R.color.color_6D7693)
    private val numberId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val dividerId = View.generateViewId()
    private var currentPosition = 0
    private var currentItem: Pair<Int, Sport>? = null

    init {
        setOnItemClickListener(this)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        setSelected(position)?.let(onSelected)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val lin = LinearLayout(parent.context)
        lin.orientation = LinearLayout.VERTICAL
        lin.gravity = Gravity.CENTER
        lin.layoutParams = LinearLayout.LayoutParams(-1, -1)

        val numText = AppCompatTextView(parent.context)
        numText.id = numberId
        numText.textSize = 16f
        numText.typeface = AppFont.inter_bold
        numText.gravity = Gravity.CENTER_HORIZONTAL
        lin.addView(numText)

        val nameText = AppCompatTextView(parent.context)
        nameText.id = nameId
        nameText.textSize = 12f
        nameText.typeface = AppFont.helvetica
        nameText.gravity = Gravity.CENTER_HORIZONTAL
        4.dp.let { nameText.setPadding(it, 0, it, 0) }
        nameText.maxLines = 1
        nameText.ellipsize = TextUtils.TruncateAt.END
        lin.addView(nameText)

        val divider = View(context)
        divider.id = dividerId
        divider.setBackgroundColor(context.getColor(R.color.color_CCCBD3F0))
        val dividerLp = FrameLayout.LayoutParams(-1, 1.dp)
        dividerLp.gravity = Gravity.BOTTOM
        dividerLp.leftMargin = 12.dp
        dividerLp.rightMargin = dividerLp.leftMargin

        val root = FrameLayout(context)
        root.layoutParams = LinearLayout.LayoutParams(-1, 68.dp)
        root.addView(divider, dividerLp)
        root.addView(lin)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: Pair<Int, Sport>, payloads: List<Any>) {
        val position = holder.bindingAdapterPosition
        holder.setGone(dividerId, position + 1 == currentPosition || position == itemCount - 1)
    }

    override fun convert(holder: BaseViewHolder, item: Pair<Int, Sport>) {
        val isSelected = item == currentItem
        val textColor = if (isSelected) selColor else norColor
        val numText = holder.getView<TextView>(numberId)
        numText.text = (item.second.items.firstOrNull { it.code== GameType.ES.key }?.num?:0).toString()
        numText.setTextColor(textColor)
        val nameText = holder.getView<TextView>(nameId)
        nameText.setText(item.first)
        nameText.setTextColor(textColor)
        val position = holder.bindingAdapterPosition
        holder.setGone(dividerId, isSelected || position + 1 == currentPosition || position == itemCount - 1)
        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_sportvenue_type)
        } else {
            holder.itemView.background = null
        }
    }

    override fun setNewInstance(list: MutableList<Pair<Int, Sport>>?) {
        changeSelected(list?.first(), 0)
        super.setNewInstance(list)
    }

    private fun changeSelected(item: Pair<Int, Sport>?, position: Int) {
        currentItem = item
        currentPosition = position
    }

    fun setSelected(position:Int): Pair<Int, Sport>? {
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

}