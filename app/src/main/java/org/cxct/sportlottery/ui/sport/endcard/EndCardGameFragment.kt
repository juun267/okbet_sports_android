package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.view.View
import org.cxct.sportlottery.databinding.FragmentEndcardgameBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class EndCardGameFragment: BaseFragment<EndCardVM, FragmentEndcardgameBinding>() {

    override fun onInitView(view: View) {
        binding.root.setBackgroundColor(Color.LTGRAY)
    }




}