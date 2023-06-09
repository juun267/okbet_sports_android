package org.cxct.sportlottery.ui.news

import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySportNewsBinding
import org.cxct.sportlottery.ui.base.BindingActivity

class SportNewsActivity : BindingActivity<NewsViewModel,ActivitySportNewsBinding>() {
    private val newsAdapter=RecyclerSportNewsAdapter()

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)

        binding.apply {
            toolBar.tvToolbarTitle.text="Announcement"
            toolBar.btnToolbarBack.setOnClickListener {
                onBackPressed()
            }

            recyclerNews.layoutManager=LinearLayoutManager(this@SportNewsActivity)
            recyclerNews.adapter=newsAdapter
        }



        newsAdapter.setList(arrayListOf("","","","","","",""))
    }
}