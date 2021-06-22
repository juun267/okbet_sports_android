package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType


class TypeTwoSpanCountGridAdapter(
    private val oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeTwoSpanCountGridAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_2_span_count_item, parent, false))


    override fun getItemCount(): Int = oddsDetail.oddArrayList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsDetail.oddArrayList[position])


    inner class ViewHolder(view: View) : OddDetailStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)

        fun bindModel(odd: Odd) {
            btnOdds?.apply {
                setupOdd(odd, oddsType)
                setupOddState(this, odd)
                isSelected = betInfoList.any { it.matchOdd.oddsId == odd.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        odd.oddState = OddState.SAME.state
                    }
                }

                if (odd.name?.length ?: 0 > 8) {
                    tv_name.text = odd.name?.substring(0, 8).plus("...")
                }
            }

            itemView.setOnClickListener {
                onOddClickListener.getBetInfoList(odd, oddsDetail)
            }
        }
    }


}