package org.cxct.sportlottery.ui.maintab.home.news

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.databinding.ItemHomeNewsBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class HomeNewsAdapter : BindingAdapter<NewsItem, ItemHomeNewsBinding>() {

    override fun onBinding(
        position: Int,
        vb: ItemHomeNewsBinding,
        item: NewsItem,
    ) = vb.run {
        ivCover.roundOf(item.image, 8.dp, R.drawable.img_banner01)
        tvTitle.text = item.title
        tvTime.text = TimeUtil.timeFormat(item.createTimeInMillisecond,
            TimeUtil.NEWS_TIME_FORMAT,
            locale = Locale.ENGLISH)
        tvContent.text = item.summary
    }
}
