package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType

class OutrightOddMoreAdapter :
    RecyclerView.Adapter<OutrightOddMoreAdapter.OddViewHolder>() {

    var data: Pair<List<Odd?>?, MatchOdd>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                data?.first?.indexOf(data?.first?.find { data ->
                    data == odd
                })?.let { notifyItemChanged(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddViewHolder {
        return OddViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddViewHolder, position: Int) {
        val item = data?.first?.get(position)
        holder.bind(data?.second, item, outrightOddListener, oddsType)
    }

    override fun getItemCount(): Int {
        return data?.first?.size ?: 0
    }

    class OddViewHolder private constructor(
        itemView: View,
        private val refreshListener: OddStateChangeListener
    ) : OddStateViewHolder(itemView) {

        fun bind(
            matchOdd: MatchOdd?,
            item: Odd?,
            outrightOddListener: OutrightOddListener?,
            oddsType: OddsType
        ) {
            itemView.outright_odd_btn.apply {
                //特殊狀況 後續盤查原因
                val param = tv_name.layoutParams as LinearLayout.LayoutParams
                param.width = LinearLayout.LayoutParams.MATCH_PARENT
                tv_name.layoutParams = param
                setupOdd(item, oddsType)
                tv_spread.text = ""
                this@OddViewHolder.setupOddState(this, item)
                setOnClickListener {
                    item?.let { it1 -> outrightOddListener?.onClickBet(matchOdd, it1) }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): OddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_v4, parent, false)

                return OddViewHolder(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener
    }
}