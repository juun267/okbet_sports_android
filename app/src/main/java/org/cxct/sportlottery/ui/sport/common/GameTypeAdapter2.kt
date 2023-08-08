package org.cxct.sportlottery.ui.sport.common

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAItem
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class GameTypeAdapter2(private val itemClick: (Item, Int) -> Unit) : BaseQuickAdapter<Item, BaseViewHolder>(0), OnItemClickListener {

    init {
        setOnItemClickListener(this)
    }

    private val iconParams by lazy {
        val wh = 40.dp
        val param = FrameLayout.LayoutParams(wh, wh)
        8.dp.let {
            param.topMargin = it
            param.bottomMargin = it
        }
        10.dp.let {
            param.leftMargin = it
            param.rightMargin = it
        }
        param
    }

    private val bgDrawable by lazy {
        DrawableCreator.Builder()
            .setShapeAlpha(0.3f)
            .setSolidColor(ContextCompat.getColor(context, R.color.color_025BE8))
            .setSizeWidth(iconParams.width.toFloat())
            .setSizeHeight(iconParams.height.toFloat())
            .setCornersRadius(iconParams.height.toFloat())
            .build()
    }

    var currentItem: Item? = null
    private set

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = AppCompatImageView(parent.context)
        itemView.layoutParams = iconParams
        itemView.scaleType = ImageView.ScaleType.CENTER_CROP
        return BaseViewHolder(itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Item) = (holder.itemView as ImageView).run {
        if (item is FIBAItem) {
            7.dp.let { setPadding(it, it, it, it) }
            setImageDrawable(item.icon)
        } else {
            setPadding(0, 0, 0, 0)
            setImageResource(GameType.getGameTypeMenuIcon(item.code))
        }
        background = if (item.isSelected) bgDrawable else null
    }

    override fun convert(holder: BaseViewHolder, item: Item, payloads: List<Any>) {
        holder.itemView.background = if (item.isSelected) bgDrawable else null
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = getItem(position)
        if (currentItem == item) {
            return
        }

        currentItem?.let {
            it.isSelected = false
            val lastPosition = getItemPosition(currentItem)
            if (lastPosition >=0 && lastPosition < getDefItemCount()) {
                notifyItemChanged(lastPosition)
            }
        }

        item.isSelected = true
        currentItem = item
        notifyItemChanged(position)
        itemClick.invoke(currentItem!!, position)
    }

    override fun setNewInstance(list: MutableList<Item>?) {
        currentItem = list?.find { it.isSelected }
        super.setNewInstance(list)
    }

}

