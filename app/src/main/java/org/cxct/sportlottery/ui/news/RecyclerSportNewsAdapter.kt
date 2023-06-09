package org.cxct.sportlottery.ui.news

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemSportNewsBinding
import org.cxct.sportlottery.view.onClick

class RecyclerSportNewsAdapter:BindingAdapter<String,ItemSportNewsBinding>() {
    override fun onBinding(position: Int, binding: ItemSportNewsBinding, item: String) {
        binding.linearMore.onClick {
            //最高行如果是1行
            if(binding.tvContent.maxLines==1){
                //设置最高10000
                binding.tvContent.maxLines=10000
            }else{
                binding.tvContent.maxLines=1
            }

        }
    }
}