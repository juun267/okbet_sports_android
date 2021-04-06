package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData


const val MORE_ITEM = 1

const val OVER_COUNT = 5

class TypeOneListAdapter(
    private val oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val curMatchId: String?,
    private val isSCO: Boolean,
    private val onMoreClickListener: OnMoreClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    enum class ItemType {
        ITEM, MORE
    }


    private val oddsList = oddsDetail.oddArrayList


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            (oddsList.size) -> {
                ItemType.MORE.ordinal
            }
            else -> ItemType.ITEM.ordinal
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_one_list_item, parent, false))
            else -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_one_list_more_item, parent, false))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bindModel(oddsList[position])
            is MoreViewHolder -> holder.bind(isSCO)
        }
    }


    override fun getItemCount(): Int {
        return oddsList.size + MORE_ITEM
    }


    inner class ViewHolder(view: View) : OddViewHolder(view) {
        fun bindModel(originOdd: Odd) {
            setData(originOdd, onOddClickListener, betInfoList, curMatchId,
                if (originOdd.spread.isNullOrEmpty()) BUTTON_SPREAD_TYPE_CENTER else BUTTON_SPREAD_TYPE_BOTTOM
            )
        }
    }


    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCountControl: TextView = itemView.findViewById(R.id.tv_count_control)
        fun bind(isSCO: Boolean) {
            tvCountControl.apply {
                setOnClickListener {
                    onMoreClickListener.click()
                }
                visibility = if (isSCO && oddsList.size > OVER_COUNT) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                text = if (oddsDetail.isMoreExpand) context.getString(R.string.odds_detail_less) else context.getString(R.string.odds_detail_more)
            }
        }
    }


    interface OnMoreClickListener {
        fun click()
    }


}