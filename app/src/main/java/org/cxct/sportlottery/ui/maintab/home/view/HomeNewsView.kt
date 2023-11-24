package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewHomeNewsBinding
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeFragment2
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.util.LogUtil
import splitties.systemservices.layoutInflater

class HomeNewsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding: ViewHomeNewsBinding = ViewHomeNewsBinding.inflate(layoutInflater,this)
    lateinit var viewModel:MainHomeViewModel
    val pageSize = 3
    private val homeHotNewsAdapter = HomeHotNewsAdapter().apply {
        setOnItemClickListener{ adapter, view, position ->
            NewsDetailActivity.start(
                context,
                (adapter.data[position] as NewsItem)
            )
        }
    }
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        rgTitle.setOnCheckedChangeListener { group, checkedId ->
            val categoryId = if (checkedId == R.id.rbtnOkbet) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
            viewModel.getHomeNews(1, pageSize, listOf(categoryId))
        }
        binding.rvNews.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvNews.adapter = homeHotNewsAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        viewModel = fragment.viewModel
        viewModel.homeNewsList.observe(fragment) {
            val dataList = if (it.size > pageSize) it.subList(0, pageSize) else it
            homeHotNewsAdapter.setList(dataList)
        }
        binding.tvMore.setOnClickListener {
            (fragment.activity as MainTabActivity).jumpToNews()
        }
        viewModel.getHomeNews(1, pageSize, listOf(NewsRepository.NEWS_OKBET_ID))
    }

}