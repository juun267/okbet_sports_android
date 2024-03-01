package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.view.View
import org.cxct.sportlottery.databinding.FragmentEndcardHomeBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class EndCardHomeFragment: BaseFragment<EndCardVM, FragmentEndcardHomeBinding>() {

    override fun onInitView(view: View) {
        binding.root.setBackgroundColor(Color.LTGRAY)
    }
}