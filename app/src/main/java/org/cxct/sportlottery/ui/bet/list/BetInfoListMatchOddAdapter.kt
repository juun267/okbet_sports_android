package org.cxct.sportlottery.ui.bet.list

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoMatchOddItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.outright.CHANGING_ITEM_BG_COLOR_DURATION
import org.cxct.sportlottery.util.TextUtil
import java.lang.Exception

class BetInfoListMatchOddAdapter(private val context: Context, private val onItemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<BetInfoListMatchOddAdapter.ViewHolder>() {


    var matchOddList: MutableList<MatchOdd> = mutableListOf()


    var updatedBetInfoList: MutableList<MatchOdd> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    private fun updateItemDataFromSocket(matchOdd: MatchOdd, updatedBetInfoList: MutableList<MatchOdd>) {
        for (newItem in updatedBetInfoList) {
            //null check後還是會crash 先以不crash為主
            try {
                newItem.oddsId.let {
                    if (it == matchOdd.oddsId) {
                        matchOdd.oddState = getOddState(matchOdd.odds, newItem)
                        newItem.odds.let { odds -> matchOdd.odds = odds }
                        newItem.status.let { status -> matchOdd.status = status }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun getOddState(oldItemOdd: Double, it: MatchOdd): Int {
        val newOdd = it.odds ?: 0.0
        return when {
            newOdd == oldItemOdd -> OddState.SAME.state
            newOdd > oldItemOdd -> OddState.LARGER.state
            newOdd < oldItemOdd -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ContentBetInfoMatchOddItemBinding = ContentBetInfoMatchOddItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return matchOddList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateItemDataFromSocket(matchOddList[position], updatedBetInfoList)
        holder.bind(matchOddList[position], position)
    }


    inner class ViewHolder(private val binding: ContentBetInfoMatchOddItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(matchOdd: MatchOdd, position: Int) {
            binding.matchOdd = matchOdd
            binding.betInfoDetail.tvOdds.text = String.format(binding.root.context.getString(R.string.bet_info_list_odd), TextUtil.formatForOdd(matchOdd.odds))
            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            val strVerse = context.getString(R.string.verse_)
            val strMatch = "${matchOdd.homeName}${strVerse}${matchOdd.awayName}"
            binding.betInfoDetail.tvMatch.text = strMatch


            when (matchOdd.status) {
                BetStatus.LOCKED.code -> {
                    binding.tvCloseWarning.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.bet_info_list_game_closed)
                    }
                    //clickable false
                }

                BetStatus.ACTIVATED.code -> {
                    binding.tvCloseWarning.visibility = View.GONE
                    //clickable true
                    setChangeOdds(
                            binding.betInfoDetail.tvOdds,
                            binding.tvCloseWarning,
                            matchOdd,
                    )
                }
            }
            binding.executePendingBindings()
        }
    }


    fun modify(list: List<MatchOdd>) {
        matchOddList.clear()
        matchOddList.addAll(list)
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
    }


    private fun setChangeOdds(tv_odds: TextView, tv_close_warning: TextView, matchOdd: MatchOdd) {
        when (matchOdd.oddState) {
            OddState.LARGER.state -> {
                changeColorByOdds(tv_close_warning)
                tv_odds.apply {
                    setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.orangeRed))
                    setTextColor(ContextCompat.getColor(tv_odds.context, R.color.white))
                    text = String.format(tv_odds.context.getString(R.string.bet_info_list_odd), TextUtil.formatForOdd(matchOdd.odds))
                }
            }

            OddState.SMALLER.state -> {
                changeColorByOdds(tv_close_warning)
                tv_odds.apply {
                    setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.green))
                    setTextColor(ContextCompat.getColor(tv_odds.context, R.color.white))
                    text = String.format(tv_odds.context.getString(R.string.bet_info_list_odd), TextUtil.formatForOdd(matchOdd.odds))
                }
            }
        }

        Handler().postDelayed({
            tv_close_warning.visibility = View.GONE
            tv_odds.apply {
                setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.transparent))
                setTextColor(ContextCompat.getColor(tv_odds.context, R.color.orangeRed))
            }
        }, CHANGING_ITEM_BG_COLOR_DURATION)

    }


    private fun changeColorByOdds(tv_close_warning: TextView) {
        tv_close_warning.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.bet_info_list_game_odds_changed)
        }
    }

}