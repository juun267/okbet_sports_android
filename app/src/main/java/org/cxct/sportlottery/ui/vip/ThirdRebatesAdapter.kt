package org.cxct.sportlottery.ui.vip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_third_rebates_form_content.view.ll_bet_range
import kotlinx.android.synthetic.main.item_third_rebates_form_content.view.tv_bet_max
import kotlinx.android.synthetic.main.item_third_rebates_form_content.view.tv_bet_min
import kotlinx.android.synthetic.main.item_third_rebates_form_content.view.tv_rebates
import kotlinx.android.synthetic.main.item_third_rebates_form_content.view.tv_rebates_rate
import kotlinx.android.synthetic.main.item_third_rebates_form_content_last.view.*
import kotlinx.android.synthetic.main.item_third_rebates_form_title.view.title_top_center
import kotlinx.android.synthetic.main.item_third_rebates_form_title.view.title_top_left
import kotlinx.android.synthetic.main.item_third_rebates_form_title.view.title_top_right
import kotlinx.android.synthetic.main.item_third_rebates_form_title.view.tv_level
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.vip.thirdRebates.Debate
import org.cxct.sportlottery.util.TextUtil

class ThirdRebatesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType { TITLE, CONTENT, LAST_CONTENT, NULL }

    var dataList = listOf<Debate>()
        set(debateList) {
            field = debateList
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.TITLE.ordinal -> TitleViewHolder.form(parent)
            ItemType.CONTENT.ordinal -> ContentViewHolder.form(parent)
            ItemType.LAST_CONTENT.ordinal -> LastContentViewHolder.form(parent)
            ItemType.NULL.ordinal -> NullViewHolder.form(parent)
            else -> ContentViewHolder.form(parent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataList[position].isTitle -> ItemType.TITLE.ordinal
            dataList[position].isLastContent -> ItemType.LAST_CONTENT.ordinal
            dataList[position].isNullTail -> ItemType.NULL.ordinal
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
            is LastContentViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
            is NullViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
        }
    }
}

class TitleViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_third_rebates_form_title, viewGroup, false)
            return TitleViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            ContextCompat.getColorStateList(context, R.color.colorWhite7).let {
                title_top_left.backgroundTintList = it
                title_top_center.backgroundTintList = it
                title_top_right.backgroundTintList = it
            }

            itemData.apply {
                val vipLevel = Level.values().find { it.levelRequirement.levelId == userLevelId }?.levelRequirement?.level?.let { context.getString(it) } ?: ""
                tv_level.text = "$vipLevel $userLevelName"
            }
        }
    }
}

class ContentViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_third_rebates_form_content, viewGroup, false)
            return ContentViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {

            val isNullToSetUnlimited = { context: Context, value: Double? ->
                (value?.toLong() ?: context.getString(R.string.unlimited)).toString()
            }

            tv_bet_min.text = isNullToSetUnlimited(context, itemData.minMoney)
            tv_bet_max.text = isNullToSetUnlimited(context, itemData.maxMoney)
            tv_rebates_rate.text = "${TextUtil.formatForVipRebates(itemData.debate ?: 0.0)}"
            tv_rebates.text = isNullToSetUnlimited(context, itemData.maxDebateMoney)

            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    ContextCompat.getColorStateList(context, R.color.color_262626_edf4ff)
                } else {
                    ContextCompat.getColorStateList(context, R.color.color_141414_F3F4F5)
                }

            backgroundColor.let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }
        }
    }
}

class LastContentViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_third_rebates_form_content_last, viewGroup, false)
            return LastContentViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {

            val isNullToSetUnlimited = { context: Context, value: Double? ->
                (value?.toLong() ?: context.getString(R.string.unlimited)).toString()
            }

            tv_bet_min2.text = isNullToSetUnlimited(context, itemData.minMoney)
            tv_bet_max2.text = isNullToSetUnlimited(context, itemData.maxMoney)
            tv_rebates_rate2.text = "${TextUtil.formatForVipRebates(itemData.debate ?: 0.0)}"
            tv_rebates2.text = isNullToSetUnlimited(context, itemData.maxDebateMoney)

            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    R.color.color_262626_edf4ff
                } else {
                    R.color.color_141414_F3F4F5
                }

            backgroundColor.let {
                ll_bet_range2.backgroundTintList = ContextCompat.getColorStateList(context, it)
                tv_rebates_rate2.setBackgroundColor(ContextCompat.getColor(context, it))
                tv_rebates2.backgroundTintList = ContextCompat.getColorStateList(context, it)
            }
        }
    }
}

class NullViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_tail_third_rebates_no_data, viewGroup, false)
            return NullViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            itemData.apply {
                val vipLevel = Level.values().find { it.levelRequirement.levelId == userLevelId }?.levelRequirement?.level?.let { context.getString(it) } ?: ""
                tv_level.text = "$vipLevel $userLevelName"
            }

        }
    }
}