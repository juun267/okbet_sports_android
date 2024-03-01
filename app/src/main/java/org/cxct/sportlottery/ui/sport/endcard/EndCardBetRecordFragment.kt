package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.view.View
import org.cxct.sportlottery.databinding.FragmentEndcardBetrecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class EndCardBetRecordFragment: BaseFragment<EndCardVM, FragmentEndcardBetrecordBinding>() {

    override fun onInitView(view: View) {
        binding.root.setBackgroundColor(Color.GRAY)
    }

}