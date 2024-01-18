package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddSpreadForSCOCompare
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetail
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetailSCO
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
    private val oddsType: OddsType,
    private val onMoreClickListener: OnMoreClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                oddsDetail.scoItem.forEach {
                    if (it.value.any { o -> o == odd }){
                        notifyItemChanged(keys.indexOf(it.key))
                        return
                    }
                }
            }
        }
    }


    enum class ItemType {
        ITEM, NO_GOALS, MORE
    }


    private var keys = oddsDetail.scoItem.mapTo(mutableListOf()) { it.key }


    var mOddsDetail: OddsDetailListData? = null
        set(value) {
            field = value
            oddsDetail = value as OddsDetailListData
            keys = oddsDetail.scoItem.mapTo(mutableListOf()) { it.key }
            notifyDataSetChanged()
        }


    override fun getItemViewType(position: Int): Int {
        return when {
            position == keys.lastIndex -> {
                ItemType.NO_GOALS.ordinal
            }
            keys.size > OVER_COUNT && position == keys.size -> {
                ItemType.MORE.ordinal
            }
            else -> {
                ItemType.ITEM.ordinal
            }
        }
    }


    override fun getItemCount(): Int = when {
        keys.size > 5 -> keys.size + MORE_ITEM
        else -> keys.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_sco_item, parent, false))
            ItemType.NO_GOALS.ordinal -> NoGoalsViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.content_type_sco_no_goals, parent, false)
            )
            else -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_more_item, parent, false))
        }
    }


    inner class ViewHolder(view: View) : OddStateViewHolderDetail(view) {

        private val btnOdds1st = itemView.findViewById<OddsButtonDetailSCO>(R.id.button_odds_1st)
        private val btnOddsAnt = itemView.findViewById<OddsButtonDetailSCO>(R.id.button_odds_ant)
        private val btnOddsLast = itemView.findViewById<OddsButtonDetailSCO>(R.id.button_odds_last)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val clItem = itemView.findViewById<ConstraintLayout>(R.id.cl_item)

        fun bindModel(key: String) {
            clItem.visibility = if (!oddsDetail.isMoreExpand && adapterPosition > 4) View.GONE else View.VISIBLE

            tvName.text = key

            val oddsList = oddsDetail.scoItem[key]

            val fOdd = oddsList?.find { it?.playCode?.contains(OddSpreadForSCOCompare.SCORE_1ST) == true }
            val aOdd = oddsList?.find { it?.playCode?.contains(OddSpreadForSCOCompare.SCORE_ANT) == true }
            val lOdd = oddsList?.find { it?.playCode?.contains(OddSpreadForSCOCompare.SCORE_LAST) == true }

            btnOdds1st?.apply {
                setupOdd(fOdd, oddsType, matchInfo = oddsDetail.matchInfo)
                setupOddState(this, fOdd)
                clickDelay {
                    fOdd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail, context.getString(R.string.sco_name_first)) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_first)
                tv_spread.text = ""
            }

            btnOddsAnt?.apply {
                setupOdd(aOdd, oddsType, matchInfo = oddsDetail.matchInfo)
                setupOddState(this, aOdd)
                clickDelay {
                    aOdd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail, context.getString(R.string.sco_name_any)) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_ant)
                tv_spread.text = ""
            }

            btnOddsLast?.apply {
                setupOdd(lOdd, oddsType, matchInfo = oddsDetail.matchInfo)
                setupOddState(this, lOdd)
                clickDelay {
                    lOdd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail, context.getString(R.string.sco_name_last)) }
                }
                tv_name.text = context.getString(R.string.odds_button_name_last)
                tv_spread.text = ""
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


    inner class NoGoalsViewHolder(view: View) : OddStateViewHolderDetail(view) {

        private val btnOdds = itemView.findViewById<OddsButtonDetail>(R.id.button_odds)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val clItem = itemView.findViewById<ConstraintLayout>(R.id.cl_item)

        fun bindModel(key: String) {
            clItem.visibility = if (!oddsDetail.isMoreExpand && adapterPosition > 4) View.GONE else View.VISIBLE

            tvName.text = key

            val oddsList = oddsDetail.scoItem[key]
            val odd = oddsList?.find { TextUtil.compareWithGameKey(OddSpreadForSCOCompare.SCORE_N, "${it?.playCode}") }

            btnOdds?.apply {
                setupOdd(odd, oddsType, matchInfo = oddsDetail.matchInfo)
                setupOddState(this, odd)
                clickDelay {
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                tv_name.visibility = View.GONE
                tv_spread.visibility = View.GONE
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpandControl: TextView = itemView.findViewById(R.id.tv_expand_control)
        fun bind() {
            tvExpandControl.apply {
                setOnClickListener {
                    onMoreClickListener.click()
                }
                text = if (oddsDetail.isMoreExpand) context.getString(R.string.odds_detail_less) else context.getString(R.string.display_more)
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