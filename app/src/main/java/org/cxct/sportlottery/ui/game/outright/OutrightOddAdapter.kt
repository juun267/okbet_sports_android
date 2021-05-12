package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitlev3.view.*
import kotlinx.android.synthetic.main.itemview_outright_oddv3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil

class OutrightOddAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        SUB_TITLE, ODD
    }

    var matchOdd: MatchOdd? = null
        set(value) {
            field = value
            field?.let {
                data = it.displayList
            }
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
                notifyItemChanged(data.indexOf(data.firstOrNull { data ->
                    if (data is Odd)
                        data == odd
                    else false
                }))
            }
        }
    }

    private var data = listOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is Odd -> ItemType.ODD.ordinal
            else -> ItemType.SUB_TITLE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.SUB_TITLE.ordinal -> SubTitleViewHolder.from(parent)
            else -> OddViewHolder.from(parent, oddStateRefreshListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SubTitleViewHolder -> {
                val item = data[position] as String
                holder.bind(item)
            }
            is OddViewHolder -> {
                val item = data[position] as Odd
                holder.bind(matchOdd, item, outrightOddListener, oddsType)
            }
        }

    }

    override fun getItemCount(): Int = data.size

    class OddViewHolder private constructor(itemView: View, private val refreshListener: OddStateChangeListener) : OddStateViewHolder(itemView) {

        fun bind(
            matchOdd: MatchOdd?,
            item: Odd,
            outrightOddListener: OutrightOddListener?,
            oddsType: OddsType
        ) {
            itemView.outright_odd_name.text = item.spread

            itemView.outright_odd_btn.apply {

                onOddStatusChangedListener = object : OddButton.OnOddStatusChangedListener {
                    override fun onOddStateChangedFinish() {
                        item.oddState = OddState.SAME.state
                    }
                }
                playType = PlayType.OUTRIGHT

                visibility = if (item.odds == null) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                isSelected = item.isSelected ?: false

                betStatus = item.status

                /*oddStatus = item.oddState*/
                this@OddViewHolder.setupOddState(this, item)

                odd_outright_text.text = when (oddsType) {
                    OddsType.EU -> {
                        item.odds?.let { TextUtil.formatForOdd(it) }
                    }
                    OddsType.HK -> {
                        item.hkOdds?.let { TextUtil.formatForOdd(it) }
                    }
                }

                setOnClickListener {
                    outrightOddListener?.onClickBet(matchOdd, item)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): OddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_oddv3, parent, false)

                return OddViewHolder(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener
    }

    class SubTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: String) {
            itemView.outright_odd_subtitle.text = item
        }

        companion object {
            fun from(parent: ViewGroup): SubTitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_subtitlev3, parent, false)

                return SubTitleViewHolder(view)
            }
        }
    }
}

class OutrightOddListener(val clickListenerBet: (matchOdd: MatchOdd?, odd: Odd) -> Unit) {
    fun onClickBet(matchOdd: MatchOdd?, odd: Odd) = clickListenerBet(matchOdd, odd)
}