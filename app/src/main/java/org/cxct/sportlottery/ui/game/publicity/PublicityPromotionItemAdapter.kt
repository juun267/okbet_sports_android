package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.PublicityPromotionItemBinding
import org.cxct.sportlottery.util.DisplayUtil.dp

class PublicityPromotionItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mPromotionList: List<PublicityPromotionItemData> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(promotionDataList: List<PublicityPromotionItemData>) {
        mPromotionList = promotionDataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PublicityPromotionItemViewHolder(
            PublicityPromotionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = mPromotionList[position]
        when (holder) {
            is PublicityPromotionItemViewHolder -> {
                holder.setupItemMargin(itemData)
                holder.bind(itemData)
            }
        }
    }

    override fun getItemCount(): Int = mPromotionList.size

    /**
     * 根據Item的位置配置不同的Margin
     */
    private fun PublicityPromotionItemViewHolder.setupItemMargin(promotionData: PublicityPromotionItemData) {
        val cvBlockLayoutParams = (binding.cvBlock.layoutParams as RecyclerView.LayoutParams)
        val marginVertical = 4.dp
        when (promotionData) {
            mPromotionList.firstOrNull() -> {
                cvBlockLayoutParams.setMargins(10.dp, marginVertical, 0, marginVertical)
            }
            mPromotionList.lastOrNull() -> {
                cvBlockLayoutParams.setMargins(8.dp, marginVertical, 10.dp, marginVertical)
            }
            else -> {
                cvBlockLayoutParams.setMargins(8.dp, marginVertical, 0, marginVertical)
            }
        }
    }
}