package org.cxct.sportlottery.ui.betRecord.dialog

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.SingleToast
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.drawable.DrawableCreator
import java.util.Locale

class PrintDialog(mContext: Context) : Dialog(mContext) {

    init {
        initDialog()
    }

    var tvPrintClickListener: ((String?) -> Unit)? = null

    private fun initDialog() {

        setContentView(R.layout.dialog_print_enter_full_name)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val etInputName = findViewById<EditText>(R.id.etInputFullName)
        val tvCancel = findViewById<TextView>(R.id.tvCancel)
        val tvPrint = findViewById<TextView>(R.id.tvPrint)
        val tvTips = findViewById<TextView>(R.id.tvTips)

        val tvTipsBg = DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setSolidColor(context.getColor(R.color.transparent_black_60)).build()

        tvTips.background = tvTipsBg

        val etBg = DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeColor(context.getColor(R.color.color_e3e8ee)).setStrokeWidth(1.dp.toFloat())
            .setSolidColor(context.getColor(R.color.color_FFFFFF)).build()

        etInputName.setHintTextColor(context.getColor(R.color.color_A7B2C4))

        etInputName.background = etBg
        tvCancel.setOnClickListener {
            dismiss()

        }
        tvPrint.setOnClickListener {
            if (etInputName.text.isEmpty()) {
                tvTips.visible()
                tvPrint.postDelayed({
                    tvTips.gone()
                }, 2000)
                return@setOnClickListener
            }
            tvPrintClickListener?.invoke(etInputName.text.toString())

        }

    }

}