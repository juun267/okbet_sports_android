package org.cxct.sportlottery.ui.sport.endcard.dialog

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardGuideBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class EndCardGuideDialog: BaseDialog<BaseViewModel, DialogEndcardGuideBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    private val views by lazy { listOf(
        binding.includeStep1,
        binding.includeStep2,
        binding.includeStep3,
        binding.includeStep4,
        binding.includeStep5)
    }
    private var currentPage = 0

    override fun onInitView() {
        initClick()
        showPage(currentPage)
    }
    private fun initClick()=binding.run{
        setOnClickListeners(includeStep2.tvPrevious,includeStep3.tvPrevious,includeStep4.tvPrevious,includeStep5.tvPrevious){
            showPage(currentPage-1)
        }
        setOnClickListeners(includeStep1.linNextStep,includeStep2.linNextStep,includeStep3.linNextStep,includeStep4.linNextStep){
            showPage(currentPage+1)
        }
        includeStep5.linNextStep.setOnClickListener { dismiss() }
    }

    private fun showPage(position:Int){
        if (position<0 || position > views.size-1)
            return
        currentPage = position
        views.forEachIndexed { index, viewBinding ->
            viewBinding.root.isVisible =  position==index
        }
    }
}