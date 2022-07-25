package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemPublicitySportIndicatorBinding
import org.cxct.sportlottery.network.sport.SportMenu

class PublicitySportIndicatorAdapter :
    RecyclerView.Adapter<PublicitySportIndicatorAdapter.PublicitySportIndicatorViewHolder>() {
    private var mSportSelectedList: MutableList<Boolean> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setupSportSelectedList(sportPageDataList: List<List<SportMenu>>?, selectedPage: Int) {
        mSportSelectedList = sportPageDataList?.mapIndexed { index, _ ->
            when (index) {
                selectedPage -> true
                else -> false
            }
        }?.toMutableList() ?: mutableListOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicitySportIndicatorViewHolder {
        return PublicitySportIndicatorViewHolder(
            ItemPublicitySportIndicatorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PublicitySportIndicatorViewHolder, position: Int) {
        holder.bind(mSportSelectedList[position])
    }

    override fun getItemCount(): Int = mSportSelectedList.size

    inner class PublicitySportIndicatorViewHolder(val binding: ItemPublicitySportIndicatorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sportSelected: Boolean) {
            binding.idSelected.isVisible = sportSelected
            binding.idUnSelected.isVisible = !sportSelected
        }
    }
}