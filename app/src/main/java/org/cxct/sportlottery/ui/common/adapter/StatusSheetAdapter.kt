package org.cxct.sportlottery.ui.common.adapter

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.CustomBottomSheetItemBinding


class StatusSheetAdapter(private val checkedListener: ItemCheckedListener) : BindingAdapter<StatusSheetData,CustomBottomSheetItemBinding>(){

    private var mPreviousItem: String? = null

    var checkedItemCode: String? = null

    var defaultCheckedCode: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBinding(
        position: Int,
        binding: CustomBottomSheetItemBinding,
        item: StatusSheetData,
    ) {
        if ((item.code == defaultCheckedCode || item.code == null) && mPreviousItem == null) {
            item.isChecked = true
            checkedItemCode = item.code
            mPreviousItem = getItem(position).showName
        }

        binding.checkbox.setOnClickListener {
            var previousPosition: Int? = null
                data.forEachIndexed { index, data ->
                    if (data.showName == mPreviousItem)
                        previousPosition = index
                }

            if (previousPosition != null) {
                data[previousPosition!!].isChecked = false
                notifyItemChanged(previousPosition!!)
            }

            mPreviousItem = data[position].showName
            binding.checkbox.isChecked = true
            item.isChecked = true

            checkedListener.onChecked(binding.checkbox.isChecked, item)

            checkedItemCode = item.code

            notifyItemChanged(position)
        }
        binding.checkbox.apply {
            isChecked = item.isChecked
            text = item.showName
            setBackgroundColor(
                if (item.isChecked)
                    ContextCompat.getColor(context, R.color.color_191919_EEEFF0)
                else
                    ContextCompat.getColor(context, R.color.color_191919_FCFCFC)
            )
        }
    }

    class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: StatusSheetData) -> Unit) {
        fun onChecked(isChecked: Boolean, data: StatusSheetData) =
            checkedListener(isChecked, data)
    }
}

data class StatusSheetData(val code: String?, val showName: String?) {
    var isChecked = false
}