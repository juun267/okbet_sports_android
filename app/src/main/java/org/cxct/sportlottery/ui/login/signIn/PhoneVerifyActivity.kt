package org.cxct.sportlottery.ui.login.signIn

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityPhoneVerifyBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.widget.boundsEditText.TextFieldBoxes
import java.util.*


class PhoneVerifyActivity : BaseActivity<LoginViewModel>(LoginViewModel::class),View.OnClickListener {

    private lateinit var binding: ActivityPhoneVerifyBinding
    private var mSmsTimer: Timer? = null

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubmit -> {
                if(!checkInputData()){
                    val deviceId = Settings.Secure.getString(
                        applicationContext.contentResolver, Settings.Secure.ANDROID_ID
                    )
                    viewModel.validateLoginDeviceSms(binding.eetVerificationCode.text.toString(),deviceId)
                }
            }
            binding.btnBack -> {
                this@PhoneVerifyActivity.onBackPressed()
            }
            binding.btnSendSms -> {
                viewModel.sendLoginDeviceSms()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initView()
        initObserve()
    }

    private fun initData(){
        viewModel.sendLoginDeviceSms()
    }

    fun initView(){
        binding.btnSendSms.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
//        GlobalScope.launch(Dispatchers.Main) {
//            withContext(Dispatchers.IO) {
//                var phone = viewModel.getUserPhone()
//                binding.tvPhoneNumber.text = viewModel.getUserPhone()
//                Log.e("Martin","213="+phone)
//
//            }
//        }
    }

    private fun initObserve() {
        viewModel.loginSmsResult.observe(this, Observer {
            showSmeTimer300()
        })
        viewModel.validResult.observe(this, Observer {
            if (it.success) {
                if (sConfigData?.thirdOpen == FLAG_OPEN)
                    MainActivity.reStart(this)
                else
                    GameActivity.reStart(this)
            }else{
                binding.etVerificationCode.setError(getString(R.string.login_phone_verify_error),false)
            }
        })


    }

    override fun onBackPressed() {
        viewModel.doLogoutCleanUser {
            if (sConfigData?.thirdOpen == FLAG_OPEN)
                MainActivity.reStart(this)
            else
                GameActivity.reStart(this)
        }
    }

    //發送簡訊後，倒數五分鐘
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()

            var sec = 60
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            binding.btnSendSms.isEnabled = false
                            //btn_send_sms.text = getString(R.string.send_timer, sec)
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@PhoneVerifyActivity,
                                    R.color.colorGrayDark
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text = getString(R.string.login_phone_verify_get_code)
                            binding.btnSendSms.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.btnSendSms.isEnabled = true
            binding.btnSendSms.text = getString(R.string.get_verification_code)
        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getAction() === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideInput(v, ev)) {
                binding.eetVerificationCode.clearFocus()
                binding.etVerificationCode.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is TextFieldBoxes) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.height
            val right = (left
                    + v.width)
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        return true
    }

    private fun checkInputData(): Boolean {
        return  binding.eetVerificationCode.text.isBlank()
    }


}