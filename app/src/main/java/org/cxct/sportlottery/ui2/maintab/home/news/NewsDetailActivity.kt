package org.cxct.sportlottery.ui2.maintab.home.news

import android.content.Context
import android.content.Intent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui2.base.BindingActivity
import org.cxct.sportlottery.ui2.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import java.util.*


class NewsDetailActivity : BindingActivity<MainHomeViewModel, ActivityNewsDetailBinding>(){

    companion object{
        fun start(context: Context,newsItem: NewsItem){
            val intent = Intent(context, NewsDetailActivity::class.java)
            intent.putExtra("newsItem",newsItem)
            context.startActivity(intent)
        }
    }

    private var currentId = -1

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        initRecyclerView()

        initObservable()
        reload(intent)
    }

    private fun reload(intent: Intent) {
        val newsItem = intent.getParcelableExtra("newsItem") as NewsItem?
        if (newsItem == null) {
            finish()
            return
        }

        loading()
        currentId = newsItem.id
        viewModel.getNewsDetail(newsItem.id)
        setupNews(newsItem)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        reload(newIntent)
        binding.scrollView.smoothScrollTo(0, 0)
    }

    private fun setupNews(newsItem: NewsItem) {
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

    private fun initRecyclerView() {
        binding.rvNews.setLinearLayoutManager()
        binding.rvNews.adapter = HomeNewsAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                start(this@NewsDetailActivity, (adapter.data[position] as NewsItem))
            }
        }
    }

    private fun initObservable() {
        viewModel.newsDetail.observe(this) {
            if (currentId != it.first) {
                return@observe
            }

            hideLoading()
            if (it.second == null) {
                return@observe
            }

            val detail = it.second!!
            if (detail.relatedList.isNullOrEmpty()) {
                binding.linNews.gone()
            } else {
                binding.linNews.visible()
                setupRelatedList(detail.relatedList)
            }
        }
    }

    private fun setupRelatedList(newsList: List<NewsItem>) = binding.run {
        (rvNews.adapter as HomeNewsAdapter).setList(newsList)
    }

    private fun getHtmlData(bodyHTML: String): String {
        val head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:auto; height:auto!important;}</style>" +
                "</head>";
        return "<html>$head<body>$bodyHTML</body></html>";
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.okWebContent.destroy()
    }
}