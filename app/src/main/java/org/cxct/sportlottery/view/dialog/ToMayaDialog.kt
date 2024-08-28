package org.cxct.sportlottery.view.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import com.xuexiang.xupdate.utils.UpdateUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogToMayaBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.LogUtil
import splitties.bundle.put

/**
 * glife 用户点击存取款跳转maya
 */
class ToMayaDialog : BaseDialog<BaseViewModel, DialogToMayaBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{

        //glife用户存取款提示弹窗点击了  不再提示标记
        private const val MAYA_TIP_FLAG = "maya_tip_flag"

        private fun markGLife(isChecked: Boolean) {
            KvUtils.put(MAYA_TIP_FLAG, isChecked)
        }

        private fun isChecked(): Boolean {
            return KvUtils.decodeBooleanTure(MAYA_TIP_FLAG, false)
        }

        /**
         * 登录后才需要显示，加全局状态值区分
         */
        var needShow = false

        fun newInstance(visibleNoReminder: Boolean = true): ToMayaDialog{
            val args = Bundle()
            args.put("visibleNoReminder",visibleNoReminder)
            val fragment = ToMayaDialog()
            fragment.arguments = args
            return fragment
        }
        /**
         * 根据条件判断是否需要显示
         */
        fun showByLogin(){
            LogUtil.d("isMayaAccount 00")
            if (LoginRepository.isLogined() && UserInfoRepository.isMayaAccount()) {
                LogUtil.d("isChecked="+isChecked()+",needShow="+needShow)
                if (!isChecked() && needShow) {
                    needShow = false
                    newInstance().show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToMayaDialog.javaClass.name)
                }
            }
        }
    }

    private val visibleNoReminder by lazy { requireArguments().getBoolean("visibleNoReminder") }
    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: OnNegativeListener? = null


    override fun onInitView() {
        isCancelable = false
        setupMaya()
        binding.btnCancel.setOnClickListener {
            if (visibleNoReminder) {
                markGLife(binding.cbNoReminder.isChecked)
            }
            dismiss()
        }
        binding.cbNoReminder.isVisible = visibleNoReminder
        binding.btnOpen.text = requireContext().getString(R.string.LT028)+" "+requireContext().getString(R.string.online_maya)
        binding.btnOpen.setOnClickListener {
            if (visibleNoReminder) {
                markGLife(binding.cbNoReminder.isChecked)
            }
            val uri = Uri.parse("https://official.paymaya.com/be7m/PayMayaAppLinks")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            UpdateUtils.startActivity(intent)
            dismiss()
        }
    }
    fun setupMaya()=binding.run{
        ivIcon.setImageResource(R.drawable.ic_maya_logo)
        tvToTitle.text = "Maya member reminder"
        tvContent.text = "Hello, as you have registered on the PayMaya platform, according to the official requirements, deposits and withdrawals must be completed exclusively through the PayMaya program. Click “Open PayMaya” below to proceed. Thank you!"
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