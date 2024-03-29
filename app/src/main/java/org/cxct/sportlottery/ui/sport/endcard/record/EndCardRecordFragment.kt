package org.cxct.sportlottery.ui.sport.endcard.record

import android.view.View
import org.cxct.sportlottery.common.extentions.canDelayClick
import org.cxct.sportlottery.databinding.FragmentEndcardRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.Param
import org.cxct.sportlottery.util.setSelectorTypeFace

class EndCardRecordFragment: BaseFragment<EndCardVM,FragmentEndcardRecordBinding>() {

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
                     fragmentHelper.showFragment(0)
                 }
                 binding.rbtnUnSettle.id -> {
                     fragmentHelper.showFragment(1)
                 }
             }
            group.setSelectorTypeFace(AppFont.helvetica_light, AppFont.helvetica)
        }
        binding.rgTab.check(binding.rbtnUnSettle.id)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            when(val fragment=fragmentHelper.getCurrentFragment()){
                is EndCardSettledRecordFragment-> fragment.reload()
                is EndCardUnsettledRecordFragment-> fragment.reload()
            }
        }
    }
    fun showPage(position: Int){
        if (isAdded) {
            if (position == 0) {
                if (binding.rbtnSettled.isChecked) {
                    fragmentHelper.showFragment(position)
                } else {
                    binding.rbtnSettled.isChecked = true
                }
            } else {
                if (binding.rbtnUnSettle.isChecked) {
                    fragmentHelper.showFragment(position)
                } else {
                    binding.rbtnUnSettle.isChecked = true
                }
            }
        }
    }
}