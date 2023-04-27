package org.cxct.sportlottery.ui.common.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.content_security_code_style_edittext.view.*
import kotlinx.android.synthetic.main.dialog_security_code_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import java.util.*

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class CustomSecurityDialog : DialogFragment() {

    private var mSmsTimer: Timer? = null
    private var mGetSecurityCodeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener {
        dismiss()
        sConfigData?.hasGetTwoFactorResult = false
    }
    var positiveClickListener: PositiveClickListener? = null
    var mContext = context
    var isPstBtnClickable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setCustomDialogStyle()
        return inflater.inflate(R.layout.dialog_security_code_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    protected fun setCustomDialogStyle() {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.setCanceledOnTouchOutside(false)
    }

    private fun initView() {
        securityCodeStyleEditText.btn_get_security.setOnClickListener(mGetSecurityCodeClickListener)

        btn_positive.setOnClickListener {
            if (isPstBtnClickable)
                positiveClickListener?.onClick(securityCodeStyleEditText.edt_security_code.text.toString().trim())
        }
        btn_negative.setOnClickListener(mNegativeClickListener)

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
        val hasInput = securityCodeStyleEditText.edt_security_code.text.toString().isNotEmpty()
        isPstBtnClickable = isClickable && hasInput
        if(isPstBtnClickable)
            btn_positive.setTextColor(ContextCompat.getColor(btn_positive.context, R.color.color_317FFF_0760D4))
        else
            btn_positive.setTextColor(ContextCompat.getColor(btn_positive.context, R.color.color_cccccc_e2e2e2))
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
            securityCodeStyleEditText.let {
                var sec = 60
                mSmsTimer = Timer()
                mSmsTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if (sec-- > 0) {
                                it.btn_get_security.isEnabled = false
                                it.btn_get_security.text = "${sec}s"
//                            btn_get_security.setTextColor(
//                                ContextCompat.getColor(
//                                    context,
//                                    R.color.colorGrayDark
//                                )
//                            )
                            } else {
                                stopSmeTimer()
                                it.btn_get_security.isEnabled = true
                                it.btn_get_security.text = getString(R.string.get_verification_code)
                                it.btn_get_security.setTextColor(Color.WHITE)
                            }
                        }
                    }
                }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
            }
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            securityCodeStyleEditText.btn_get_security.isEnabled = true
            securityCodeStyleEditText.btn_get_security.text = getString(R.string.get_verification_code)
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