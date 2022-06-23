package org.cxct.sportlottery.ui.profileCenter.timezone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemTimezoneBinding

class TimeZoneAdapter(private val clickListener: ItemClickListener) :
    ListAdapter<TimeZone, RecyclerView.ViewHolder>(DiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun setItems(list: List<TimeZone>?){
        adapterScope.launch {
            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(list)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var itemHolder = holder as ItemViewHolder
        val data = getItem(position) as TimeZone
        itemHolder.bind(data, clickListener)
    }

    class ItemViewHolder private constructor(val binding: ItemTimezoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TimeZone, clickListener: ItemClickListener) {
            itemView.setOnClickListener(View.OnClickListener {
                clickListener.onClick(data)
            })
            binding.tvTime.text=data.name
            binding.tvCity.text=data.city_zh
            binding.tvCountry.text=data.country_zh
            if (data.isSelected){
               binding.tvCity.setTextColor(itemView.context.getColor(R.color.color_317FFF_0760D4))
                binding.tvTime.setTextColor(itemView.context.getColor(R.color.color_317FFF_0760D4))
            }else{
                binding.tvCity.setTextColor(itemView.context.getColor(R.color.color_909090_666666))
                binding.tvTime.setTextColor(itemView.context.getColor(R.color.color_909090_666666))
            }
        }
        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTimezoneBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }
    }


}

class DiffCallback : DiffUtil.ItemCallback<TimeZone>() {
    override fun areItemsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
        return oldItem.city_en == newItem.city_en
    }

    override fun areContentsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
        return oldItem == newItem
    }
}

class ItemClickListener(val clickListener: (data: TimeZone) -> Unit) {
    fun onClick(data: TimeZone) = clickListener(data)
}

