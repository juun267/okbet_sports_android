package org.cxct.sportlottery.ui.odds

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val PLAYING = 0
        const val LOCK = 1
    }

    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

    fun setData(odd: Odd, onOddClickListener: OnOddClickListener, betInfoList: MutableList<BetInfoListData>) {

        tvName.text = odd.name
        tvOdds.text = odd.odds.toString()

        when (odd.status) {
            PLAYING -> {
                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                odd.isSelect = select

                vCover.visibility = View.GONE
                tvOdds.isSelected = odd.isSelect
                tvOdds.setOnClickListener {
                    if (!odd.isSelect) {
                        onOddClickListener.getBetInfoList(odd)
                    } else {
                        onOddClickListener.removeBetInfoItem(odd)
                    }
                }
            }

            LOCK -> vCover.visibility = View.VISIBLE

        }
    }

}