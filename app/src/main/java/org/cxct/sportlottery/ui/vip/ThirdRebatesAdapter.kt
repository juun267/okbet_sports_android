package org.cxct.sportlottery.ui.vip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_title_third_rebates_form.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.vip.thirdRebates.Debate

class ThirdRebatesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType { TITLE, CONTENT, TAIL }

    var dataList = listOf<Debate>()
        set(debateList) {
            field = debateList
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.TITLE.ordinal -> TitleViewHolder.form(parent)
            ItemType.TAIL.ordinal -> TailViewHolder.form(parent)
            else -> ContentViewHolder.form(parent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataList[position].isTitle -> ItemType.TITLE.ordinal
            dataList[position].isLevelTail -> ItemType.TAIL.ordinal
            else -> ItemType.CONTENT.ordinal
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        when (holder) {
            is TitleViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
            is ContentViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
            is TailViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
        }
        commonBindView(holder, holder.itemView, data)
    }

    private fun commonBindView(holder: RecyclerView.ViewHolder, itemView: View, data: Debate) {
        holder.apply {
            itemView.apply {
                tv_bet_min.text = isNullToSetUnlimited(context, data.minMoney)
                tv_bet_max.text = isNullToSetUnlimited(context, data.maxMoney)
                tv_rebates_rate.text = "${data.debate} ${context.getString(R.string.percent)}"
                tv_rebates.text = isNullToSetUnlimited(context, data.maxDebateMoney)
            }
        }
    }

    private fun isNullToSetUnlimited(context: Context, value: Double?): String {
        return (value?.toLong() ?: context.getString(R.string.unlimited)).toString()
    }
}

class TitleViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_title_third_rebates_form, viewGroup, false)
            return TitleViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            ContextCompat.getColorStateList(context, R.color.form_background_title).let {
                title_top_left.backgroundTintList = it
                title_top_center.backgroundTintList = it
                title_top_right.backgroundTintList = it
            }
            ContextCompat.getColorStateList(context, R.color.form_background_even).let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }

            itemData.apply {
                val vipLevel = Level.values().find { it.levelRequirement.levelId == userLevelId }?.levelRequirement?.level?.let { context.getString(it) } ?: ""
                tv_level.text = "$vipLevel$userLevelName"
            }
        }
    }
}

class ContentViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_content_third_rebates_form, viewGroup, false)
            return ContentViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    ContextCompat.getColorStateList(context, R.color.form_background_even)
                } else {
                    ContextCompat.getColorStateList(context, R.color.form_background_odd)
                }

            backgroundColor.let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }
        }
    }
}

class TailViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_tail_third_rebates_form, viewGroup, false)
            return TailViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    ContextCompat.getColorStateList(context, R.color.form_background_even)
                } else {
                    ContextCompat.getColorStateList(context, R.color.form_background_odd)
                }
            backgroundColor.let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }
        }
    }
}