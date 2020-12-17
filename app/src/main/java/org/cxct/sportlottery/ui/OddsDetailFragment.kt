package org.cxct.sportlottery.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.OddsDetailFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class OddsDetailFragment : Fragment() {

    companion object {
        fun newInstance() = OddsDetailFragment()
    }

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

    private lateinit var dataBinding: OddsDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.odds_detail_fragment, container, false);
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding.apply {
            oddsDetailViewModel = this@OddsDetailFragment.oddsDetailViewModel
            lifecycleOwner = this@OddsDetailFragment
        }

    }

}