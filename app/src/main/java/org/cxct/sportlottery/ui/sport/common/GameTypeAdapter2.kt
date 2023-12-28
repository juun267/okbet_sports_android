package org.cxct.sportlottery.ui.sport.common

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.opensource.svgaplayer.SVGAImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAItem
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.setServiceClick


class GameTypeAdapter2(private val itemClick: (Item, Int) -> Unit) : BaseQuickAdapter<Item, BaseViewHolder>(0), OnItemClickListener {

    init {
        setOnItemClickListener(this)
    }


    fun applyEventView(activity: FragmentActivity) {
        if (hasFooterLayout()) {
            return
        }
        val ivPromotion = AppCompatImageView(activity)
        ivPromotion.layoutParams = iconParams
        ivPromotion.setImageResource(R.drawable.ic_sport_promotion)
        ivPromotion.setOnClickListener { activity.startActivity(PromotionListActivity::class.java) }
        addFooterView(ivPromotion, -1, LinearLayout.HORIZONTAL)
        val ivService = AppCompatImageView(activity)
        ivService.layoutParams = iconParams
        ivService.setImageResource(R.drawable.ic_sport_service)
        ivService.setServiceClick(activity.supportFragmentManager)
        addFooterView(ivService, -1, LinearLayout.HORIZONTAL)
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

    var currentItem: Item? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = SVGAImageView(parent.context)
        itemView.layoutParams = iconParams
        itemView.scaleType = ImageView.ScaleType.CENTER_CROP
        return BaseViewHolder(itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Item) = (holder.itemView as SVGAImageView).run {
        if (item is FIBAItem) {
            7.dp.let { setPadding(it, it, it, it) }
            setImageDrawable(item.icon)
        } else {
            setPadding(0, 0, 0, 0)
            loops = 1
            clearsAfterStop = false
            setAssetSvgIcon(GameType.getGameTypeMenuSVGA(item.code))
            if (item.isSelected){
                stepToFrame(0, true)
            }
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
                notifyItemChanged(lastPosition,Any())
            }
        }

        item.isSelected = true
        currentItem = item
        notifyItemChanged(position,Any())
        (view as SVGAImageView).stepToFrame(0, true)
        itemClick.invoke(currentItem!!, position)
    }

    override fun setNewInstance(list: MutableList<Item>?) {
        recyclerViewOrNull?.isVisible = !list.isNullOrEmpty()
        currentItem = list?.find { it.isSelected }
        super.setNewInstance(list)
    }

    fun selectGameType(gameType: String) {
        if (gameType == currentItem?.code) {
            return
        }

        data.forEachIndexed { index, item ->
            if (item.code == gameType) {
                val vh = recyclerView.findViewHolderForAdapterPosition(index)
                if (vh != null) {
                    vh.itemView.performClick()
                } else {
                    currentItem?.let {
                        it.isSelected = false
                        val position = getItemPosition(it)
                        notifyItemChanged(position, position)
                    }

                    item.isSelected = true
                    currentItem = item
                    itemClick.invoke(currentItem!!, index)
                    notifyItemChanged(index, index)
                }
                return
            }
        }
    }

}

