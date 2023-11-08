package org.cxct.sportlottery.ui.sport.esport

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator


class ESportTypeAdapter(private val itemClick: (CategoryItem, Int) -> Unit) : BaseQuickAdapter<CategoryItem, BaseViewHolder>(0), OnItemClickListener {

    init {
        setOnItemClickListener(this)
    }

    private val iconParams by lazy {
        val wh = 40.dp
        val param = FrameLayout.LayoutParams(wh, wh)
        8.dp.let {

        }
        param.topMargin = 8.dp
        param.bottomMargin = 10.dp
        10.dp.let {
            param.leftMargin = it
            param.rightMargin = it
        }
        param
    }

    private val bgDrawable by lazy {
        DrawableCreator.Builder()
            .setSolidColor(ContextCompat.getColor(context, R.color.color_D3E0F3))
            .setSizeWidth(iconParams.width.toFloat())
            .setSizeHeight(iconParams.height.toFloat())
            .setCornersRadius(iconParams.height.toFloat())
            .setStrokeColor(ContextCompat.getColor(context, R.color.color_FFFFFF))
            .setStrokeWidth(1.dp.toFloat())
            .build()
    }

    var currentItem: CategoryItem? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = ImageView(parent.context)
        itemView.layoutParams = iconParams
        itemView.scaleType = ImageView.ScaleType.CENTER_CROP
        itemView.setPadding(12, 12, 12, 12)
        return BaseViewHolder(itemView)
    }

    override fun convert(holder: BaseViewHolder, item: CategoryItem) = (holder.itemView as ImageView).run {
        setImageResource(ESportType.getESportIcon(item.code))
        background = if (item.isSelected) bgDrawable else null
    }

    override fun convert(holder: BaseViewHolder, item: CategoryItem, payloads: List<Any>) {
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
                notifyItemChanged(lastPosition,Any())
            }
        }

        item.isSelected = true
        currentItem = item
        notifyItemChanged(position,Any())
        itemClick.invoke(currentItem!!, position)
    }

    override fun setNewInstance(list: MutableList<CategoryItem>?) {
        currentItem = list?.find { it.isSelected }
        super.setNewInstance(list)
    }

}

