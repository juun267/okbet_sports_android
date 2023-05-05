package org.cxct.sportlottery.ui.maintab.home.news

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_help_center.*
import kotlinx.android.synthetic.main.fragment_home_live.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.databinding.FragmentMainHome2Binding
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.EventBusUtil

class NewsHomeFragment : BindingSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {


    private val NEWS_OKBET_ID = 12
    private val NEWS_SPORT_ID = 13

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment

    override fun onInitView(view: View) {
        super.onInitView(view)
        view.fitsSystemStatus()
        initToolBar()
        initNews()
        initObservable()
        viewModel.getRecommendNews(1,5, listOf(NEWS_OKBET_ID))
    }

    fun initToolBar() = homeToolbar.run {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this@NewsHomeFragment), 0, 0)
        attach(this@NewsHomeFragment, getMainTabActivity(), viewModel)
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showLeftFrament(0, 1)
        }
    }
    private fun initObservable() {
        viewModel.recommendNewsList.observe(this){
            setupNews(it)
        }
    }
    private fun initNews() {
        binding.includeNews.apply {
            tvCateName.text = getString(R.string.N912)
            tabNews.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val categoryId = if (tab?.position == 0) NEWS_OKBET_ID else NEWS_SPORT_ID
                    viewModel.getHomeNews(1, 5, listOf(categoryId))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
            tvMore.gone()
            ivMore.gone()
        }
    }

    private fun setupNews(newsList: List<NewsItem>) {
        binding.includeNews.apply {
            if (rvNews.adapter == null) {
                rvNews.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvNews.adapter = HomeNewsAdapter().apply {
                    setList(newsList)
                    setOnItemClickListener{ adapter, view, position ->
                        NewsDetailActivity.start(requireContext(),(adapter.data[position] as NewsItem))
                    }
                }
            } else {
                (rvNews.adapter as HomeNewsAdapter).setList(newsList)
            }
        }
    }
}