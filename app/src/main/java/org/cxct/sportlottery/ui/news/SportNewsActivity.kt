package org.cxct.sportlottery.ui.news

import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySportNewsBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.view.DateRangeSearchView2
import org.cxct.sportlottery.view.loadMore

/**
 * 体育侧边栏新闻列表
 */
class SportNewsActivity : BindingActivity<NewsViewModel,ActivitySportNewsBinding>() {
    private val newsAdapter=RecyclerSportNewsAdapter()
    private var timeRangeView: DateRangeSearchView2?=null
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)

        timeRangeView=DateRangeSearchView2(this)
        //初始化查询时间
        viewModel.sportStartTime=timeRangeView?.startTime
        viewModel.sportEndTime=timeRangeView?.endTime

        newsAdapter.addHeaderView(timeRangeView!!)
        binding.apply {
            toolBar.tvToolbarTitle.text=getString(R.string.LT054COPY)
            toolBar.btnToolbarBack.setOnClickListener {
                onBackPressed()
            }

            recyclerNews.layoutManager=LinearLayoutManager(this@SportNewsActivity)
            recyclerNews.adapter=newsAdapter

            //加载更多
            recyclerNews.loadMore {
                viewModel.getSportsNewsData()
            }
        }

        //点击按时间查询
        timeRangeView?.setOnClickSearchListener {
            newsAdapter.data.clear()
            viewModel.sportPageIndex=1
            viewModel.sportStartTime=timeRangeView?.startTime
            viewModel.sportEndTime=timeRangeView?.endTime
            viewModel.getSportsNewsData()
        }

    }

    override fun onInitData() {
        //请求新闻数据
        viewModel.getSportsNewsData()
        viewModel.sportsNewsList.observe(this){
            newsAdapter.addData(it)
        }
    }
}