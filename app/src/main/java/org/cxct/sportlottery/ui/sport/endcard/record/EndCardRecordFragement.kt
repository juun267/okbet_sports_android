package org.cxct.sportlottery.ui.sport.endcard.record

import android.view.View
import org.cxct.sportlottery.common.extentions.canDelayClick
import org.cxct.sportlottery.databinding.FragmentEndcardRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.Param

class EndCardRecordFragement: BaseFragment<EndCardVM,FragmentEndcardRecordBinding>() {

    private val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            childFragmentManager, binding.flContent.id, arrayOf(
                Param(EndCardSettledRecordFragment::class.java, needRemove = true),
                Param(EndCardUnsettledRecordFragment::class.java,needRemove = true),
            )
        )
    }

    override fun onInitView(view: View) {
        binding.rgTab.setOnCheckedChangeListener { group, checkedId ->
             when(checkedId){
                 binding.rbtnSettled.id -> {
                     if (binding.rbtnSettled.canDelayClick()){
                         fragmentHelper.showFragment(0)
                     }
                 }
                 binding.rbtnUnSettle.id -> {
                     if (binding.rbtnUnSettle.canDelayClick()){
                         fragmentHelper.showFragment(1)
                     }
                 }
             }
        }
        binding.rgTab.check(binding.rbtnSettled.id)
    }
    fun showPage(position: Int){
        fragmentHelper.showFragment(position)
    }
}