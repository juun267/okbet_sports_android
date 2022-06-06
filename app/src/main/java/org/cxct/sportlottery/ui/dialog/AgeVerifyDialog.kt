package org.cxct.sportlottery.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_age_verify.*
import org.cxct.sportlottery.R

class AgeVerifyDialog(
    val activity: FragmentActivity,
    private val onAgeVerifyCallBack: OnAgeVerifyCallBack
) : AlertDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_age_verify)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false) //設置無法點擊外部關閉
        setCancelable(false) //設置無法點擊 Back 關閉
        initView()
    }

    private fun initView() {
        btn_exit.setOnClickListener {
            onAgeVerifyCallBack.onExit()
            dismiss()
        }

        btn_confirm.setOnClickListener {
            onAgeVerifyCallBack.onConfirm()
            dismiss()
        }
    }

    interface OnAgeVerifyCallBack {
        fun onConfirm()
        fun onExit()
    }
}
