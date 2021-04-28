package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp


class TypeGroupItemAdapter(
    private val oddsDetail: OddsDetailListData,
    private val groupItemCount: Int,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) :
    RecyclerView.Adapter<TypeGroupItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_group_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsDetail.oddArrayList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsDetail.oddArrayList[position], position)
    }


    inner class ViewHolder(view: View) : OddViewHolder(view) {

        private val tvSpread = itemView.findViewById<TextView>(R.id.tv_spread)
        private val rlContent = itemView.findViewById<RelativeLayout>(R.id.rl_content)

        fun bindModel(odd: Odd, position: Int) {

            setData(oddsDetail, odd, onOddClickListener, betInfoList, BUTTON_SPREAD_TYPE_BOTTOM, oddsType, null)

            tvSpread.text = odd.spread

            val rlParams: RelativeLayout.LayoutParams = rlContent.layoutParams as RelativeLayout.LayoutParams

            if (groupItemCount == 2) {
                if (position % groupItemCount != 0) {

                    if (position != oddsDetail.oddArrayList.size - 1) {
                        val params: RecyclerView.LayoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                        params.setMargins(0, 0, 0, 2)
                        itemView.layoutParams = params
                    }
                    rlParams.setMargins(0, 5.dp, 0, 10.dp)
                } else {
                    rlParams.setMargins(0, 10.dp, 0, 5.dp)
                }
            }

            if (groupItemCount == 3) {
                when {
                    position % groupItemCount == 2 -> {
                        if (position != oddsDetail.oddArrayList.size - 1) {
                            val params: RecyclerView.LayoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                            params.setMargins(0, 0, 0, 2)
                            itemView.layoutParams = params
                        }
                        rlParams.setMargins(0, 5.dp, 0, 10.dp)
                    }
                    position % groupItemCount == 1 -> rlParams.setMargins(0, 5.dp, 0, 5.dp)
                    position % groupItemCount == 0 -> rlParams.setMargins(0, 10.dp, 0, 5.dp)
                }
            }

            rlContent.layoutParams = rlParams

        }
    }


}