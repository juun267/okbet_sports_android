package org.cxct.sportlottery.ui.maintab.home.news

import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.formatHTML
import org.cxct.sportlottery.view.webView.OkWebViewClient
import timber.log.Timber
import java.util.*


class NewsDetailActivity :
    org.cxct.sportlottery.ui.base.BaseActivity<MainHomeViewModel, ActivityNewsDetailBinding>() {

    companion object {
        fun start(context: Context, newsItem: NewsItem) {
            val intent = Intent(context, NewsDetailActivity::class.java)
            intent.putExtra("newsItem", newsItem)
            context.startActivity(intent)
        }
    }

    private var currentId = -1

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        binding.customToolBar.apply {
            binding.tvToolbarTitle.paint.isFakeBoldText = true
            setOnBackPressListener { finish() }
        }
        binding.okWebContent.apply {
            setBackgroundColor(getColor(R.color.color_F8F9FD))
            okWebViewClient = object : OkWebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    Timber.d("shouldOverrideUrlLoading request:${request?.url} path:${request?.url?.path}")
                    JumpUtil.toInternalWeb(this@NewsDetailActivity, request?.url.toString(), "")
                    return true
                }
            }
        }
        binding.bottomView.bindServiceClick(supportFragmentManager)
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
            ivCover.roundOf(newsItem?.image, 10.dp, R.drawable.img_banner01)
            tvTitle.text = newsItem?.title
            tvTime.text = TimeUtil.timeFormat(
                newsItem?.createTimeInMillisecond,
                TimeUtil.NEWS_TIME_FORMAT,
                locale = Locale.ENGLISH)
            okWebContent.loadData((newsItem?.contents ?: "").formatHTML(), "text/html", null)
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
            setupNews(detail.detail)
            if (detail.relatedList.isNullOrEmpty()) {
                binding.linNews.gone()
            } else {
                binding.linNews.visible()
                val dataList = if (detail.relatedList.size > 4) detail.relatedList.subList(
                    0, 4
                ) else detail.relatedList
                setupRelatedList(dataList)
            }
        }
    }

    private fun setupRelatedList(newsList: List<NewsItem>) = binding.run {
        (rvNews.adapter as HomeNewsAdapter).setList(newsList)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.okWebContent.destroy()
    }
}