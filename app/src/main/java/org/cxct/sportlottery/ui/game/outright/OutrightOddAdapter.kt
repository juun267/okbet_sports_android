package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitle_v4.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager

class OutrightOddAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        SUB_TITLE, ODD, MORE
    }

    var matchOdd: MatchOdd? = null
        set(value) {
            field = value
            field?.let { matchOdd ->
                val list = mutableListOf<Any>()
                matchOdd.odds.forEach {
                    list.add(it.key)

                    list.addAll(
                        it.value.filterNotNull()
                            .filterIndexed { index, _ -> index < 4 }
                            .map { odd ->
                                odd.outrightCateKey = it.key
                                odd
                            })


                    if (it.value.filterNotNull().size > 4) {
                        list.add(it.key to matchOdd)
                    }
                }
                data = list
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
        return when (data.getOrNull(position)) {
            is Odd -> ItemType.ODD.ordinal
            is String -> ItemType.SUB_TITLE.ordinal
            else -> ItemType.MORE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.SUB_TITLE.ordinal -> SubTitleViewHolder.from(parent)
            ItemType.ODD.ordinal -> OddViewHolder.from(parent, oddStateRefreshListener)
            else -> MoreViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SubTitleViewHolder -> {
                val item = data[position] as String
                holder.bind(item, matchOdd?.dynamicMarkets)
            }
            is OddViewHolder -> {
                val item = data[position] as Odd
                holder.bind(matchOdd, item, outrightOddListener, oddsType)
            }
            is MoreViewHolder -> {
                val item = data[position] as Pair<*, *>
                holder.bind((item.first as String), (item.second as MatchOdd), outrightOddListener)
            }
        }

    }

    override fun getItemCount(): Int = data.size

    class OddViewHolder private constructor(
        itemView: View,
        private val refreshListener: OddStateChangeListener
    ) : OddStateViewHolder(itemView) {

        fun bind(
            matchOdd: MatchOdd?,
            item: Odd,
            outrightOddListener: OutrightOddListener?,
            oddsType: OddsType
        ) {
            itemView.outright_odd_btn.apply {
                setupOdd(item, oddsType)
                tv_spread.text = ""
                this@OddViewHolder.setupOddState(this, item)
                setOnClickListener {
                    outrightOddListener?.onClickBet(matchOdd, item)
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

    class SubTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: String, dynamicMarkets: Map<String, DynamicMarket>?) {
            itemView.outright_odd_subtitle.text = dynamicMarkets?.get(item)?.let {
                when (LanguageManager.getSelectLanguage(itemView.context)) {
                    LanguageManager.Language.ZH -> {
                        it.zh
                    }
                    else -> {
                        it.en
                    }
                }
            }
            itemView.outright_odd_instruction.text = "1/3, 顶级 2" //TODO Cheryl: 等後端api的新數值再做更改
        }

        companion object {
            fun from(parent: ViewGroup): SubTitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_subtitle_v4, parent, false)

                return SubTitleViewHolder(view)
            }
        }
    }

    class MoreViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(oddsKey: String, matchOdd: MatchOdd, outrightOddListener: OutrightOddListener?) {
            itemView.setOnClickListener {
                outrightOddListener?.onClickMore(oddsKey, matchOdd)
            }
        }

        companion object {
            fun from(parent: ViewGroup): MoreViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_more_v4, parent, false)

                return MoreViewHolder(view)
            }
        }
    }
}

class OutrightOddListener(
    val clickListenerBet: (matchOdd: MatchOdd?, odd: Odd) -> Unit,
    val clickListenerMore: (oddsKey: String, matchOdd: MatchOdd) -> Unit
) {
    fun onClickBet(matchOdd: MatchOdd?, odd: Odd) = clickListenerBet(matchOdd, odd)
    fun onClickMore(oddsKey: String, matchOdd: MatchOdd) = clickListenerMore(oddsKey, matchOdd)
}