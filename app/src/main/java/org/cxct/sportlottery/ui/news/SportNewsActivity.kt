package org.cxct.sportlottery.ui.news

import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySportNewsBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.view.DateRangeSearchView2
import org.cxct.sportlottery.view.loadMore

class SportNewsActivity : BindingActivity<NewsViewModel,ActivitySportNewsBinding>() {
    private val newsAdapter=RecyclerSportNewsAdapter()
    private var timeRangeView: DateRangeSearchView2?=null
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        timeRangeView=DateRangeSearchView2(this)
        newsAdapter.addHeaderView(timeRangeView!!)
        binding.apply {
            toolBar.tvToolbarTitle.text="Announcement"
            toolBar.btnToolbarBack.setOnClickListener {
                onBackPressed()
            }

            recyclerNews.layoutManager=LinearLayoutManager(this@SportNewsActivity)
            recyclerNews.adapter=newsAdapter

            recyclerNews.loadMore {
                viewModel.getSportsNewsData()
            }
        }

    }

    override fun onInitData() {
        viewModel.getSportsNewsData()
        viewModel.sportsNewsList.observe(this){
            newsAdapter.addData(it)
//            newsAdapter.setList(it)
        }
    }
}