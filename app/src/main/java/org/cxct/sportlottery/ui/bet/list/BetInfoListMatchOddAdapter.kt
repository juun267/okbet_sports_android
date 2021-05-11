package org.cxct.sportlottery.ui.bet.list


import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoMatchOddItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds


class BetInfoListMatchOddAdapter(private val context: Context, private val onItemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<BetInfoListMatchOddAdapter.ViewHolder>() {


    var matchOddList: MutableList<MatchOdd> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
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
        holder.bind(matchOddList[position], position)
    }


    inner class ViewHolder(private val binding: ContentBetInfoMatchOddItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(matchOdd: MatchOdd, position: Int) {
            binding.matchOdd = matchOdd
            binding.betInfoDetail.tvOdds.text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            val strVerse = context.getString(R.string.verse_lower)
            val strMatch = "${matchOdd.homeName}${strVerse}${matchOdd.awayName}"
            binding.betInfoDetail.tvMatch.text = strMatch
            (binding.betInfoDetail.tvMatch.layoutParams as LinearLayout.LayoutParams).bottomMargin = 3.dp
            binding.betInfoDetail.tvName.text =
                if (matchOdd.inplay == INPLAY) {
                    context.getString(
                        R.string.bet_info_in_play_score,
                        matchOdd.playCateName,
                        matchOdd.homeScore.toString(),
                        matchOdd.awayScore.toString()
                    )
                } else matchOdd.playCateName


            if(matchOdd.gameType == matchOddList[0].gameType) {

                binding.tvCloseWarning.text = context.getString(
                    matchOdd.betAddError?.string ?: R.string.bet_info_list_game_closed
                )

                when (matchOdd.status) {
                    BetStatus.LOCKED.code, BetStatus.DEACTIVATED.code -> {
                        binding.tvCloseWarning.visibility = View.VISIBLE
                        (binding.betInfoDetail.tvMatch.layoutParams as LinearLayout.LayoutParams).bottomMargin = 0.dp
                    }

                    BetStatus.ACTIVATED.code -> {
                        binding.tvCloseWarning.visibility = if (matchOdd.betAddError == null) View.GONE else View.VISIBLE
                        setChangeOdds(
                            binding.betInfoDetail.tvOdds,
                            binding.tvOddChange,
                            matchOdd,
                        )
                        (binding.betInfoDetail.tvMatch.layoutParams as LinearLayout.LayoutParams).bottomMargin = 3.dp
                    }
                }


            }else{
                binding.tvCloseWarning.apply {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.bet_info_different_game_type)
                }
            }
            binding.executePendingBindings()
        }
    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
        fun onOddChange()
    }


    private fun setChangeOdds(tvOdds: TextView, tvOddChange: TextView, matchOdd: MatchOdd) {
        when (matchOdd.oddState) {
            OddState.LARGER.state -> {
                tvOddChange.visibility = View.VISIBLE
                tvOdds.apply {
                    setBackgroundColor(ContextCompat.getColor(tvOdds.context, R.color.orangeRed))
                    setTextColor(ContextCompat.getColor(tvOdds.context, R.color.white))
                    text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
                }
                onItemClickListener.onOddChange()
            }

            OddState.SMALLER.state -> {
                tvOddChange.visibility = View.VISIBLE
                tvOdds.apply {
                    setBackgroundColor(ContextCompat.getColor(tvOdds.context, R.color.orangeRed))
                    setTextColor(ContextCompat.getColor(tvOdds.context, R.color.white))
                    text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
                }
                onItemClickListener.onOddChange()
            }
        }

        Handler().postDelayed({
            tvOddChange.visibility = View.GONE
            tvOdds.apply {
                setBackgroundColor(ContextCompat.getColor(tvOdds.context, R.color.transparent))
                setTextColor(ContextCompat.getColor(tvOdds.context, R.color.colorOrange))
            }
        }, CHANGING_ITEM_BG_COLOR_DURATION)

    }


}