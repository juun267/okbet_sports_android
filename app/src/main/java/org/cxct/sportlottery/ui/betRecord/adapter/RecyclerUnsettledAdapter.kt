package org.cxct.sportlottery.ui.betRecord.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBetListBinding

class RecyclerUnsettledAdapter : BindingAdapter<String,ItemBetListBinding>() {

    override fun onBinding(position: Int, binding: ItemBetListBinding, item: String) {
        binding.run {
            recyclerBetCard.layoutManager=LinearLayoutManager(context)
            val cardAdapter=RecyclerBetCardAdapter()
            recyclerBetCard.adapter=cardAdapter
            cardAdapter.setList(arrayListOf("",""))
        }
    }
}