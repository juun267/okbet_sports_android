package org.cxct.sportlottery.ui.profileCenter.timezone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemTimezoneBinding
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

class TimeZoneAdapter(private val clickListener: ItemClickListener) :
    ListAdapter<TimeZone, RecyclerView.ViewHolder>(DiffCallback()) {

    fun setItems(list: List<TimeZone>?, commitCallback: Runnable){
        submitList(list, commitCallback)
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
            when(LanguageManager.getSelectLanguage(itemView.context)){
                LanguageManager.Language.ZH->{
                    binding.tvCity.text=data.city_zh
                    binding.tvCountry.text = data.country_zh
                }
                LanguageManager.Language.EN -> {
                    binding.tvCity.text = data.city_en
                    binding.tvCountry.text = data.country_en
                }
                LanguageManager.Language.VI -> {
                    binding.tvCity.text = data.city_vi
                    binding.tvCountry.text = data.country_vi
                }

                LanguageManager.Language.PHI ->{
                    binding.tvCity.text = data.city_ph
                    binding.tvCountry.text = data.country_ph
                }
            }
            binding.tvDateWeek.text = getDateAndWeek(data.name)

            if (data.isSelected){
               binding.tvCity.setTextColor(itemView.context.getColor(R.color.color_025BE8))
                binding.tvTime.setTextColor(itemView.context.getColor(R.color.color_025BE8))
            }else{
                binding.tvCity.setTextColor(itemView.context.getColor(R.color.color_535D76))
                binding.tvTime.setTextColor(itemView.context.getColor(R.color.color_A7B2C4))
            }
        }

        fun getDateAndWeek(timeZone:String):String{
            var cal=Calendar.getInstance(java.util.TimeZone.getTimeZone(timeZone))
            val newFormatter = SimpleDateFormat(TimeUtil.MD_FORMAT, Locale.getDefault())
            newFormatter.timeZone=java.util.TimeZone.getTimeZone(timeZone)
            var date=newFormatter.format(cal.time)
            return date+ " (" + TimeUtil.setupDayOfWeekVi(itemView.context,cal) + ")"
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
        return oldItem.city_en == newItem.city_en&&oldItem.country_en == newItem.country_en
    }

    override fun areContentsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
        return oldItem == newItem
    }
}

class ItemClickListener(val clickListener: (data: TimeZone) -> Unit) {
    fun onClick(data: TimeZone) = clickListener(data)
}

