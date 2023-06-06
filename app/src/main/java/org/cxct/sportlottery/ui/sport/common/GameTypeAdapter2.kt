package org.cxct.sportlottery.ui.sport.common

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.util.DisplayUtil.dp

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

    var currentItem: Item? = null
    private set

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = AppCompatImageView(parent.context)
        itemView.layoutParams = iconParams
        return BaseViewHolder(itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Item) = (holder.itemView as ImageView).run {
        setImageResource(GameType.getGameTypeMenuIcon(item.code))
        isSelected = item.isSelected
    }

    override fun convert(holder: BaseViewHolder, item: Item, payloads: List<Any>) {
        holder.itemView.isSelected = item.isSelected
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

