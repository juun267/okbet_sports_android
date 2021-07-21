package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.OddSpreadForSCOCompare
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil


/**
 * @author Kevin
 * @create 2021/7/19
 * @description
 */
const val MORE_ITEM = 1
const val OVER_COUNT = 5
class TypeSCOAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType,
    private val onMoreClickListener: OnMoreClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    enum class ItemType {
        ITEM, NO_GOALS, MORE
    }


    private var keys = oddsDetail.scoItem.mapTo(mutableListOf(), { it.key })


    var mOddsDetail: OddsDetailListData? = null
        set(value) {
            field = value
            oddsDetail = value as OddsDetailListData
            keys = oddsDetail.scoItem.mapTo(mutableListOf(), { it.key })
            notifyDataSetChanged()
        }


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            keys.lastIndex -> {
                ItemType.NO_GOALS.ordinal
            }
            keys.size -> {
                ItemType.MORE.ordinal
            }
            else -> {
                ItemType.ITEM.ordinal
            }
        }
    }


    override fun getItemCount(): Int = keys.size + MORE_ITEM


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_sco_item, parent, false))
            ItemType.NO_GOALS.ordinal -> NoGoalsViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.content_type_sco_no_goals, parent, false)
            )
            else -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_more_item, parent, false))
        }
    }


    inner class ViewHolder(view: View) : OddDetailStateViewHolder(view) {

        private val btnOdds1st = itemView.findViewById<OddsButton>(R.id.button_odds_1st)
        private val btnOddsAnt = itemView.findViewById<OddsButton>(R.id.button_odds_ant)
        private val btnOddsLast = itemView.findViewById<OddsButton>(R.id.button_odds_last)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val clItem = itemView.findViewById<ConstraintLayout>(R.id.cl_item)

        fun bindModel(key: String) {

            clItem.visibility = if (!oddsDetail.isMoreExpand && adapterPosition > 4) View.GONE else View.VISIBLE

            tvName.text = key

            val oddsList = oddsDetail.scoItem[key]

            val fOdds = oddsList?.find { it?.spread?.contains(OddSpreadForSCOCompare.SCORE_1ST.spread) == true }
            val aOdds = oddsList?.find { it?.spread?.contains(OddSpreadForSCOCompare.SCORE_ANT.spread) == true }
            val lOdds = oddsList?.find { it?.spread?.contains(OddSpreadForSCOCompare.SCORE_LAST.spread) == true }

            btnOdds1st?.apply {
                setupOdd(fOdds, oddsType)
                setupOddState(this, fOdds)
                isSelected = betInfoList.any { it.matchOdd.oddsId == fOdds?.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
                    }
                }
                setOnClickListener {
                    fOdds?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_first)
                tv_spread.text = ""
            }

            btnOddsAnt?.apply {
                setupOdd(aOdds, oddsType)
                setupOddState(this, aOdds)
                isSelected = betInfoList.any { it.matchOdd.oddsId == aOdds?.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
                    }
                }
                setOnClickListener {
                    aOdds?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_ant)
                tv_spread.text = ""
            }

            btnOddsLast?.apply {
                setupOdd(lOdds, oddsType)
                setupOddState(this, lOdds)
                isSelected = betInfoList.any { it.matchOdd.oddsId == lOdds?.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
                    }
                }
                setOnClickListener {
                    lOdds?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_last)
                tv_spread.text = ""
            }
        }
    }


    inner class NoGoalsViewHolder(view: View) : OddDetailStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val clItem = itemView.findViewById<ConstraintLayout>(R.id.cl_item)

        fun bindModel(key: String) {

            clItem.visibility = if (!oddsDetail.isMoreExpand && adapterPosition > 4) View.GONE else View.VISIBLE

            tvName.text = key

            val oddsList = oddsDetail.scoItem[key]
            val odds = oddsList?.find { TextUtil.compareWithGameKey(OddSpreadForSCOCompare.SCORE_N.spread, "${it?.spread}") }

            btnOdds?.apply {
                setupOdd(odds, oddsType)
                setupOddState(this, odds)
                isSelected = betInfoList.any { it.matchOdd.oddsId == odds?.id }
                oddStateChangeListener = object : OddStateChangeListener {
                    override fun refreshOddButton(odd: Odd) {
                        notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
                    }
                }
                setOnClickListener {
                    odds?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                tv_name.visibility = View.GONE
                tv_spread.visibility = View.GONE
            }

        }
    }


    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpandControl: TextView = itemView.findViewById(R.id.tv_expand_control)
        fun bind() {
            tvExpandControl.apply {
                setOnClickListener {
                    onMoreClickListener.click()
                }
                visibility = if (oddsDetail.oddArrayList.size > OVER_COUNT) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                text = if (oddsDetail.isMoreExpand) context.getString(R.string.odds_detail_less) else context.getString(R.string.odds_detail_more)
            }
        }
    }


    interface OnMoreClickListener {
        fun click()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bindModel(keys[position])
            is NoGoalsViewHolder -> holder.bindModel(keys[position])
            is MoreViewHolder -> holder.bind()
        }
    }


}