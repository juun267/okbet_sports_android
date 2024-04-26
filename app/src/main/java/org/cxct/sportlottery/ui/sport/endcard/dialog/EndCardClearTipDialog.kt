package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.databinding.DialogEndcardClearTipBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils

class EndCardClearTipDialog: BaseDialog<BaseViewModel, DialogEndcardClearTipBinding>() {

    companion object{
        fun isNeedShow()= !KvUtils.decodeBoolean(KvUtils.KEY_ENDCARD_CLEAR)
        fun newInstance() = EndCardClearTipDialog()
    }
    init {
        marginHorizontal = 12.dp
    }

    var onConfirm: (()->Unit)?=null

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
            (requireParentFragment() as EndCardGameFragment).startBet()
        }
        btnConfirm.setOnClickListener {
            dismiss()
            onConfirm?.invoke()
        }
    }
}