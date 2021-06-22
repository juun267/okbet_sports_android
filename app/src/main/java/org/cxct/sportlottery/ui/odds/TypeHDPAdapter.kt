package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType


class TypeHDPAdapter(
    private val oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeHDPAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_hdp_item, parent, false))


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
            }

            itemView.setOnClickListener {
                onOddClickListener.getBetInfoList(odd, oddsDetail)
            }
        }
    }


}