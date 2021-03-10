package org.cxct.sportlottery.ui.odds

import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.OddButtonHighLight

abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

    fun setData(odd: Odd, onOddClickListener: OnOddClickListener, betInfoList: MutableList<BetInfoListData>, curMatchId: String?) {

        OddButtonHighLight.set(tvOdds, odd)

        if(tvName!=null) {
            tvName.text = odd.name
        }

        odd.odds?.let { odds ->
            tvOdds.text = TextUtil.formatForOdd(odds)
        }

        when (odd.status) {
            BetStatus.ACTIVATED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.GONE
                tvOdds.isEnabled = true

                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                odd.isSelect = select

                tvOdds.isSelected = odd.isSelect ?: false
                tvOdds.setOnClickListener {
                    if (odd.isSelect != true) {
                        if (curMatchId != null && betInfoList.any { it.matchOdd.matchId == curMatchId }) {
                            return@setOnClickListener
                        }
                        onOddClickListener.getBetInfoList(odd)
                    } else {
                        Handler().postDelayed({//讓ripple效果呈現出來
                            onOddClickListener.removeBetInfoItem(odd)
                        }, 200)
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
}