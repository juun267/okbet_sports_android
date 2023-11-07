package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.databinding.ViewHomeNewsBinding
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.news.HomeNewsAdapter
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import splitties.systemservices.layoutInflater

class HomeNewsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding: ViewHomeNewsBinding = ViewHomeNewsBinding.inflate(layoutInflater,this,true)
    lateinit var viewModel:MainHomeViewModel
    private val homeNewsAdapter = HomeNewsAdapter().apply {
        setOnItemClickListener{ adapter, view, position ->
            NewsDetailActivity.start(
                context,
                (adapter.data[position] as NewsItem)
            )
        }
    }
    init {
        initView()
    }

    private fun initView() =binding.run {
        tabNews.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as TextView).typeface = Typeface.DEFAULT_BOLD
                val categoryId = if (tab.position == 0) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
                viewModel.getHomeNews(1, 5, listOf(categoryId))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.view.getChildAt(1) as TextView).typeface = Typeface.DEFAULT
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        linTab.setOnClickListener {
//            getHomeFragment().jumpToNews()
        }
        binding.rvNews.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvNews.adapter = homeNewsAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        viewModel = fragment.viewModel
        viewModel.homeNewsList.observe(fragment) {
            val dataList = if (it.size > 4) it.subList(0, 4) else it
            homeNewsAdapter.setList(dataList)
        }
        viewModel.getHomeNews(1, 5, listOf(NewsRepository.NEWS_OKBET_ID))
    }

}