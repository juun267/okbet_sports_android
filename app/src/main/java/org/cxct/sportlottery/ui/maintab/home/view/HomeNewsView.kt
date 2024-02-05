package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.core.view.marginRight
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.include_home_news.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.databinding.TabHomeNewsBinding
import org.cxct.sportlottery.databinding.ViewHomeNewsBinding
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.RCVDecoration
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import splitties.systemservices.layoutInflater

class HomeNewsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomeNewsBinding.inflate(layoutInflater,this)
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
        tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            getSelectCategoryId()?.let {
                viewModel.getHomeNews(1, pageSize, listOf(it))
            }
        })
        binding.rvNews.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvNews.adapter = homeHotNewsAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        viewModel = fragment.viewModel
        viewModel.newsCategory.observe(fragment) {
            binding.tabNews.removeAllTabs()
            binding.root.isVisible = it.isNotEmpty()
            it.forEach {
                val itemBinding = TabHomeNewsBinding.inflate(layoutInflater)
                binding.tabNews.addTab(binding.tabNews.newTab().setTag(it.id).setCustomView(itemBinding.root))
                itemBinding.tvTitle.text = it.categoryName
            }
            binding.tabNews.getTabAt(0)?.select()
            getSelectCategoryId()?.let {
                viewModel.getHomeNews(1, pageSize, listOf(it))
            }
        }
        viewModel.homeNewsList.observe(fragment) {
            val dataList = if (it.size > pageSize) it.subList(0, pageSize) else it
            homeHotNewsAdapter.setList(dataList)
        }
        binding.tvMore.setOnClickListener {
            (fragment.activity as MainTabActivity).jumpToNews()
        }
        viewModel.getNewsCategory()
    }
    private fun getSelectCategoryId():Int?=binding.tabNews.getTabAt(binding.tabNews.selectedTabPosition)?.tag as? Int

}