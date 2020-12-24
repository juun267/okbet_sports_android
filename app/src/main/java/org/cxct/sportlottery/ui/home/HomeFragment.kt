package org.cxct.sportlottery.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class HomeFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private lateinit var homeBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeBinding.apply {
            mainViewModel = this@HomeFragment.viewModel
            lifecycleOwner = this@HomeFragment
        }
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshView()
        queryData()
    }

    private fun queryData() {
        viewModel.getMatchPreload()
    }

    private fun refreshView() {
        viewModel.earlyGameResult.observe(viewLifecycleOwner, Observer {
            drawer_early.setCount(it.matchPreloadData?.num.toString())
        })

        viewModel.inPlayGameResult.observe(viewLifecycleOwner, Observer {
            drawer_in_play.setCount(it.matchPreloadData?.num.toString())
        })

        viewModel.todayGameResult.observe(viewLifecycleOwner, Observer {
            drawer_today.setCount(it.matchPreloadData?.num.toString())
        })
    }
}