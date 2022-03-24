package org.cxct.sportlottery.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.databinding.FragmentNewsDeatilBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class NewsDetailFragment : BaseFragment<NewsViewModel>(NewsViewModel::class) {
    private var _binding: FragmentNewsDeatilBinding? = null

    private val binding get() = _binding!!

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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}