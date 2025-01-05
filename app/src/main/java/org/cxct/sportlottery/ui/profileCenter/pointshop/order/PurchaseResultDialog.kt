package org.cxct.sportlottery.ui.profileCenter.pointshop.order

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.DialogPurchaseResultBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.profileCenter.pointshop.OrderDetailActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.record.PointExchangeActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.util.DisplayUtil.dp

class PurchaseResultDialog: BaseDialog<BaseViewModel,DialogPurchaseResultBinding>() {

    companion object{
        const val STATUS_SUCCESS = 0
        const val STATUS_FAIL = 1
        fun newInstance(status: Int, message: String)= PurchaseResultDialog().apply{
            arguments = Bundle().apply {
                putInt("status",status)
                putString("message",message)
            }
        }
    }
    init {
        marginHorizontal = 38.dp
    }

    private val status by lazy { arguments?.getInt("status") }
    private val message by lazy { arguments?.getString("message") }

    override fun onInitView() = binding.run{
        isCancelable = true
        if (status == STATUS_SUCCESS){
            ivStatus.setImageResource(R.drawable.ic_order_success)
            tvName.text = message
            tvCheck.text = getString(R.string.B208)
        }else{
            ivStatus.setImageResource(R.drawable.ic_order_error)
            tvName.text = message
            tvCheck.text = getString(R.string.B209)
        }
        tvCheck.setOnClickListener {
            dismiss()
            if (status == STATUS_SUCCESS){
                startActivity(Intent(requireActivity(), PointExchangeActivity::class.java))
                requireActivity().finish()
            }else{
                requireActivity().finish()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        startActivity(Intent(requireContext(),PointShopActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        requireActivity().finish()
    }
}