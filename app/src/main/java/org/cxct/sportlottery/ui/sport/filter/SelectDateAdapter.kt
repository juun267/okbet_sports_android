package org.cxct.sportlottery.ui.sport.filter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemSelectDateBinding
import org.cxct.sportlottery.ui.sport.common.SelectDate

class SelectDateAdapter(data: MutableList<SelectDate>, private val onSelectDate:(SelectDate)->Unit): BindingAdapter<SelectDate, ItemSelectDateBinding>(data = data) {
     var selectPos = 0

    override fun onBinding(position: Int, binding: ItemSelectDateBinding, item: SelectDate) {
        binding.root.isSelected = selectPos == position
        binding.tvName.text = item.name
        binding.tvDateWithMonth.text = item.label
        binding.root.setOnClickListener {
            selectPos = position
            notifyDataSetChanged()
            onSelectDate.invoke(data[position])
        }
    }
}