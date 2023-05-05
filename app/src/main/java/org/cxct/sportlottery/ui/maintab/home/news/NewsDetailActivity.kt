package org.cxct.sportlottery.ui.maintab.home.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetDetailsBinding
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.betList.BetListViewModel


class NewsDetailActivity : BaseActivity<BetListViewModel>(BetListViewModel::class){

    companion object{
        fun start(context: Context,newsItem: NewsItem){
            val intent = Intent(context,NewsDetailActivity.javaClass)
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
        initView()
        initData()

    }

    private fun initData(){

    }


    private fun initView(){
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }


}