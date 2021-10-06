package org.cxct.sportlottery.ui.vip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_third_rebates_form.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.vip.thirdRebates.Debate
import org.cxct.sportlottery.util.TextUtil

class ThirdRebatesAdapter2 : RecyclerView.Adapter<ItemViewHolder>() {

    var dataList = listOf<Debate>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.form(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}

class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(itemData: Debate) {
        itemView.apply {

            if (itemData.isNullTail) {
                view_no_data.isVisible = true
                ll_content.isVisible = false
            }

            if (itemData.isLevelTail) {
                view_no_data.isVisible = false
                ll_content.isVisible = true
            }

            val vipLevel = Level.values().find { it.levelRequirement.levelId == itemData.userLevelId }?.levelRequirement?.level?.let { context.getString(it) } ?: ""
            tv_level.text = "$vipLevel ${itemData.userLevelName}"

            val isNullToSetUnlimited = { context: Context, value: Double? ->
                (value?.toLong() ?: context.getString(R.string.unlimited)).toString()
            }

            tv_bet_min.text = isNullToSetUnlimited(context, itemData.minMoney)
            tv_bet_max.text = isNullToSetUnlimited(context, itemData.maxMoney)
            tv_rebates_rate.text = "${TextUtil.formatForVipRebates(itemData.debate ?: 0.0)}"
            tv_rebates.text = isNullToSetUnlimited(context, itemData.maxDebateMoney)

            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    ContextCompat.getColorStateList(context, R.color.colorWhite1)
                } else {
                    ContextCompat.getColorStateList(context, R.color.colorWhite3)
                }

            backgroundColor.let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }
        }
    }

    companion object {
        fun form(viewGroup: ViewGroup): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_third_rebates_form, viewGroup, false)
            return ItemViewHolder(view)
        }
    }

}