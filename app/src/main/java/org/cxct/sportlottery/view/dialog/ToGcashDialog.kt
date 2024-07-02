package org.cxct.sportlottery.view.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import com.xuexiang.xupdate.utils.UpdateUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogToGcashBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.glifeUserWithdrawEnable
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.KvUtils
import splitties.bundle.put

/**
 * glife 用户点击存取款跳转gcash
 */
class ToGcashDialog : BaseDialog<BaseViewModel,DialogToGcashBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{

        //glife用户存取款提示弹窗点击了  不再提示标记
        private const val GLIFE_TIP_FLAG = "glife_tip_flag"

        private fun markGLife(isChecked: Boolean) {
            KvUtils.put(GLIFE_TIP_FLAG, isChecked)
        }

        private fun isCheckedGLife(): Boolean {
            return KvUtils.decodeBooleanTure(GLIFE_TIP_FLAG, false)
        }

        /**
         * 登录后才需要显示，加全局状态值区分
         */
        var needShow = false

        fun newInstance(visibleNoReminder: Boolean = true): ToGcashDialog{
            val args = Bundle()
            args.put("visibleNoReminder",visibleNoReminder)
            val fragment = ToGcashDialog()
            fragment.arguments = args
            return fragment
        }
        /**
         * 根据条件判断是否需要显示
         */
        fun showByLogin(){
            if (LoginRepository.isLogined() && UserInfoRepository.isGlifeAccount()) {
                if (!isCheckedGLife() && needShow) {
                    needShow = false
                    newInstance().show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToGcashDialog.javaClass.name)
                }
            }
        }
        fun showByClick(next: () -> Unit){

            if (LoginRepository.isLogined() && UserInfoRepository.isGlifeAccount() && !glifeUserWithdrawEnable()) {
                newInstance(false).show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToGcashDialog.javaClass.name)
                return
            }
            next.invoke()
        }
    }

    private val visibleNoReminder by lazy { requireArguments().getBoolean("visibleNoReminder") }
    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: OnNegativeListener? = null


    override fun onInitView() {
        isCancelable = false
        binding.btnGlifeCancel.setOnClickListener {
            if (visibleNoReminder) {
                markGLife(binding.cbNoReminder.isChecked)
            }
            dismiss()
        }
        binding.cbNoReminder.isVisible = visibleNoReminder
        binding.btnGlifeOpen.text = requireContext().getString(R.string.LT028)+" "+requireContext().getString(R.string.online_gcash)
        binding.btnGlifeOpen.setOnClickListener {
            if (visibleNoReminder) {
                markGLife(binding.cbNoReminder.isChecked)
            }
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