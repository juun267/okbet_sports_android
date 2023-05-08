package org.cxct.sportlottery.ui.maintab.home.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityBetDetailsBinding
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil


class NewsDetailActivity : BaseActivity<MainHomeViewModel>(MainHomeViewModel::class){

    companion object{
        fun start(context: Context,newsItem: NewsItem){
            val intent = Intent(context,NewsDetailActivity::class.java)
            intent.putExtra("newsItem",newsItem)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityNewsDetailBinding
    private val newsItem by lazy { intent.getParcelableExtra("newsItem") as NewsItem? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        newsItem?.let {
            setupNews(it)
        }
        initData()
        initObservable()
    }

    private fun initData(){
        newsItem?.let {
            viewModel.getNewsDetail(it.id)
        }
    }


    private fun setupNews(newsItem: NewsItem){
        binding.apply {
            ivCover.roundOf(newsItem?.image, 12.dp, R.drawable.img_banner01)
            tvTitle.text = newsItem?.title
            tvTime.text = TimeUtil.timeFormat(newsItem?.updateTimeInMillisecond, TimeUtil.YMD_HMS_FORMAT)
            tvContent.text = newsItem?.metaDetail
        }
    }

    private fun initObservable() {
        viewModel.newsDetail.observe(this){
             setupNews(it.detail)
            if(it.relatedList.isNullOrEmpty()){
                binding.linNews.gone()
            }else{
                binding.linNews.visible()
                setupRelatedList(it.relatedList)
            }
        }
    }
    private fun setupRelatedList(newsList: List<NewsItem>) {
        binding.apply {
            if (rvNews.adapter == null) {
                rvNews.layoutManager = LinearLayoutManager(this@NewsDetailActivity, RecyclerView.VERTICAL, false)
                rvNews.adapter = HomeNewsAdapter().apply {
                    setList(newsList)
                    setOnItemClickListener(listener = OnItemClickListener { adapter, view, position ->
                        start(this@NewsDetailActivity,(adapter.data[position] as NewsItem))
                    })
                }
            } else {
                (rvNews.adapter as HomeNewsAdapter).setList(newsList)
            }
        }
    }

}