package org.cxct.sportlottery.ui.news

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemSportNewsBinding
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors
import java.util.Locale

class RecyclerSportNewsAdapter:BindingAdapter<News,ItemSportNewsBinding>() {
    override fun onBinding(position: Int, binding: ItemSportNewsBinding, item: News) {

        binding.tvTime.text= TimeUtil.timeFormat(item.addTime.toLong(),
            TimeUtil.NEWS_TIME_FORMAT,
            locale = Locale.ENGLISH)
        binding.linearMore.onClick {
            //最高行如果是1行
            if(binding.tvContent.maxLines==1){
                //设置最高10000
                binding.tvContent.maxLines=10000
                binding.tvContent.text = item.message
                binding.tvExpand.setColors(R.color.color_025BE8)
                binding.tvExpand.text=context.getString(R.string.D039)
                binding.ivArrow.setImageResource(R.drawable.ic_sport_news_arrow_blue)
            }else{
                binding.tvContent.maxLines=1
                binding.tvContent.text = item.title
                binding.tvExpand.setColors(R.color.color_6D7693)
                binding.tvExpand.text=context.getString(R.string.J634)
                binding.ivArrow.setImageResource(R.drawable.ic_sport_news_arrow)
            }

        }
    }
}