package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogEndcardDeleteGuideBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils

class EndCardDeleteGuideDialog: BaseDialog<BaseViewModel, DialogEndcardDeleteGuideBinding>() {

    companion object{
        fun needShow()= !KvUtils.decodeBoolean(KvUtils.KEY_ENDCARD_DELETE_GUIDE)
        fun newInstance(rect: Rect)= EndCardDeleteGuideDialog().apply {
            arguments = Bundle().apply {
                putParcelable("rect",rect)
            }
        }
    }
    init {
        setStyle(R.style.FullScreen)
    }
    private val rect by lazy { arguments?.getParcelable<Rect>("rect") }

    override fun onInitView() {
        KvUtils.put(KvUtils.KEY_ENDCARD_DELETE_GUIDE,true)
        rect?.let {
            val lp = (binding.ivDeleteBtn.layoutParams as MarginLayoutParams)
            lp.setMargins(it.left-5.dp,it.top-5.dp-ImmersionBar.getStatusBarHeight(this),0,0)
            binding.ivDeleteBtn.layoutParams = lp
        }
       binding.btnConfirm.setOnClickListener {
           dismiss()
       }
    }

}