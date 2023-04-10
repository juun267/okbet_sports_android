package org.cxct.sportlottery.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import org.cxct.sportlottery.databinding.FragmentNewsDeatilBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper

/**
 * @app_destination 最新消息-詳情
 */
class NewsDetailFragment : BaseFragment<NewsViewModel>(NewsViewModel::class) {
    private var _binding: FragmentNewsDeatilBinding? = null

    private val binding get() = _binding!!

    private val args: NewsDetailFragmentArgs? by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewsDeatilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}