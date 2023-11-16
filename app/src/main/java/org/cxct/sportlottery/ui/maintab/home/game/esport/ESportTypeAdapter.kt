package org.cxct.sportlottery.ui.maintab.home.game.esport

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp


class ESportTypeAdapter : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(ESportGroupProvider())
        addFullSpanNodeProvider(ESportMatchProvider())
        setAnimationWithDefault(AnimationType.SlideInBottom)
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return if (data[position] is ESportGroup) 1 else 2
    }

    fun setUp(datas: MutableList<Pair<Int, Item?>>) {
        val list = mutableListOf<ESportGroup>()
        datas.forEach { list.add(ESportGroup(it.first, it.second?.categoryList?.map { categoryItem-> ESportMatch(it.first,categoryItem,null) }?.toMutableList()?: mutableListOf())) }
        setNewInstance(list as MutableList<BaseNode>)
    }
}

 data class ESportGroup(val name: Int,
                              val items: MutableList<ESportMatch>,
                              override val childNode: MutableList<BaseNode>? = items as MutableList<BaseNode>?): BaseNode()

private class ESportGroupProvider(override val itemViewType: Int = 1, override val layoutId: Int = 0): BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.setTextColor(Color.BLACK)
        textView.textSize = 14f
        textView.setPadding(0, 0, 0, 8.dp)
        return BaseViewHolder(textView)
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        (helper.itemView as TextView).setText((item as ESportGroup).name)
    }

}
data class ESportMatch(val name: Int,
                       val item: CategoryItem,
                       override val childNode: MutableList<BaseNode>?): BaseNode()

private class ESportMatchProvider(override val itemViewType: Int = 2, override val layoutId: Int = 0): BaseNodeProvider() {

    private val numberId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val iconId = View.generateViewId()
    private val imgId = View.generateViewId()
    private val lp = LinearLayout.LayoutParams(-1, 100.dp).apply { bottomMargin = 8.dp }
    private val nameLp = FrameLayout.LayoutParams(-2, -2).apply { topMargin = 44.dp }
    private val iconLp = FrameLayout.LayoutParams(20.dp, 20.dp).apply {
        topMargin = 16.dp
    }
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
        frameLayout.setBackgroundResource(R.drawable.bg_home_esport_card)
        frameLayout.setPadding(24.dp, 0, 0, 0)
        val icon = AppCompatImageView(context)
        icon.id = iconId
        frameLayout.addView(icon, iconLp)

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
        numText.setTextColor(context.getColor(R.color.color_0D2245))
        numText.textSize = 14f
        numText.typeface = AppFont.inter_bold
        frameLayout.addView(numText, numLp)

        return BaseViewHolder(frameLayout)
    }
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val bean = item as ESportMatch
        helper.setText(nameId, bean.item.name)
        helper.setText(numberId, bean.item.num.toString())
        helper.setImageResource(iconId,ESportType.getHomeESportIcon(bean.item.code))
        helper.setImageResource(imgId,ESportType.getHomeESportBg(bean.item.code))
    }

}