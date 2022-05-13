package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_more_v4.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitle_v4.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_v4.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
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
                matchOdd.oddsMap?.forEach {
                    if (it.value?.get(0) != null) {
                        list.add(it.key)

                        list.addAll(
                            it.value?.filterNotNull()
                                //?.filterIndexed { index, _ -> index < 4 }
                                ?.mapIndexed { index, odd ->
                                    odd.outrightCateKey = it.key
                                    odd
                                } ?: listOf()
                        )

                        if (it.value?.filterNotNull()?.size ?: 0 > 4) {
                            list.add(it.key to matchOdd)
                        }
                    }
                    data = list
                }
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
            else -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_outright_odd_more_v4, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SubTitleViewHolder -> {
                val item = data[position] as String
                holder.bind(matchOdd,item, matchOdd?.dynamicMarkets, outrightOddListener)
            }
            is OddViewHolder -> {
                val item = data[position] as Odd
                matchOdd?.matchInfo?.leagueName = matchOdd?.matchInfo?.name
                holder.bind(matchOdd, item, outrightOddListener, oddsType)
            }
            is MoreViewHolder -> {
                val item = data[position] as Pair<*, *>
                val isExpand = data.filterIsInstance<Odd>()
                    .first { it.outrightCateKey == item.first as String }.isExpand
                holder.bind(
                    (item.first as String),
                    (item.second as MatchOdd),
                    isExpand,
                    outrightOddListener
                )
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
            if(item.isExpand){
                itemView.outright_odd_btn.apply {
                    setupOdd(item, oddsType)
                    tv_spread.text = ""
                    this@OddViewHolder.setupOddState(this, item)
                    setOnClickListener {
                        outrightOddListener?.onClickBet(matchOdd, item, item.outrightCateKey ?: "")
                    }
                }
            }
            itemView.ll_odd_content.visibility =
                if (item.isExpand) View.VISIBLE else View.GONE
            itemView.layoutParams = if (item.isExpand) LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ) else LinearLayout.LayoutParams(0, 0)
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

        fun bind(matchOdd: MatchOdd?,item: String, dynamicMarkets: Map<String, DynamicMarket>?,outrightOddListener: OutrightOddListener?) {
            itemView.outright_odd_subtitle.text = dynamicMarkets?.get(item)?.let {
                when (LanguageManager.getSelectLanguage(itemView.context)) {
                    LanguageManager.Language.ZH -> {
                        it.zh
                    }
                    LanguageManager.Language.VI -> {
                        it.vi
                    }
                    else -> {
                        it.en
                    }
                }
            }
            itemView.outright_odd_subtitle.setOnClickListener {
                outrightOddListener?.onClickExpand(matchOdd,item)
            }
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

    fun getOddIsExpand(position: Int): Boolean {
        val item = data[position] as Pair<*, *>
        return data.filterIsInstance<Odd>().first { it.outrightCateKey == item.first as String }.isExpand
    }

    inner class MoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvMore = itemView.findViewById<TextView>(R.id.tvMore)
        private val ivMoreIcon = itemView.findViewById<ImageView>(R.id.ivMoreIcon)

        fun bind(oddsKey: String, matchOdd: MatchOdd, isExpand:Boolean, outrightOddListener: OutrightOddListener?) {
            itemView.setOnClickListener {
                outrightOddListener?.onClickMore(oddsKey, matchOdd)
                val item = data[adapterPosition] as Pair<*, *>
                data.filterIsInstance<Odd>().first { it.outrightCateKey == item.first as String }.isExpand = !data.filterIsInstance<Odd>().first { it.outrightCateKey == item.first as String }.isExpand

            }
            when(isExpand) {
                true -> {
                    tvMore.text = itemView.context.getString(R.string.odds_detail_more)
                    ivMoreIcon.animate().rotation(0f).setDuration(100).start()
                }
                false -> {
                    tvMore.text = itemView.context.getString(R.string.odds_detail_less)
                    ivMoreIcon.animate().rotation(180f).setDuration(100).start()
                }
            }
//            itemView.ll_more_content.visibility = if (isExpand) View.VISIBLE else View.GONE
//            itemView.layoutParams = if (isExpand) LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                (itemView.context.resources.displayMetrics.density * 64).toInt()
//            ) else LinearLayout.LayoutParams(0, 0)
        }

//        companion object {
//            fun from(parent: ViewGroup): MoreViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val view = layoutInflater.inflate(R.layout.itemview_outright_odd_more_v4, parent, false)
//
//                return MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_outright_odd_more_v4, parent, false))
//            }
//        }
    }
}

class OutrightOddListener(
    val clickListenerBet: (matchOdd: MatchOdd?, odd: Odd, playCateCode: String) -> Unit,
    val clickListenerMore: (oddsKey: String, matchOdd: MatchOdd) -> Unit,
    val clickExpand: (matchOdd: MatchOdd?, oddsKey: String) -> Unit
) {
    fun onClickBet(matchOdd: MatchOdd?, odd: Odd, playCateCode: String) = clickListenerBet(matchOdd, odd, playCateCode)
    fun onClickMore(oddsKey: String, matchOdd: MatchOdd) = clickListenerMore(oddsKey, matchOdd)
    fun onClickExpand(matchOdd: MatchOdd?,oddsKey: String) = clickExpand(matchOdd, oddsKey)
}