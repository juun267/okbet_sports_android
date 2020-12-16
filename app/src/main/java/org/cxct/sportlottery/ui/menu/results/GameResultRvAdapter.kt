package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ContentGameResultRvBinding

class GameResultRvAdapter(private val viewModel: SettlementViewModel): RecyclerView.Adapter<ResultItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultItemViewHolder {
        val viewLayout = LayoutInflater.from(parent.context)
        val binding = ContentGameResultRvBinding.inflate(viewLayout, parent, false)
        return ResultItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ResultItemViewHolder, position: Int) {
        holder.bind(viewModel, "") //TODO Dean : send item (item data)
    }

}

class ResultItemViewHolder internal constructor(private val binding: ContentGameResultRvBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(viewModel: SettlementViewModel, item: String){
        binding.viewModel = viewModel
//        binding.rvTimeLineDetail.adapter = adapter //TODO Dean : create detail rv adapter
    }
}