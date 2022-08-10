package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddButtonListener
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.QuickListManager


class TypeLCSAdapter(
    private val matchInfo: MatchInfo?,
    private val oddsList: List<Odd?>,
    private val oddsType: OddsType,
    private val isOddPercentage: Boolean? = false
) : RecyclerView.Adapter<TypeLCSAdapter.ViewHolder>() {

    var listener: OddButtonListener? = null

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsList.indexOf(oddsList.find { o -> o == odd }))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_grid_item_lcs, parent, false))


    override fun getItemCount(): Int = oddsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsList[position], listener)


    inner class ViewHolder(view: View) : OddStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)

        fun bindModel(odd: Odd?, listener: OddButtonListener?) {
            btnOdds?.apply {
                setupOdd(odd, oddsType, isOddPercentage = isOddPercentage)
                setupOddState(this, odd)
                setOnClickListener {
                    odd?.let { o ->
                        it.isSelected = !it.isSelected
                        listener?.onClickBet(matchInfo, o, PlayCate.LCS.value)
                    }
                }
                isSelected = QuickListManager.getQuickSelectedList()?.contains(odd?.id) ?: false
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}