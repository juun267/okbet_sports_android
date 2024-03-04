package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration

class EndCardBetDialog: BaseDialog<BaseViewModel, DialogEndcardBetBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    private val oddAdapter by lazy { EndCardBetOddAdapter() }

    override fun onInitView() {
        initClick()
        initOddList()
    }
    private fun initClick()=binding.run{
        setOnClickListeners(root,ivClose){
            dismiss()
        }
        btnAddMore.setOnClickListener {
            EndCardBetFailDialog().show(parentFragmentManager)
            dismiss()
        }
        btnBetting.setOnClickListener {
            EndCardBetSuccessDialog().show(parentFragmentManager)
            dismiss()
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,5)
            addItemDecoration(GridItemDecoration(6.dp,8.dp, Color.TRANSPARENT,false))
            adapter = oddAdapter
            oddAdapter.setList(listOf("","","","","","","","","","","","",""))
        }
    }
}