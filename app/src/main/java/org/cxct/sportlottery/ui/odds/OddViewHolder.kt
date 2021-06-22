package org.cxct.sportlottery.ui.odds

import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.OddButtonHighLight
import org.cxct.sportlottery.util.getOdds


const val BUTTON_SPREAD_TYPE_CENTER: Int = 0
const val BUTTON_SPREAD_TYPE_END: Int = 1
const val BUTTON_SPREAD_TYPE_BOTTOM: Int = 2


abstract class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    private val vCover = itemView.findViewById<View>(R.id.iv_disable_cover)
    private val tvName: TextView? = itemView.findViewById(R.id.tv_name)
    private val tvSpread = itemView.findViewById<TextView>(R.id.tv_spread)

    val tvOdds: TextView? = itemView.findViewById(R.id.tv_odds)

    var nameChangeColor: Boolean = true

    fun setData(
        oddsDetail: OddsDetailListData,
        odd: Odd,
        onOddClickListener: OnOddClickListener,
        betInfoList: MutableList<BetInfoListData>,
        spreadType: Int,
        oddsType: OddsType
    ) {

        oddsDetail.gameType.let { type ->
            when {
                TextUtil.compareWithGameKey(type, OddsDetailListAdapter.GameType.HDP.value) -> showName(false)
            }
        }

        setOdds(odd, oddsType)
        setName(odd)
        setSpread(odd, spreadType)

        tvOdds?.let {
            OddButtonHighLight.set(
                nameChangeColor,
                tvName,
                it,
                tvSpread,
                odd,
                object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        odd.oddState = OddState.SAME.state
                    }
                })
        }

        when (odd.status) {
            BetStatus.ACTIVATED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.GONE
                tvOdds?.isEnabled = true

                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }

                odd.isSelect = select

                tvOdds?.isSelected = odd.isSelect ?: false
                tvSpread?.let { it.isSelected = odd.isSelect ?: false }
                tvName?.let { it.isSelected = odd.isSelect ?: false }
                itemView.setOnClickListener {
                    onOddClickListener.getBetInfoList(odd, oddsDetail)
                }

            }

            BetStatus.LOCKED.code -> {
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.VISIBLE
                tvOdds?.isEnabled = false
            }


            BetStatus.DEACTIVATED.code -> {
                //比照h5照樣顯示（文件為不顯示）
                itemView.visibility = View.VISIBLE
                vCover.visibility = View.VISIBLE
                tvOdds?.isEnabled = false
            }

        }
    }


    fun showName(visible: Boolean) {
        if (tvName != null) {
            tvName.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }


    private fun setOdds(odd: Odd, oddsType: OddsType) {
        tvOdds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
    }


    private fun setSpread(odd: Odd, spreadType: Int) {
        if (tvSpread != null && !odd.spread.isNullOrEmpty()) {
            tvSpread.text = odd.spread
        }

        when (spreadType) {
            BUTTON_SPREAD_TYPE_CENTER -> {
                tvOdds?.gravity = Gravity.CENTER
                tvOdds?.setPadding(0, 0, 0, 0)
            }
            BUTTON_SPREAD_TYPE_END -> {
                tvOdds?.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            BUTTON_SPREAD_TYPE_BOTTOM -> {
                tvOdds?.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }
        }
    }


    private fun setName(odd: Odd) {
        if (tvName != null && !odd.name.isNullOrEmpty()) {
            tvName.text = odd.name
        }
    }


}