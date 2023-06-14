package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import com.xuexiang.xupdate.utils.UpdateUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogToGcashBinding
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.KvUtils.GLIFE_TIP_FLAG

/**
 * glife 用户点击存取款跳转gcash
 */
class ToGcashDialog(context: Context, val visibleNoReminder: Boolean = true) : Dialog(context) {

    companion object{
        /**
         * 登录后才需要显示，加全局状态值区分
         */
        var needShow = false

        /**
         * 根据条件判断是否需要显示
         */
        fun allowShow(viewModel: BaseSocketViewModel){
            if (viewModel.getLoginBoolean() && viewModel.userInfo.value?.vipType == 1) {
                if (!KvUtils.decodeBooleanTure(KvUtils.GLIFE_TIP_FLAG, false)&&needShow) {
                    needShow=false
                    ToGcashDialog(AppManager.currentActivity()).show()
                }
            }
        }
    }


    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: OnNegativeListener? = null
    private lateinit var binding: DialogToGcashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogToGcashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnGlifeCancel.setOnClickListener {
            KvUtils.put(GLIFE_TIP_FLAG, binding.cbNoReminder.isChecked)
            dismiss()
        }
        binding.cbNoReminder.isVisible = visibleNoReminder
        binding.btnGlifeOpen.text = context.getString(R.string.LT028)+" "+context.getString(R.string.online_gcash)
        binding.btnGlifeOpen.setOnClickListener {
            KvUtils.put(GLIFE_TIP_FLAG, binding.cbNoReminder.isChecked)
            val uri = Uri.parse("https://miniprogram.gcash.com/s01/SBMk5e")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            UpdateUtils.startActivity(intent)
            dismiss()
        }


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