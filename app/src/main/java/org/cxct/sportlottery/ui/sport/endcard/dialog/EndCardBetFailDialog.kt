package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.os.Bundle
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetFailBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndCardBetFailDialog: BaseDialog<BaseViewModel, DialogEndcardBetFailBinding>() {

    companion object{
        fun newInstance(msg: String)= EndCardBetFailDialog().apply {
            arguments = Bundle().apply {
                putString("msg",msg)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }
    private val msg by lazy { arguments?.getString("msg") }

    override fun onInitView() {
        initClick()
        binding.tvMsg.text = msg
    }
    private fun initClick()=binding.run{
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
    }
}