package org.cxct.sportlottery.ui.maintab.home.news

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_news_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.EventBusUtil

class NewsHomeFragment : BindingSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {


    private val NEWS_OKBET_ID = 12
    private val NEWS_SPORT_ID = 13
    private var categoryIds = listOf(NEWS_OKBET_ID)

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment
    private val PAGE_SIZE = 4
    private var currentPage = 1;

    override fun onInitView(view: View) {
        super.onInitView(view)
        initToolBar()
        initNews()
        initObservable()
        viewModel.getPageNews(currentPage, PAGE_SIZE, categoryIds)
    }

    fun initToolBar() = homeToolbar.run {
        attach(this@NewsHomeFragment, getMainTabActivity(), viewModel)
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showMainLeftMenu(this@NewsHomeFragment.javaClass)
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
            tabNews.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    categoryIds = listOf(if (tab?.position == 0) NEWS_OKBET_ID else NEWS_SPORT_ID)
                    viewModel.getPageNews(1, PAGE_SIZE, categoryIds)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
            tvMore.gone()
            ivMore.gone()
        }
        binding.tvShowMore.setOnClickListener {
            viewModel.getPageNews(currentPage + 1, PAGE_SIZE, categoryIds)
        }
    }

    private fun setupNews(pageNum: Int, newsList: List<NewsItem>) {
        binding.includeNews.apply {
            if (rvNews.adapter == null) {
                rvNews.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvNews.adapter = HomeNewsAdapter().apply {
                    setList(newsList)
                    setOnItemClickListener { adapter, view, position ->
                        NewsDetailActivity.start(requireContext(),
                            (adapter.data[position] as NewsItem))
                    }
                }
            } else {
                (rvNews.adapter as HomeNewsAdapter).apply {
                    if (pageNum > 1) {
                        addData(newsList)
                    } else {
                        setList(newsList)
                    }
                }
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