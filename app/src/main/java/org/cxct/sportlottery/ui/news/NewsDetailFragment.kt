package org.cxct.sportlottery.ui.news

import android.view.View
import androidx.navigation.fragment.navArgs
import org.cxct.sportlottery.databinding.FragmentNewsDeatilBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper

/**
 * @app_destination 最新消息-詳情
 */
class NewsDetailFragment : BaseFragment<NewsViewModel,FragmentNewsDeatilBinding>() {

    private val args: NewsDetailFragmentArgs? by navArgs()

    override fun onInitView(view: View) {
        initViews()
    }

    private fun initViews() {
        initContent()
    }

    private fun initContent() {
        args?.news?.let { news ->
            with(binding) {
                tvTitle.text = news.title
                tvContent.text = news.message
                tvDate.text = news.showDate

                OverScrollDecoratorHelper.setUpOverScroll(scrollView)
            }
        }
    }

}