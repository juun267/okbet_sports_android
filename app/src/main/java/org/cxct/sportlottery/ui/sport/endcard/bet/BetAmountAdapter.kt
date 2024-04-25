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
    private val defaultTextColor by lazy { context.getColor(R.color.color_FFFFFF) }
    private val selectedBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_1475E1), context.getColor(R.color.color_003CD7))
            .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
            .setRadius(20.dp.toFloat())
            .setWidth(80.dp)
            .setHeight(40.dp)
    }
    private val defaultBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.transparent))
            .setRadius(20.dp.toFloat())
            .setWidth(80.dp)
            .setHeight(40.dp)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.textSize = 14f
        textView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(-2, 40.dp)
        5.dp.let { lp.setMargins(4.dp, it, 0, it) }
        textView.layoutParams = lp
        return BaseViewHolder(textView)
    }

    override fun convert(holder: BaseViewHolder, item: EndCardBet, payloads: List<Any>) {
        changeStyle(false, holder.itemView as TextView)
    }

    override fun convert(holder: BaseViewHolder, item: EndCardBet) {
        with(holder.itemView as TextView) {
            text = "${context.getText(R.string.P318)} $showCurrencySign${item.betMoney}"
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

    fun setUpData(list: MutableList<EndCardBet>?, selectItem: EndCardBet) {
        selectedItem = selectItem
        setNewInstance(list)
    }
}