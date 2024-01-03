package org.cxct.sportlottery.ui.finance

import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ViewItemFinanceBinding

class FinanceRecordAdapter(itemClick: OnItemClickListener): BindingAdapter<String, ViewItemFinanceBinding>() {

    init {
        setOnItemClickListener(itemClick)
    }

    override fun onBinding(position: Int, binding: ViewItemFinanceBinding, item: String) {
        binding.tvName.text = item
    }

}

