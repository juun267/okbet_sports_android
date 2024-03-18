package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation

class BetAmountAdapter(private val onItemClick: (EndCardBet)-> Unit): BaseQuickAdapter<EndCardBet, BaseViewHolder>(0) {

    private var selectedItem: EndCardBet? = null
    private val selectedColor = Color.WHITE
    private val defaultTextColor by lazy { context.getColor(R.color.color_BEC7DC) }
    private val selectedBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_1F75FF), context.getColor(R.color.color_0D31AF))
            .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
            .setRadius(8.dp.toFloat())
            .setWidth(80.dp)
            .setHeight(36.dp)
    }
    private val defaultBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_26354D))
            .setRadius(8.dp.toFloat())
            .setWidth(80.dp)
            .setHeight(36.dp)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.textSize = 14f
        textView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(-2, -2)
        lp.leftMargin = 8.dp
        lp.topMargin = 7.dp
        textView.layoutParams = lp
        return BaseViewHolder(textView)
    }

    override fun convert(holder: BaseViewHolder, item: EndCardBet, payloads: List<Any>) {
        changeStyle(false, holder.itemView as TextView)
    }

    override fun convert(holder: BaseViewHolder, item: EndCardBet) {
        with(holder.itemView as TextView) {
            text = "${context.getText(R.string.P318)} ${item.betMoney}$showCurrencySign"
            changeStyle(selectedItem == item, this)
            setOnClickListener {
                if (selectedItem != item) {
                    onItemClick.invoke(item)
                    val lastPosition = getItemPosition(selectedItem)
                    selectedItem = item
                    notifyItemChanged(lastPosition, lastPosition)
                    changeStyle(true, this)
                }
            }
        }
    }

    private fun changeStyle(isSelected: Boolean, textView: TextView) = textView.run {
        if (isSelected) {
            background = selectedBg
            setTextColor(selectedColor)
        } else {
            background = defaultBg
            setTextColor(defaultTextColor)
        }
    }

    override fun setNewInstance(list: MutableList<EndCardBet>?) {
        selectedItem = list?.firstOrNull()
        super.setNewInstance(list)
    }
}