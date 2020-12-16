package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding

class SettlementRvAdapter(private val viewModel: SettlementViewModel) : RecyclerView.Adapter<SettlementRvAdapter.ItemViewHolder>() {

    var data = viewModel.settlementData.value

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        val binding = ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
        binding.viewModel = viewModel
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.bind(viewModel, data?.get(position) ?: SettlementItem(""))
    }

    override fun getItemCount(): Int {
//        return mDataList.size
        return data?.count() ?:0
    }
    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(viewModel: SettlementViewModel, item: SettlementItem){
            binding.item = item
            val gameResultRvAdapter = GameResultRvAdapter(viewModel)
            binding.rvGameResult.adapter = gameResultRvAdapter
            binding.executePendingBindings()
        }
}
}