package org.cxct.sportlottery.ui.odds

import android.content.res.ColorStateList
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.outright.CHANGING_ITEM_BG_COLOR_DURATION

abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

    fun setData(odd: Odd, onOddClickListener: OnOddClickListener, betInfoList: MutableList<BetInfoListData>, curMatchId: String?) {

        setHighlight(tvOdds, odd.oddState)

        tvName.text = odd.name
        tvOdds.text = odd.odds.toString()

        when (odd.status) {
            BetStatus.ACTIVATED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.GONE
                tvOdds.isEnabled = true

                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                odd.isSelect = select

                tvOdds.isSelected = odd.isSelect
                tvOdds.setOnClickListener {
                    if (!odd.isSelect) {
                        if (curMatchId != null && betInfoList.any { it.matchOdd.matchId == curMatchId }) {
                            return@setOnClickListener
                        }
                        onOddClickListener.getBetInfoList(odd)
                    } else {
                        onOddClickListener.removeBetInfoItem(odd)
                    }
                }
            }

            BetStatus.LOCKED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.VISIBLE
                tvOdds.isEnabled = false
            }


            BetStatus.DEACTIVATED.code -> {
                itemView.visibility = View.GONE
                vCover.visibility = View.GONE
                tvOdds.isEnabled = false
            }

        }
    }

    private fun setHighlight(textView: TextView, status: Int) {
        when (status) {
            OddState.LARGER.state -> textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(textView.context, R.color.green))
            OddState.SMALLER.state -> textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(textView.context, R.color.red))
        }

        Handler().postDelayed(
            {
                textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(textView.context, R.color.light_gray))
            }, CHANGING_ITEM_BG_COLOR_DURATION
        )
    }

}