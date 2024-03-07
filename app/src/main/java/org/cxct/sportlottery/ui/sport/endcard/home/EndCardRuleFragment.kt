package org.cxct.sportlottery.ui.sport.endcard.home

import android.view.View
import org.cxct.sportlottery.databinding.FragmentEndcardRuleBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM


class EndCardRuleFragment: BaseFragment<EndCardVM, FragmentEndcardRuleBinding>() {

    override fun onInitView(view: View) {
        binding.linBack.setOnClickListener {
            (activity as EndCardActivity).removeFragment(this)
        }
        binding.okWebView.loadUrl("")
    }

}