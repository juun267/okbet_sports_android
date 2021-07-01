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


class TypeCSAdapter(
    private val oddsDetail: OddsDetailListData,
    private val oddsList: List<Odd?>,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeCSAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_grid_item, parent, false))


    override fun getItemCount(): Int = oddsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsList[position])


    inner class ViewHolder(view: View) : OddDetailStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)

        fun bindModel(odd: Odd?) {
            btnOdds?.apply {
                setupOdd(odd, oddsType)
                setupOddState(this, odd)
                isSelected = betInfoList.any { it.matchOdd.oddsId == odd?.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
                    }
                }

                if (odd?.name?.length ?: 0 > 8) {
                    tv_name.text = odd?.name?.substring(0, 8).plus("...")
                }
            }

            itemView.setOnClickListener {
                odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
            }
        }
    }


}