package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType


class TypeSingleAdapter (
    private val oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeSingleAdapter.ViewHolder>() {


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_grid_item, parent, false))


    override fun getItemCount(): Int = oddsDetail.oddArrayList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsDetail.gameType, oddsDetail.oddArrayList[position])


    inner class ViewHolder(view: View) : OddStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)

        fun bindModel(gameType: String?, odd: Odd?) {
            btnOdds?.apply {
                setupOdd(odd, oddsType, gameType)
                setupOddState(this, odd)
                setOnClickListener {
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}