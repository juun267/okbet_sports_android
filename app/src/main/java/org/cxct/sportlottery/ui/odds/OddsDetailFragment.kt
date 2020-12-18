package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.ui.home.HomeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val ARG_PARAM = "param"

class OddsDetailFragment : Fragment() {

    companion object {
        fun newInstance(param: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, param)
                }
            }
    }

    private var param: String? = null

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

    private lateinit var dataBinding: FragmentOddsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param = it.getString(ARG_PARAM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false);
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding.apply {
            oddsDetailViewModel = this@OddsDetailFragment.oddsDetailViewModel
            lifecycleOwner = this@OddsDetailFragment
        }

        //test
        for (i in 0 until 6) {
            tab_cat.addTab(tab_cat.newTab())
        }

        tab_cat.getTabAt(0)?.text = "受欢迎的"
        tab_cat.getTabAt(1)?.text = "所有盘口"
        tab_cat.getTabAt(2)?.text = "让球/大小"
        tab_cat.getTabAt(3)?.text = "波胆"
        tab_cat.getTabAt(4)?.text = "进球"
        tab_cat.getTabAt(5)?.text = "特殊投注TEST"

    }

}