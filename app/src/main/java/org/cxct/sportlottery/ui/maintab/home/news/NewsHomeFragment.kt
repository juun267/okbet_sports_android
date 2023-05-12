package org.cxct.sportlottery.ui.maintab.home.news

import android.view.View
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class NewsHomeFragment : BindingSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {

    private var categoryId = NewsRepository.NEWS_OKBET_ID

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment
    private val PAGE_SIZE = 4
    private var currentPage = 1;

    override fun onInitView(view: View) {
        initToolBar()
        initRecyclerView()
        initNews()

    }

    override fun onInitData() {
        initObservable()
        if (currentPage == 1) { // 第一次进来的时候
            viewModel.getPageNews(currentPage, PAGE_SIZE, categoryId)
        }
    }

    fun initToolBar() = binding.homeToolbar.run {
        attach(this@NewsHomeFragment, getMainTabActivity(), viewModel)
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showMainLeftMenu(this@NewsHomeFragment.javaClass)
        }
    }

    private fun initRecyclerView() = binding.includeNews.rvNews.run {
        layoutManager = setLinearLayoutManager()
        adapter = HomeNewsAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                NewsDetailActivity.start(requireContext(),(adapter.data[position] as NewsItem))
            }
        }
    }

    private fun initObservable() {
        viewModel.pageNewsList.observe(this) {
            currentPage = it.pageNum
            setupNews(it.pageNum, it.records ?: listOf())
            setupShowMore(it)
        }
    }
    private fun initNews() {
        binding.includeNews.apply {
            tvCateName.text = getString(R.string.N912)
            tvMore.gone()
            ivMore.gone()
            tabNews.addOnTabSelectedListener(TabSelectedAdapter {
                categoryId = if (it.position == 0) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
                viewModel.getPageNews(1, PAGE_SIZE, categoryId)
            })
        }
        binding.tvShowMore.setOnClickListener {
            viewModel.getPageNews(currentPage + 1, PAGE_SIZE, categoryId)
        }
    }

    private fun setupNews(pageNum: Int, newsList: List<NewsItem>) {
        (binding.includeNews.rvNews.adapter as HomeNewsAdapter).apply {
            if (pageNum > 1) {
                addData(newsList)
            } else {
                setList(newsList)
            }
        }
    }

    private fun setupShowMore(pageInfo: PageInfo<NewsItem>) {
        binding.tvShowMore.apply {
            isVisible = pageInfo.pageNum < pageInfo.totalNum
            if (isVisible) {
                text =
                    "${getString(R.string.display_more)} (${pageInfo.totalSize - pageInfo.pageNum * PAGE_SIZE})"
            }
        }
    }
}