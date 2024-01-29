package org.cxct.sportlottery.ui.maintab.home.news

import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.include_home_news.*
import kotlinx.android.synthetic.main.view_home_news.rvNews
import kotlinx.android.synthetic.main.view_home_news.tvCateName
import kotlinx.android.synthetic.main.view_home_news.tvMore
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class NewsHomeFragment : BaseSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {

    private var categoryId = NewsRepository.NEWS_OKBET_ID

    private inline fun getMainTabActivity() = activity as MainTabActivity
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
        tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true, Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
        }
    }

    private fun initRecyclerView() = rvNews.run {
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
        tvCateName.text = getString(R.string.N912)
        tvMore.gone()
//            ivMore.gone()
        tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            categoryId = if (tab.position == 0) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
            viewModel.getPageNews(1, PAGE_SIZE, categoryId)
        })
        binding.tvShowMore.setOnClickListener {
            viewModel.getPageNews(currentPage + 1, PAGE_SIZE, categoryId)
        }
        binding.bottomView.bindServiceClick(childFragmentManager)
    }

    private fun setupNews(pageNum: Int, newsList: List<NewsItem>) {
        (rvNews.adapter as HomeNewsAdapter).apply {
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
                    "${getString(R.string.N885)} (${pageInfo.totalSize - pageInfo.pageNum * PAGE_SIZE})"
            }
        }
    }
}