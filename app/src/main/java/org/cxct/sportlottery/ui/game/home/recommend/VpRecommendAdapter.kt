package org.cxct.sportlottery.ui.game.home.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType

class VpRecommendAdapter(
    val dataList: List<OddBean>,
    val oddsType: OddsType,
    val matchOdd: MatchOdd
) : RecyclerView.Adapter<VpRecommendAdapter.ViewHolderHdpOu>() {

    var onClickOddListener: OnClickOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recommend_vp, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolderHdpOu(itemView: View) : OddDetailStateViewHolder(itemView) {

        //TODO simon test 賽事推薦 賠率串接
        fun bind(data: OddBean) {
            val playKey = data.oddCode
            itemView.apply {
                tv_play_type.text = playKey

                data.oddList.forEachIndexed { index, odd ->
                    when (index) {
                        0 -> setupOddButton(button_odds_left, odd)
                        1 -> setupOddButton(button_odds_middle, odd)
                        2 -> setupOddButton(button_odds_right, odd)
                    }
                }

            }

        }


        private fun setupOddButton(oddButton: OddsButton, odd: Odd) {
            oddButton.apply {
                val detailOdd = odd.toDetailOdd()
                setupOdd(detailOdd, oddsType)
                setupOddState(this, detailOdd)
                isSelected = odd.isSelected ?: false
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: org.cxct.sportlottery.network.odds.detail.Odd) {
                        dataList.forEachIndexed { index, oddBean ->
                            if (oddBean.oddList.find { it.id == odd.id } != null)
                                notifyItemChanged(index)
                        }
                    }
                }

                setOnClickListener {
                    val playCateName = itemView.tv_play_type.text.toString()
                    val playName = odd.name ?: "" //TODO simon test review 不確定 playName 能否使用 name 參數
                    onClickOddListener?.onClickBet(matchOdd, odd, playCateName, playName)
                }
            }

        }

    }

}


//TODO simon test review Odd 資料轉換
fun Odd.toDetailOdd(): org.cxct.sportlottery.network.odds.detail.Odd {
    return org.cxct.sportlottery.network.odds.detail.Odd(
        extInfo = extInfo,
        id = id,
        name = name,
        odds = odds,
        hkOdds = hkOdds,
        producerId = producerId,
        spread = spread,
        status = status
    )
}