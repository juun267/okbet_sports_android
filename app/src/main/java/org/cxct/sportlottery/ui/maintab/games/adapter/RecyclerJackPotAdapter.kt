package org.cxct.sportlottery.ui.maintab.games.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemJackPotBinding
import org.cxct.sportlottery.util.DisplayUtil.dp

class RecyclerJackPotAdapter(private val rollerItemWidth:Int): BindingAdapter<String, ItemJackPotBinding>()  {

    override fun onBinding(position: Int, binding: ItemJackPotBinding, item: String) {
        binding.run {
            val params=tvNumber.layoutParams
            params.width=rollerItemWidth
//            params.height=30.dp
            tvNumber.layoutParams=params
            tvNumber.text=item
        }

    }
}