package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_age_verify.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.makeLinks

class AgeVerifyDialog(
    val activity: FragmentActivity,
    private val onAgeVerifyCallBack: OnAgeVerifyCallBack
) : AlertDialog(activity) {
    //TODO 参考弹窗
    private var isCbChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setLayout(
            ScreenUtil.getScreenWidth(context) - 40.dp,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
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
            if (!cb_agree_statement.isChecked) return@setOnClickListener
            onAgeVerifyCallBack.onConfirm()
            dismiss()
        }

        cb_agree_statement.setOnCheckedChangeListener { _, isChecked ->
            isCbChecked = isChecked
            btn_confirm.apply {
                setBackgroundResource(
                    if (isChecked) R.drawable.bg_radius_4_button_1053af else R.drawable.bg_rectangle_4dp_gray_dark
                )
                setTextColor(
                    ContextCompat.getColor(
                        activity,
                        if (isChecked) R.color.color_FCFCFC else R.color.color_e5e5e5_666666
                    )
                )
            }
        }
        cb_agree_statement.isChecked = false

        tv_statement_link.makeLinks(
            //讓checkBox可點擊的範圍延伸到未變色的文字上
            Pair(
                context.getString(R.string.dialog_age_verify_hint),
                View.OnClickListener {
                    isCbChecked = !isCbChecked
                    cb_agree_statement.isChecked = isCbChecked
                }),
            //有色文字的點擊事件
            Pair(
                context.getString(R.string.terms_conditions),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        context,
                        Constants.getAgreementRuleUrl(context),
                        context.getString(R.string.terms_conditions)
                    )
                })
        )
    }

    interface OnAgeVerifyCallBack {
        fun onConfirm()
        fun onExit()
    }
}
