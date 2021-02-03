package org.cxct.sportlottery.ui.bet.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoMatchOddItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.util.TextUtil

class BetInfoListMatchOddAdapter(private val context: Context, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BetInfoListMatchOddAdapter.ViewHolder>() {

    var matchOddList: MutableList<MatchOdd> = mutableListOf()

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
            binding.betInfoDetail.tvOdds.text = String.format(binding.root.context.getString(R.string.bet_info_list_odd), TextUtil.formatForOdd(matchOdd.odds))
            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            val strVerse = context.getString(R.string.verse_)
            val strMatch = "${matchOdd.homeName}${strVerse}${matchOdd.awayName}"
            binding.betInfoDetail.tvMatch.text = strMatch

            binding.executePendingBindings()
        }
    }


    fun modify(list: List<MatchOdd>, position: Int) {
            matchOddList.clear()
            matchOddList.addAll(list)
            notifyDataSetChanged()

    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
    }

}