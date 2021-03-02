package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ContentOddsDetailMoreBinding


class OddsDetailMoreAdapter(private val matchInfoList: List<MoreGameEntity>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<OddsDetailMoreAdapter.MoreViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ContentOddsDetailMoreBinding = ContentOddsDetailMoreBinding.inflate(layoutInflater, parent, false)
        return MoreViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return matchInfoList.size
    }


    override fun onBindViewHolder(holder: MoreViewHolder, position: Int) {
        holder.bind(matchInfoList[position])
    }


    inner class MoreViewHolder(private val binding: ContentOddsDetailMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moreGameEntity: MoreGameEntity) {
            binding.item = moreGameEntity
            binding.executePendingBindings()
            binding.root.setOnClickListener { onItemClickListener.onItemClick(moreGameEntity.id) }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(matchId: String)
    }

}