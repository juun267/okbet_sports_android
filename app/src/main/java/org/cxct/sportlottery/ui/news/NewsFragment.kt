package org.cxct.sportlottery.ui.news

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentNewsBinding
import org.cxct.sportlottery.network.common.NewsType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.util.SpaceItemDecoration

class NewsFragment : BaseFragment<NewsViewModel>(NewsViewModel::class) {
    private var _binding: FragmentNewsBinding? = null

    private val binding get() = _binding!!

    private val tabLayoutSelectedListener by lazy {
        object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabCustomView = tab?.customView
                tabCustomView?.let {
                    val tabTextView: TextView = it as TextView
                    with(tabTextView) {
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(context, R.color.color_e5e5e5_333333))
                    }
                }
                when (tab?.position) {
                    0 -> newsType = NewsType.GAME
                    1 -> newsType = NewsType.SYSTEM
                    2 -> newsType = NewsType.PLAT
                }

                queryNewsData(true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                val tabCustomView = tab?.customView
                tabCustomView?.let {
                    val tabTextView: TextView = it as TextView
                    with(tabTextView) {
                        setTypeface(null, Typeface.NORMAL)
                        setTextColor(ContextCompat.getColor(context, R.color.color_A3A3A3_666666))
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        }
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1)) {
                queryNewsData()
            }
        }
    }

    private val newsAdapter by lazy {
        NewsAdapter(NewsAdapter.NewsListener(onClickDetail = { news ->
            val action = NewsFragmentDirections.actionNewsFragmentToNewsDetailFragment(news)
            view?.findNavController()?.navigate(action)
        }, onLoadMoreData = {
            queryNewsData()
        }))
    }

    private var newsType: NewsType = NewsType.GAME

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
        queryNewsData(true)
    }

    private fun initViews() {
        initTabLayout()
        initRvNews()
    }

    private fun initTabLayout() {
        with(binding.tabLayout) {
            //region 設置TabCustomView 為了將選中Tab文字粗體
            for (i in 0..tabCount) {
                val tab = getTabAt(i)
                if (tab != null) {
                    val tabTextView = TextView(context)
                    tab.customView = tabTextView

                    with(tabTextView.layoutParams) {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }

                    tabTextView.text = tab.text
                }
            }

            when (newsType) {
                NewsType.GAME -> getTabAt(0)
                NewsType.SYSTEM -> getTabAt(1)
                NewsType.PLAT -> getTabAt(2)
            }?.let { selectedTab ->
                selectedTab.select()
                with((selectedTab.customView as TextView)) {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.color_e5e5e5_333333))
                }
            }
            //endregion

            addOnTabSelectedListener(tabLayoutSelectedListener)
        }
    }

    private fun initRvNews() {
        with(binding.rvNews) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = newsAdapter
            addItemDecoration(SpaceItemDecoration(context, R.dimen.recyclerview_news_item_dec_spec))
            addOnScrollListener(recyclerViewOnScrollListener)

            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        }
    }

    private fun queryNewsData(refresh: Boolean = false) {
        viewModel.getNewsData(newsType, refresh)
    }

    private fun initObservers() {
        viewModel.newsList.observe(viewLifecycleOwner, {
            it?.let {
                newsAdapter.newsList = it
            }
        })

        viewModel.showAllNews.observe(viewLifecycleOwner, {
            newsAdapter.showAllNews = it
        })

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        with(binding) {
            tabLayout.removeOnTabSelectedListener(tabLayoutSelectedListener)
            rvNews.removeOnScrollListener(recyclerViewOnScrollListener)
        }

        _binding = null
    }
}