package org.cxct.sportlottery.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {

    private val mainViewModel by activityViewModels<BaseViewModel>() //TODO simon test 研究如何引入 activity 的 viewModel
    private val homeViewModel by inject<HomeViewModel>()
    private lateinit var homeBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeBinding.apply {
//            mainViewModel = this@HomeFragment.mainViewModel as MainViewModel
            homeViewModel = this@HomeFragment.homeViewModel
            lifecycleOwner = this@HomeFragment
        }
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        queryData()
    }

    private fun queryData() {
        homeViewModel.getMatchPreload()
    }
}
