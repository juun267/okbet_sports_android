package org.cxct.sportlottery.ui.maintab.home.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityBetDetailsBinding
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import java.util.*
import java.util.*


class NewsDetailActivity : BindingActivity<MainHomeViewModel, ActivityNewsDetailBinding>(){

    companion object{
        fun start(context: Context,newsItem: NewsItem){
            val intent = Intent(context,NewsDetailActivity::class.java)
            intent.putExtra("newsItem",newsItem)
            context.startActivity(intent)
        }
    }

    private val newsItem by lazy { intent.getParcelableExtra("newsItem") as NewsItem? }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        newsItem?.let {
            setupNews(it)
        }
        initData()
        initObservable()
    }

    private fun initData(){
        newsItem?.let {
            loading()
            viewModel.getNewsDetail(it.id)
        }
    }


    private fun setupNews(newsItem: NewsItem){
        binding.apply {
            ivCover.roundOf(newsItem?.image, 12.dp, R.drawable.img_banner01)
            tvTitle.text = newsItem?.title
            tvTime.text = TimeUtil.timeFormat(newsItem?.updateTimeInMillisecond,
                TimeUtil.NEWS_TIME_FORMAT,
                locale = Locale.ENGLISH)
            okWebContent.setBackgroundColor(getColor(R.color.color_F8F9FD))
            okWebContent.loadData(getHtmlData(newsItem?.contents ?: ""), "text/html", null)
        }
    }

    private fun initObservable() {
        viewModel.newsDetail.observe(this){
             hideLoading()
             setupNews(it.detail)
            if(it.relatedList.isNullOrEmpty()){
                binding.linNews.gone()
            }else{
                binding.linNews.visible()
                setupRelatedList(it.relatedList)
            }
        }
    }
    private fun setupRelatedList(newsList: List<NewsItem>) {
        binding.apply {
            if (rvNews.adapter == null) {
                rvNews.layoutManager = LinearLayoutManager(this@NewsDetailActivity, RecyclerView.VERTICAL, false)
                rvNews.adapter = HomeNewsAdapter().apply {
                    setList(newsList)
                    setOnItemClickListener(listener = OnItemClickListener { adapter, view, position ->
                        start(this@NewsDetailActivity, (adapter.data[position] as NewsItem))
                    })
                }
            } else {
                (rvNews.adapter as HomeNewsAdapter).setList(newsList)
            }
        }
    }

    private fun getHtmlData(bodyHTML: String): String {
        val head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:auto; height:auto!important;}</style>" +
                "</head>";
        return "<html>$head<body>$bodyHTML</body></html>";
    }
}