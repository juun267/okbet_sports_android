package org.cxct.sportlottery.ui.game.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_odd_btn_eps.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.QuickListManager

class OddButtonEpsAdapter(private val matchInfo: MatchInfo?) :
    RecyclerView.Adapter<OddButtonEpsViewHolder>() {

    var data: List<Odd?> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var listener: OddButtonListener? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.filterNotNull().find {
                    it.id == odd.id
                }))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonEpsViewHolder {
        return OddButtonEpsViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonEpsViewHolder, position: Int) {
        data[position]?.let {
            holder.bind(matchInfo, it, oddsType, listener)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OddButtonEpsViewHolder private constructor(
    itemView: View,
    override val oddStateChangeListener: OddStateChangeListener
) : OddStateViewHolder(itemView) {

    fun bind(
        matchInfo: MatchInfo?,
        odd: Odd,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?
    ) {
        itemView.quick_odd_eps_text1.apply {
            text = odd.name ?: ""
        }

        itemView.quick_odd_eps_btn1.apply {
            setupOddForEPS(odd, oddsType)

            this@OddButtonEpsViewHolder.setupOddState(this, odd)

            setOnClickListener {
                it.isSelected = !it.isSelected
                oddButtonListener?.onClickBet(this,matchInfo, odd, PlayCate.EPS.value)
            }

            isSelected = QuickListManager.getQuickSelectedList()?.contains(odd.id) ?: false
        }
    }

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonEpsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_odd_btn_eps, parent, false)

            return OddButtonEpsViewHolder(view, oddStateRefreshListener)
        }
    }
}