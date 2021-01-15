package org.cxct.sportlottery.ui.odds

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

    fun setData(odd: Odd, onOddClickListener: OnOddClickListener, betInfoList: MutableList<BetInfoListData>) {

        tvName.text = odd.name
        tvOdds.text = odd.odds.toString()

        when (odd.status) {
            0 -> {

                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                odd.isSelect = select

                vCover.visibility = View.GONE
                tvOdds.isSelected = odd.isSelect

                tvOdds.setOnClickListener {
                    tvOdds.isSelected = !tvOdds.isSelected
                    odd.isSelect = tvOdds.isSelected
                    if (odd.isSelect) {
                        onOddClickListener.getBetInfoList(odd)
                    } else {
                        onOddClickListener.removeBetInfoItem(odd)
                    }
                }
            }

            1 -> vCover.visibility = View.VISIBLE

        }
    }

}