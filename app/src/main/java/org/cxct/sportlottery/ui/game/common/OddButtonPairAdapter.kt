package org.cxct.sportlottery.ui.game.common

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_odd_btn_pair.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.QuickListManager

class OddButtonPairAdapter(private val matchInfo: MatchInfo?) :
    RecyclerView.Adapter<OddButtonPairViewHolder>() {

    var odds: List<Odd?> = listOf()
        set(value) {
            field = value.sortedBy { it?.marketSort }
            field.forEach {
                if(QuickListManager.getQuickSelectedList()?.contains(it?.id) == true){
                    it?.isSelected = true
                }
            }
            data = field.withIndex().groupBy {
                it.index / 2
            }
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                //notifyDataSetChanged()
            }
        }

    var listener: OddButtonListener? = null

    private var data: Map<Int, List<IndexedValue<Odd?>>> = mapOf()
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                data.forEach { oddMap ->
                    val oddIdList = oddMap.value.mapNotNull {
                        it.value?.id
                    }

                    if (oddIdList.contains(odd.id)) {
                        notifyItemChanged(oddMap.key)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPairViewHolder {
        return OddButtonPairViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonPairViewHolder, position: Int) {
        Log.d("Hewie", "綁定：快選列表($position)")
        data[position]?.let {
            holder.bind(matchInfo, it, oddsType, listener)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OddButtonPairViewHolder private constructor(
    itemView: View,
    override val oddStateChangeListener: OddStateChangeListener
) : OddStateViewHolder(itemView) {

    fun bind(
        matchInfo: MatchInfo?,
        oddPair: List<IndexedValue<Odd?>>,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?
    ) {
        itemView.quick_odd_btn_pair_one_1.apply pair1ButtonSettings@{
            if (oddPair.size < 2) return@pair1ButtonSettings

            setupOdd(oddPair.getOrNull(0)?.value, oddsType)

            this@OddButtonPairViewHolder.setupOddState(this, oddPair.getOrNull(0)?.value)

            isSelected = QuickListManager.getQuickSelectedList()?.contains( oddPair.getOrNull(0)?.value?.id) ?: false

            setOnClickListener {
                oddPair.getOrNull(0)?.value?.let { odd ->
                    it.isSelected = !it.isSelected
                    oddButtonListener?.onClickBet(this,matchInfo, odd, odd.playCode ?: "")
                }
            }
        }

        itemView.quick_odd_btn_pair_one_2.apply pair2ButtonSettings@{
            if (oddPair.size < 2) return@pair2ButtonSettings

            setupOdd(oddPair.getOrNull(1)?.value, oddsType)

            this@OddButtonPairViewHolder.setupOddState(this, oddPair.getOrNull(1)?.value)

            isSelected = QuickListManager.getQuickSelectedList()?.contains( oddPair.getOrNull(1)?.value?.id) ?: false

            setOnClickListener {
                oddPair.getOrNull(1)?.value?.let { odd ->
                    it.isSelected = !it.isSelected
                    oddButtonListener?.onClickBet(this,matchInfo, odd, odd.playCode ?: "")
                }
            }
        }
    }

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonPairViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_odd_btn_pair, parent, false)

            return OddButtonPairViewHolder(view, oddStateRefreshListener)
        }
    }
}