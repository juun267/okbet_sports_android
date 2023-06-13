package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import com.xuexiang.xupdate.utils.UpdateUtils
import org.cxct.sportlottery.databinding.DialogToGcashBinding
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.KvUtils.GLIFE_TIP_FLAG

/**
 * glife 用户点击存取款跳转gcash
 */
class ToGcashDialog(context: Context) : Dialog(context) {


    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: OnNegativeListener? = null
    private lateinit var binding: DialogToGcashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogToGcashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnGlifeCancel.setOnClickListener {
            KvUtils.put(GLIFE_TIP_FLAG, binding.cbNoReminder.isChecked)
            dismiss()
        }

        binding.btnGlifeOpen.setOnClickListener {
            KvUtils.put(GLIFE_TIP_FLAG, binding.cbNoReminder.isChecked)
            val uri = Uri.parse("https://miniprogram.gcash.com/s01/axXslZ")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            UpdateUtils.startActivity(intent)
            dismiss()
        }


    }

    fun setGoneNoReminder() {
        binding.cbNoReminder.isVisible = false
    }

    fun setPositiveClickListener(positiveClickListener: OnPositiveListener) {
        mPositiveClickListener = positiveClickListener
    }

    fun setNegativeClickListener(negativeClickListener: OnNegativeListener) {
        mNegativeClickListener = negativeClickListener
    }

    interface OnPositiveListener {
        fun positiveClick(isCheck: Boolean)
    }

    interface OnNegativeListener {
        fun negativeClick(isCheck: Boolean)
    }


}