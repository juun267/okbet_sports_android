package org.cxct.sportlottery.ui.odds

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.detail.recycle.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.game.widget.OddsButtonDetail
import org.cxct.sportlottery.ui.menu.OddsType

@SuppressLint("NotifyDataSetChanged")
class TypeOneListAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType,
    private val isOddPercentage: Boolean? = false,
    private val onMoreClickListener: OnMoreClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsDetail.needShowItem.indexOf(oddsDetail.needShowItem.find { o -> o == odd }))
            }
        }
    }

    var mOddsDetail: OddsDetailListData? = null
        set(value) {
            field = value
            oddsDetail = value as OddsDetailListData
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (onMoreClickListener) {
            null -> return ItemType.ITEM.ordinal
            else -> when (position) {
                oddsDetail.needShowItem.size -> ItemType.MORE.ordinal
                else -> ItemType.ITEM.ordinal
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ItemType.ITEM.ordinal -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_one_list_item, parent, false))
            else -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_more_item, parent, false))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is ViewHolder -> holder.bindModel(oddsDetail.needShowItem[position])
            is MoreViewHolder -> {
                holder.bind()
            }
            else -> {
            }
        }

    override fun getItemCount(): Int {
        return if (onMoreClickListener == null)
            oddsDetail.needShowItem.size
        else oddsDetail.needShowItem.size + 1
    }

    inner class ViewHolder(view: View) : OddStateViewHolderDetail(view) {

        private val btnOdds = itemView.findViewById<OddsButtonDetail>(R.id.button_odds)

        fun bindModel(odd: Odd?) {
            btnOdds?.apply {
                setupOdd(odd, oddsType, isOddPercentage = isOddPercentage)
                setupOddState(this, odd)
                setOnClickListener {
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener
    }

    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpandControl: TextView = itemView.findViewById(R.id.tv_expand_control)
        fun bind() {
            tvExpandControl.apply {
                setOnClickListener {
                    onMoreClickListener?.click()
                }
                text = if (oddsDetail.isMoreExpand) context.getString(R.string.odds_detail_less) else context.getString(R.string.display_more)
            }
        }
    }

    interface OnMoreClickListener {
        fun click()
    }

    enum class ItemType {
        ITEM, MORE
    }

}