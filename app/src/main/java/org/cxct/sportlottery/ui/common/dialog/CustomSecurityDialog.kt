package org.cxct.sportlottery.ui.common.dialog

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogSecurityCodeSubmitBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import java.util.*

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class CustomSecurityDialog : BaseDialog<BaseViewModel,DialogSecurityCodeSubmitBinding>() {

    private var mSmsTimer: Timer? = null
    private var mGetSecurityCodeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener {
        dismiss()
        sConfigData?.hasGetTwoFactorResult = false
    }
    var positiveClickListener: PositiveClickListener? = null
    var isPstBtnClickable = false

    init {
        setStyle(R.style.CustomDialogStyle)
    }
    override fun onInitView() {
        isCancelable = false
        initView()
    }

    private fun initView()=binding.run {
        securityCodeStyleEditText.binding.btnGetSecurity.setOnClickListener(mGetSecurityCodeClickListener)

        btnPositive.setOnClickListener {
            if (isPstBtnClickable)
                positiveClickListener?.onClick(securityCodeStyleEditText.binding.edtSecurityCode.text.toString().trim())
        }
        btnNegative.setOnClickListener(mNegativeClickListener)

        securityCodeStyleEditText.mClearEdittextListener = View.OnClickListener { setPositiveBtnClickable(sConfigData?.hasGetTwoFactorResult == true) }
    }

    //发送双重验证讯息
    fun getSecurityCodeClickListener(getSecurityCodeClickListener: View.OnClickListener) {
        mGetSecurityCodeClickListener = getSecurityCodeClickListener
    }

    fun setPositiveClickListener(positiveClickListener: View.OnClickListener) {
        mPositiveClickListener = positiveClickListener
    }

    fun setNegativeClickListener(negativeClickListener: View.OnClickListener) {
        mNegativeClickListener = negativeClickListener
    }

    //還要判斷輸入恇有沒有東西
    fun setPositiveBtnClickable(isClickable:Boolean){
        val hasInput = binding.securityCodeStyleEditText.binding.edtSecurityCode.text.toString().isNotEmpty()
        isPstBtnClickable = isClickable && hasInput
        if(isPstBtnClickable)
            binding.btnPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
        else
            binding.btnPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_cccccc_e2e2e2))
    }

    fun showErrorStatus(b:Boolean){
//        securityCodeStyleEditText.showErrorStatus(b)
    }

    class PositiveClickListener(private val clickListener: (string:String) -> Unit) {
        fun onClick(string:String) = clickListener(string)
    }

    //發送簡訊後，倒數五分鐘
    fun showSmeTimer300() {
        try {
            stopSmeTimer()
            binding.securityCodeStyleEditText.binding.btnGetSecurity.apply {
                var sec = 60
                mSmsTimer = Timer()
                mSmsTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if (sec-- > 0) {
                                isEnabled = false
                                text = "${sec}s"
//                            btn_get_security.setTextColor(
//                                ContextCompat.getColor(
//                                    context,
//                                    R.color.colorGrayDark
//                                )
//                            )
                            } else {
                                stopSmeTimer()
                                isEnabled = true
                                text = getString(R.string.get_verification_code)
                                setTextColor(Color.WHITE)
                            }
                        }
                    }
                }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
            }
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.securityCodeStyleEditText.binding.btnGetSecurity.apply {
                isEnabled = true
                text = getString(R.string.get_verification_code)
            }
        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }

    override fun onPause() {
        super.onPause()
        stopSmeTimer()
        sConfigData?.hasGetTwoFactorResult = false
    }

}