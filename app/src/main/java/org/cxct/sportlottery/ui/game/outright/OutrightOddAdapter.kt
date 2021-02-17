package org.cxct.sportlottery.ui.game.outright

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_odd.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitle.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

const val CHANGING_ITEM_BG_COLOR_DURATION: Long = 3000

class OutrightOddAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        SUB_TITLE, ODD
    }

    var matchOdd: MatchOdd? = null
        set(value) {
            field = value

            value?.let {
                data = it.displayList
            }
        }

    private var data = listOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    var betInfoListData: List<BetInfoListData>? = null
        set(value) {
            field = value
            data.forEach { item ->
                when (item) {
                    is Odd -> {
                        item.isSelected = value?.any {
                            it.matchOdd.oddsId == item.id
                        } ?: false
                    }
                }
            }
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
            else -> ViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SubTitleViewHolder -> {
                val item = data[position] as String
                holder.bind(item)
            }
            is ViewHolder -> {
                val item = data[position] as Odd
                holder.bind(item, outrightOddListener, betInfoListData)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            item: Odd,
            outrightOddListener: OutrightOddListener?,
            betInfoListData: List<BetInfoListData>?
        ) {
            itemView.apply {
                kotlin.run status@{
                    betInfoListData?.forEach {
                        if (it.matchOdd.oddsId == item.id) {
                            item.isSelected = true
                            return@status
                        } else {
                            item.isSelected = false
                        }
                    }
                }
                outright_name.text = item.spread
                item.odds?.let { odd -> outright_bet.text = TextUtil.formatForOdd(odd) }
                isSelected = item.isSelected ?: false
                outright_bet.setOnClickListener {
                    outrightOddListener?.onClick(item)
                }
                setHighlight(outright_bet, item.oddState)
                setStatus(outright_bet, bet_lock_img, item.odds.toString().isEmpty(), item.status)
            }
        }


        private fun setStatus(
            textView: TextView,
            lockImg: ImageView,
            isOddsNull: Boolean,
            status: Int
        ) {
            var itemState = status
            if (isOddsNull) itemState = 2

            when (itemState) {
                BetStatus.ACTIVATED.code -> {
                    lockImg.visibility = View.GONE
                    textView.visibility = View.VISIBLE
                    textView.isEnabled = true
                }
                BetStatus.LOCKED.code -> {
                    lockImg.visibility = View.VISIBLE
                    textView.visibility = View.VISIBLE
                    textView.isEnabled = false
                }
                BetStatus.DEACTIVATED.code -> {
                    lockImg.visibility = View.GONE
                    textView.visibility = View.GONE
                    textView.isEnabled = false
                }
            }
        }

        private fun setHighlight(button: TextView, status: Int? = OddState.SAME.state) {
            when (status) {
                OddState.LARGER.state ->
                    button.background = ContextCompat.getDrawable(
                        button.context,
                        R.drawable.shape_play_category_bet_bg_green
                    )
                OddState.SMALLER.state ->
                    button.background = ContextCompat.getDrawable(
                        button.context,
                        R.drawable.shape_play_category_bet_bg_red
                    )
            }

            Handler().postDelayed(
                {
                    button.background = ContextCompat.getDrawable(
                        button.context,
                        R.drawable.shape_play_category_bet_bg
                    )
                }, CHANGING_ITEM_BG_COLOR_DURATION
            )
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }

    class SubTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: String) {
            itemView.outright_detail_list_subtitle.text = item
        }

        companion object {
            fun from(parent: ViewGroup): SubTitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_subtitle, parent, false)

                return SubTitleViewHolder(view)
            }
        }
    }

    class OutrightOddListener(val clickListener: (odd: Odd) -> Unit) {
        fun onClick(odd: Odd) = clickListener(odd)
    }
}