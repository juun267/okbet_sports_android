package org.cxct.sportlottery.ui.sport.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemGameTypeBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.util.setServiceClick


class GameTypeAdapter2(private val itemClick: (Item, Int) -> Unit) : BindingAdapter<Item, ItemGameTypeBinding>(),OnItemClickListener{

    init {
        setOnItemClickListener(this)
    }

    fun applyEventView(activity: FragmentActivity) {
        if (hasFooterLayout()) {
            return
        }
        val promotionBinding = ItemGameTypeBinding.inflate(LayoutInflater.from(activity),recyclerView,false)
        promotionBinding.ivSport.setImageResource(R.drawable.ic_sport_promotion)
        promotionBinding.root.setOnClickListener { PromotionListActivity.startFrom(context, "体育赛事列表页赛事类别栏") }
        addFooterView(promotionBinding.root, -1, LinearLayout.HORIZONTAL)
        val serviceBinding = ItemGameTypeBinding.inflate(LayoutInflater.from(activity),recyclerView,false)
        serviceBinding.ivSport.setImageResource(R.drawable.ic_sport_service)
        serviceBinding.root.setServiceClick(activity.supportFragmentManager)
        addFooterView(serviceBinding.root, -1, LinearLayout.HORIZONTAL)
    }


    var currentItem: Item? = null

    override fun onBinding(position: Int, binding: ItemGameTypeBinding, item: Item) {
        binding.ivSport.setImageResource(GameType.getHallGameTypeMenuIcon(item.code))
        binding.tvCount.text = "${item.num}"
        binding.root.setCardBackgroundColor(if(item.isSelected) ContextCompat.getColor(context,R.color.color_33025be8) else Color.TRANSPARENT)
    }

    override fun onBinding(
        position: Int,
        binding: ItemGameTypeBinding,
        item: Item,
        payloads: List<Any>
    ) {
        super.onBinding(position, binding, item, payloads)
        binding.root.setCardBackgroundColor(if(item.isSelected) ContextCompat.getColor(context,R.color.color_33025be8) else Color.TRANSPARENT)
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

    override fun setNewInstance(list: MutableList<Item>?) {
        recyclerViewOrNull?.isVisible = !list.isNullOrEmpty()
        currentItem = list?.find { it.isSelected }
        super.setNewInstance(list)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isVisible = getDefItemCount() > 0
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

