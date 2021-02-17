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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

const val CHANGING_ITEM_BG_COLOR_DURATION: Long = 3000

class OutrightOddAdapter : RecyclerView.Adapter<OutrightOddAdapter.ViewHolder>() {
    var data2 = listOf<MatchOdd>()
        set(value) {
            field = value
            data = if (field.isNotEmpty()) {
                field[0].odds.values.first()
            } else {
                listOf()
            }
        }

    var data = listOf<Odd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    var betInfoListData: List<BetInfoListData>? = null
        set(value) {
            field = value
            //TODO : review why not work first time
            data.forEach { odd ->
                odd.isSelected = value?.any {
                    it.matchOdd.oddsId == odd.id
                } ?: false
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, outrightOddListener, betInfoListData)
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

    class OutrightOddListener(val clickListener: (odd: Odd) -> Unit) {
        fun onClick(odd: Odd) = clickListener(odd)
    }
}