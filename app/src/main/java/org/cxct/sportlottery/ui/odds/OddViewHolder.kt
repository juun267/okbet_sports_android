package org.cxct.sportlottery.ui.odds

import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.OddButtonHighLight


const val BUTTON_SPREAD_TYPE_CENTER: Int = 0
const val BUTTON_SPREAD_TYPE_END: Int = 1
const val BUTTON_SPREAD_TYPE_BOTTOM: Int = 2


abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    private val rlOddItem = itemView.findViewById<RelativeLayout>(R.id.rl_odd_item)
    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
    private val tvSpread = itemView.findViewById<TextView>(R.id.tv_spread)


    fun setData(odd: Odd, onOddClickListener: OnOddClickListener, betInfoList: MutableList<BetInfoListData>, curMatchId: String?, spreadType: Int) {

        if (rlOddItem != null) rlOddItem.visibility = if (odd.itemViewVisible) View.VISIBLE else View.GONE

        setData(odd)

        OddButtonHighLight.set(tvOdds, tvSpread, odd)

        when (spreadType) {
            BUTTON_SPREAD_TYPE_CENTER -> {
                tvOdds.gravity = Gravity.CENTER
                tvOdds.setPadding(0, 0, 0, 0)
            }
            BUTTON_SPREAD_TYPE_END -> {
                tvOdds.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            BUTTON_SPREAD_TYPE_BOTTOM -> {
                tvOdds.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }
        }

        when (odd.status) {
            BetStatus.ACTIVATED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.GONE
                tvOdds.isEnabled = true

                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }

                odd.isSelect = select

                tvOdds.isSelected = odd.isSelect ?: false
                tvSpread?.let { it.isSelected = odd.isSelect ?: false }
                tvOdds.setOnClickListener {
                    if (odd.isSelect != true) {
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
                //比照h5照樣顯示（文件為不顯示）
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.VISIBLE
                tvOdds.isEnabled = false
            }

        }
    }


    private fun setData(odd: Odd) {
        if (tvSpread != null && !odd.spread.isNullOrEmpty()) {
            tvSpread.text = odd.spread
        }

        if (tvName != null && !odd.name.isNullOrEmpty()) {
            tvName.text = odd.name
        }

        odd.odds?.let { odds ->
            tvOdds.text = TextUtil.formatForOdd(odds)
        }
    }


}