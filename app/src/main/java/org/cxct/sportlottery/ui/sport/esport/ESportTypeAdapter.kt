package org.cxct.sportlottery.ui.sport.esport

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.opensource.svgaplayer.SVGAImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
import org.cxct.sportlottery.util.drawable.DrawableCreator


class ESportTypeAdapter(private val itemClick: (CategoryItem, Int) -> Unit) : BaseQuickAdapter<CategoryItem, BaseViewHolder>(0), OnItemClickListener {

    init {
        setOnItemClickListener(this)
    }

    private val iconParams by lazy {
        val wh = 40.dp
        val param = FrameLayout.LayoutParams(wh, wh)
        8.dp.let {
//            param.topMargin = it
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

    var currentItem: CategoryItem? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = SVGAImageView(parent.context)
        itemView.layoutParams = iconParams
        itemView.scaleType = ImageView.ScaleType.CENTER_CROP
        return BaseViewHolder(itemView)
    }

    override fun convert(holder: BaseViewHolder, item: CategoryItem) = (holder.itemView as SVGAImageView).run {
        setPadding(0, 0, 0, 0)
        loops = 1
        clearsAfterStop = false
        setAssetSvgIcon(GameType.getGameTypeMenuSVGA(item.code))
        if (item.isSelected){
            stepToFrame(0, true)
        }
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
        (view as SVGAImageView).stepToFrame(0, true)
        itemClick.invoke(currentItem!!, position)
    }

    override fun setNewInstance(list: MutableList<CategoryItem>?) {
        currentItem = list?.find { it.isSelected }
        super.setNewInstance(list)
    }

}

