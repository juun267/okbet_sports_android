package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp


class SportTypeAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(SportGroupProvider(this))
        addFullSpanNodeProvider(SportMatchProvider(this))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return if (data[position] is SportGroup) 1 else 2
    }

    fun setUp(datas: MutableList<Pair<Int, Sport>>) {
        val list = mutableListOf<SportGroup>()
        datas.forEach { list.add(SportGroup(it.first, it.second.items.toMutableList())) }
        setNewInstance(list as MutableList<BaseNode>)
    }


}

 data class SportGroup(val name: Int,
                              val items: MutableList<Item>,
                              override val childNode: MutableList<BaseNode>? = items as MutableList<BaseNode>?): BaseNode()

private class SportGroupProvider(val adapter: SportTypeAdapter, override val itemViewType: Int = 1, override val layoutId: Int = 0): BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.setTextColor(Color.BLACK)
        textView.textSize = 14f
        textView.setPadding(0, 0, 0, 8.dp)
        return BaseViewHolder(textView)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val textView = helper.itemView as TextView
        textView.setText((item as SportGroup).name)
        textView.setPadding(0, if (adapter.getItemOrNull(0) == item) 0 else textView.paddingBottom, 0, textView.paddingBottom)
    }

}

private class SportMatchProvider(val adapter: SportTypeAdapter, override val itemViewType: Int = 2, override val layoutId: Int = 0): BaseNodeProvider() {

    private val numberId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val imgId = View.generateViewId()
    private val dp8 = 8.dp
    private val lp = LinearLayout.LayoutParams(-1, 100.dp)
    private val nameLp = FrameLayout.LayoutParams(-2, -2).apply { topMargin = 24.dp }
    private val imgLp = FrameLayout.LayoutParams(-2, -2).apply {
        gravity = Gravity.RIGHT or Gravity.BOTTOM
    }
    private val numLp = FrameLayout.LayoutParams(-2, -2).apply {
        gravity = Gravity.BOTTOM
        bottomMargin = 15.dp
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val context = parent.context
        val frameLayout = FrameLayout(context)
        frameLayout.layoutParams = lp
        frameLayout.setBackgroundResource(R.drawable.bg_home_sport_card)
        frameLayout.foreground = parent.context.getDrawable(R.drawable.fg_ripple)
        frameLayout.setPadding(24.dp, 0, 0, 0)

        val img = AppCompatImageView(context)
        img.id = imgId
        frameLayout.addView(img, imgLp)

        val nameText = AppCompatTextView(context)
        nameText.id = nameId
        nameText.setTextColor(context.getColor(R.color.color_025BE8))
        nameText.textSize = 16f
        nameText.typeface = AppFont.inter_bold
        frameLayout.addView(nameText, nameLp)

        val numText = AppCompatTextView(context)
        numText.id = numberId
        numText.setTextColor(context.getColor(R.color.color_313F56))
        numText.textSize = 20f
        numText.typeface = AppFont.inter_bold
        frameLayout.addView(numText, numLp)

        return BaseViewHolder(frameLayout)
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val bean = item as Item
        helper.setText(nameId, bean.name)
        helper.setText(numberId, bean.num.toString())
        helper.setImageResource(imgId, GameType.getSportHomeImg("${bean.code}"))
        (helper.itemView.layoutParams as MarginLayoutParams).bottomMargin = if (helper.absoluteAdapterPosition==adapter.itemCount-1) 20.dp else dp8
    }


}