package org.cxct.sportlottery.ui2.login.signUp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_register_success.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class RegisterSuccessDialog: DialogFragment() {



    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.dialog_register_success, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    val timer= object: CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            var diff = millisUntilFinished
            val secondsInMilli: Long = 1000
            val elapsedSeconds = diff / secondsInMilli

            tvCount?.let {
                it.text = "($elapsedSeconds)"
            }
        }

        override fun onFinish() {
            dismiss()
            goHomePage()
        }
    }
    private fun initView() {

        timer.start()
        tvVisitFirst.setOnClickListener {
            dismiss()
            goHomePage()
        }
        tvJump.text = getString(R.string.register_jump,getString(R.string.app_name))
        btnRecharge.setOnClickListener (mNegativeClickListener)
        btnRecharge.setTitleLetterSpacing()
    }
    fun setNegativeClickListener(negativeClickListener: View.OnClickListener) {
        mNegativeClickListener = negativeClickListener
    }

    /**
     * 根據第三方開關判斷當前首頁是哪一個
     */
    private fun goHomePage() {
//        if (sConfigData?.thirdOpen == FLAG_OPEN) {
//            MainActivity.reStart(mContext)
//        } else {
        val cxt = context ?: MultiLanguagesApplication.appContext
        MainTabActivity.reStart(cxt)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}