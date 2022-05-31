package org.cxct.sportlottery.ui.login.signIn

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityPhoneVerifyBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing
import java.util.*


class PhoneVerifyActivity : BaseActivity<LoginViewModel>(LoginViewModel::class),
    View.OnClickListener {

    private lateinit var binding: ActivityPhoneVerifyBinding
    private var mSmsTimer: Timer? = null

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubmit -> {
                if (!checkInputData()) {
                    val deviceId = Settings.Secure.getString(
                        this.contentResolver, Settings.Secure.ANDROID_ID
                    )
                    viewModel.validateLoginDeviceSms(
                        binding.eetVerificationCode.text.toString(),
                        deviceId
                    )
                }
            }
            binding.btnBack -> {
                this@PhoneVerifyActivity.onBackPressed()
            }
            binding.btnSendSms -> {
                viewModel.sendLoginDeviceSms()
            }

            binding.constraintLayout -> {
                hideSoftKeyboard(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserve()
    }

    fun initView() {
        binding.btnSendSms.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.btnSubmit.setTitleLetterSpacing()
        binding.constraintLayout.setOnClickListener(this)
        binding.eetVerificationCode.addTextChangedListener {
            binding.btnSubmit.isEnabled = it?.length ?: 0 > 0
        }
        binding.constraintLayout.setOnClickListener {
            binding.eetVerificationCode.clearFocus()
            binding.etVerificationCode.clearFocus()

            hideSoftKeyboard(this@PhoneVerifyActivity)
        }
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
                    GamePublicityActivity.reStart(this)
            } else {
                binding.etVerificationCode.setError(
                    getString(R.string.login_phone_verify_error),
                    false
                )
            }
        })


    }

    override fun onBackPressed() {
        viewModel.doLogoutCleanUser {
            if (sConfigData?.thirdOpen == FLAG_OPEN)
                MainActivity.reStart(this)
            else
                GamePublicityActivity.reStart(this)
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
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@PhoneVerifyActivity,
                                    R.color.color_BDBDBD_404040
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text =
                                getString(R.string.login_phone_verify_get_code)
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

    private fun checkInputData(): Boolean {
        return binding.eetVerificationCode.text.isBlank()
    }


}