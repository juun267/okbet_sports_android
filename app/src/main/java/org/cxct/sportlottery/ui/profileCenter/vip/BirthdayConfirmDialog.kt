package org.cxct.sportlottery.ui.profileCenter.vip

import android.os.Bundle
import org.cxct.sportlottery.databinding.ViewBirthdayConfirmBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import java.text.SimpleDateFormat
import java.util.*

class BirthdayConfirmDialog: BaseDialog<VipViewModel,ViewBirthdayConfirmBinding>() {
    companion object{
        fun newInstance(date: Date) = BirthdayConfirmDialog().apply{
            arguments = Bundle().apply {
                putSerializable("date",date)
            }
        }
    }
    init {
        marginHorizontal = 15.dp
    }
    private val date by lazy { arguments?.getSerializable("date") as Date }

    override fun onInitView() {
        binding.tvDate.text = SimpleDateFormat("MMM dd").format(date)
        binding.btnReturn.setOnClickListener {
            (requireActivity() as VipBenefitsActivity).showBirthday()
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            (requireActivity() as VipBenefitsActivity).setBirthday(date)
            dismiss()
        }
    }
}