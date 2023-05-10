package org.cxct.sportlottery.ui.maintab.home.news

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class NewsHomeFragment : org.cxct.sportlottery.ui.base.BindingSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {


    private val NEWS_OKBET_ID = 12
    private val NEWS_SPORT_ID = 13
    private var categoryIds = listOf(NEWS_OKBET_ID)

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
            viewModel.getPageNews(currentPage, PAGE_SIZE, categoryIds)
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
                categoryIds = listOf(if (it.position == 0) NEWS_OKBET_ID else NEWS_SPORT_ID)
                viewModel.getPageNews(1, PAGE_SIZE, categoryIds)
            })
        }
        binding.tvShowMore.setOnClickListener {
            viewModel.getPageNews(currentPage + 1, PAGE_SIZE, categoryIds)
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