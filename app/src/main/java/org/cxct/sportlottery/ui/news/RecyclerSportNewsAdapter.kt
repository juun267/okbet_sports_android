package org.cxct.sportlottery.ui.news

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemSportNewsBinding
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors

class RecyclerSportNewsAdapter:BindingAdapter<News,ItemSportNewsBinding>() {
    override fun onBinding(position: Int, binding: ItemSportNewsBinding, item: News) {
        binding.tvTime.text=item.addTime
        binding.tvContent.text=item.message
        binding.linearMore.onClick {
            //最高行如果是1行
            if(binding.tvContent.maxLines==1){
                //设置最高10000
                binding.tvContent.maxLines=10000
                binding.tvExpand.setColors(R.color.color_025BE8)
            }else{
                binding.tvContent.maxLines=1
                binding.tvExpand.setColors(R.color.color_6D7693)
            }

        }
    }
}