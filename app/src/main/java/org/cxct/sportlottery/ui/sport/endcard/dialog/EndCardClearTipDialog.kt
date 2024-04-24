package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.os.Bundle
import org.cxct.sportlottery.databinding.DialogEndcardClearTipBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils

class EndCardClearTipDialog: BaseDialog<BaseViewModel, DialogEndcardClearTipBinding>() {

    companion object{
        fun isNeedShow()= !KvUtils.decodeBoolean(KvUtils.KEY_ENDCARD_CLEAR)
        fun newInstance(oddId: String) = EndCardClearTipDialog().apply {
            arguments = Bundle().apply {
                putString("oddId",oddId)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }

    private val oddId by lazy { arguments?.getString("oddId") }

    override fun onInitView() {
        initClick()
    }
    private fun initClick()=binding.run{
        cbOkIknow.setOnCheckedChangeListener { _, b ->
            KvUtils.put(KvUtils.KEY_ENDCARD_CLEAR,b)
        }
        ivClose.setOnClickListener {
            dismiss()
        }
        btnBet.setOnClickListener {
            dismiss()
            (requireParentFragment() as EndCardBetDialog).addBet()
        }
        btnConfirm.setOnClickListener {
            dismiss()
            oddId?.let { it1 ->
                (requireParentFragment() as EndCardBetDialog).removeItem(it1)
            }
        }
    }
}