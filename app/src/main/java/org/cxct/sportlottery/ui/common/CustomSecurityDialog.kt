package org.cxct.sportlottery.ui.common

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_security_code_submit.*
import org.cxct.sportlottery.R
import java.util.*

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class CustomSecurityDialog(context: Context) : DialogFragment() {

    private var mSmsTimer: Timer? = null
    private var mGetSecurityCodeClickListener: View.OnClickListener = View.OnClickListener { showSmeTimer300() }
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    var positiveClickListener: PositiveClickListener? = null

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
         dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)//隱藏rootLayout
         //處理 一開始先隱藏鍵盤
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.setCanceledOnTouchOutside(false)// 點擊外面不會消失
    }

    private fun initView() {
        btn_get_security.setOnClickListener(mGetSecurityCodeClickListener)
        btn_positive.setOnClickListener{
            positiveClickListener?.onClick(edt_security_code.text.toString().trim())
        }
        btn_negative.setOnClickListener(mNegativeClickListener)
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

    class PositiveClickListener(private val clickListener: (string:String) -> Unit) {
        fun onClick(string:String) = clickListener(string)
    }

    //發送簡訊後，倒數五分鐘
    fun showSmeTimer300() {
        try {
            stopSmeTimer()

            var sec = 60
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            btn_get_security.isEnabled = false
                            btn_get_security.text = "${sec}s"
//                            btn_get_security.setTextColor(
//                                ContextCompat.getColor(
//                                    context,
//                                    R.color.colorGrayDark
//                                )
//                            )
                        } else {
                            stopSmeTimer()
                            btn_get_security.isEnabled = true
                            btn_get_security.text = getString(R.string.get_verification_code)
                            btn_get_security.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            btn_get_security.isEnabled = true
            btn_get_security.text = getString(R.string.get_verification_code)
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
    }
}