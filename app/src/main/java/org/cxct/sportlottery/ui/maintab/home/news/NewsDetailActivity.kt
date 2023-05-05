package org.cxct.sportlottery.ui.maintab.home.news

import android.content.Context
import android.content.Intent
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityNewsDetailBinding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.betList.BetListViewModel


class NewsDetailActivity : BindingActivity<BetListViewModel, ActivityNewsDetailBinding>(){

    companion object{
        fun start(context: Context,newsItem: NewsItem){
            val intent = Intent(context,NewsDetailActivity::class.java)
            intent.putExtra("newsItem",newsItem)
            context.startActivity(intent)
        }
    }

    private val newsItem by lazy { intent.getParcelableExtra("newsItem") as NewsItem? }

    override fun onInitView(){
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    override fun onInitData() {

    }


}