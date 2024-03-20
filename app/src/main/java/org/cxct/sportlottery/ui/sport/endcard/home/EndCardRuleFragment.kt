package org.cxct.sportlottery.ui.sport.endcard.home

import android.view.View
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentEndcardRuleBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM


class EndCardRuleFragment: BaseFragment<EndCardVM, FragmentEndcardRuleBinding>() {

    override fun onInitView(view: View) {
        binding.linBack.setOnClickListener {
            (activity as EndCardActivity).removeFragment(this)
        }
        binding.okWebView.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.color_1A202E))
        binding.okWebView.loadUrl(Constants.getEndCardRuleUrl())
    }

}