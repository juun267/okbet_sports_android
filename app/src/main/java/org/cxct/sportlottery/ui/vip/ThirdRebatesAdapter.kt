package org.cxct.sportlottery.ui.vip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_title_third_rebates_form.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.vip.thirdRebates.Debate
import org.cxct.sportlottery.ui.vip.ThirdRebatesAdapter.Companion.FORM_EVEN_BACKGROUND_RES
import org.cxct.sportlottery.ui.vip.ThirdRebatesAdapter.Companion.FORM_ODD_BACKGROUND_RES
import org.cxct.sportlottery.ui.vip.ThirdRebatesAdapter.Companion.FORM_TITLE_BACKGROUND_RES
import org.cxct.sportlottery.util.TextUtil

class ThirdRebatesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        @ColorRes
        const val FORM_TITLE_BACKGROUND_RES = R.color.colorWhite7

        @ColorRes
        const val FORM_EVEN_BACKGROUND_RES = R.color.colorWhite1

        @ColorRes
        const val FORM_ODD_BACKGROUND_RES = R.color.colorWhite3
    }

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
                tv_rebates_rate.text = "${TextUtil.formatForVipRebates(data.debate ?: 0.0)}"
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
            ContextCompat.getColorStateList(context, FORM_TITLE_BACKGROUND_RES).let {
                title_top_left.backgroundTintList = it
                title_top_center.backgroundTintList = it
                title_top_right.backgroundTintList = it
            }
            ContextCompat.getColorStateList(context, FORM_EVEN_BACKGROUND_RES).let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
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
            val view = layoutInflater.inflate(R.layout.item_content_third_rebates_form, viewGroup, false)
            return ContentViewHolder(view)
        }
    }

    fun bind(itemData: Debate) {
        itemView.apply {
            val backgroundColor =
                if (itemData.levelIndex % 2 == 0) {
                    ContextCompat.getColorStateList(context, FORM_EVEN_BACKGROUND_RES)
                } else {
                    ContextCompat.getColorStateList(context, FORM_ODD_BACKGROUND_RES)
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
                    ContextCompat.getColorStateList(context, FORM_EVEN_BACKGROUND_RES)
                } else {
                    ContextCompat.getColorStateList(context, FORM_ODD_BACKGROUND_RES)
                }
            backgroundColor.let {
                ll_bet_range.backgroundTintList = it
                tv_rebates_rate.backgroundTintList = it
                tv_rebates.backgroundTintList = it
            }
        }
    }
}