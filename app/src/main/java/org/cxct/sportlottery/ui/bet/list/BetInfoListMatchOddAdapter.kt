package org.cxct.sportlottery.ui.bet.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoMatchOddItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd

class BetInfoListMatchOddAdapter(private val onItemClickListener: OnItemClickListener) :
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
            binding.betInfoDetail.tvOdds.text = String.format(binding.root.context.getString(R.string.bet_info_list_odd), matchOdd.odds.toString())
            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            binding.executePendingBindings()
        }
    }


    fun modify(list: List<MatchOdd>, position: Int) {
        if (matchOddList.size == 0) {
            matchOddList.addAll(list)
            notifyDataSetChanged()
        } else if (list.size < matchOddList.size) {
            with(matchOddList) {
                notifyItemRemoved(position)
                notifyItemRangeChanged(0, matchOddList.size)
                clear()
                addAll(list)
            }
        }
    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
    }

}