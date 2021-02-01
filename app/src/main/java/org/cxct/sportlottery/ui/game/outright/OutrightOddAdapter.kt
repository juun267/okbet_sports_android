package org.cxct.sportlottery.ui.game.outright

import android.content.res.ColorStateList
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

const val CHANGING_ITEM_BG_COLOR_DURATION: Long = 3000

class OutrightOddAdapter : RecyclerView.Adapter<OutrightOddAdapter.ViewHolder>() {
    var data = listOf<Odd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var updatedWinnerOddsList = listOf<Odd>()
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
        updateItemDataFromSocket(item)

        holder.bind(item, outrightOddListener, betInfoListData)
    }

    private fun updateItemDataFromSocket(originItem: Odd) {
        if (updatedWinnerOddsList.isNullOrEmpty()) return

        updatedWinnerOddsList.forEach {
            if (originItem.id == it.id) {
                //後端表示originItem.spread的值只用api回傳, socket不可覆蓋
                originItem.odds = it.odds
                originItem.status = it.status
                originItem.producerId = it.producerId
            }
        }

    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Odd, outrightOddListener: OutrightOddListener?, betInfoListData: List<BetInfoListData>?) {
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
                isSelected = item.isSelected
                outright_bet.setOnClickListener {
                    outrightOddListener?.onClick(item)
                }
                setStatus(outright_bet, bet_lock_img, item.odds.toString().isEmpty(), item.status)
                setHighlight(outright_bet, item.oddState)
            }
        }


        private fun setStatus(button: Button, lockImg: ImageView, isOddsNull: Boolean, status: Int) {
            var itemState = status
            if (isOddsNull) itemState = 2

            when (itemState) {
                BetStatus.ACTIVATED.code -> {
                    lockImg.visibility = View.GONE
                    button.visibility = View.VISIBLE
                    button.isEnabled = true
                }
                BetStatus.LOCKED.code -> {
                    lockImg.visibility = View.VISIBLE
                    button.visibility = View.VISIBLE
                    button.isEnabled = false
                }
                BetStatus.DEACTIVATED.code -> {
                    lockImg.visibility = View.GONE
                    button.visibility = View.GONE
                    button.isEnabled = false
                }
            }
        }

        private fun setHighlight(button: Button, status: Int) {
            when (status) {
                OddState.LARGER.state -> button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.green))
                OddState.SMALLER.state -> button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.red))
            }

            Handler().postDelayed(
                {
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.white))
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