package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding

class SettlementRvAdapter(private val viewModel: SettlementViewModel) : RecyclerView.Adapter<SettlementRvAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        val binding = ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
//        return mDataList.size
        return 4
    }
    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) : RecyclerView.ViewHolder(binding.root){

}
}