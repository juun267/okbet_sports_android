package org.cxct.sportlottery.ui.login.signIn

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityPhoneVerifyBinding
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing
import java.util.*


class PhoneVerifyActivity : BaseActivity<LoginViewModel,ActivityPhoneVerifyBinding>(LoginViewModel::class),
    View.OnClickListener {

    private var mSmsTimer: Timer? = null

    companion object {
        var loginData: LoginData? = null
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubmit -> {
                if (!checkInputData()) {
                    val deviceId = Settings.Secure.getString(
                        this.contentResolver, Settings.Secure.ANDROID_ID
                    )
                    viewModel.validateLoginDeviceSms(
                        loginData?.token ?: "",
                        binding.eetVerificationCode.text.toString(),
                        deviceId
                    )
                }
            }
            binding.btnBack -> {
                this@PhoneVerifyActivity.onBackPressed()
            }
            binding.btnSendSms -> {
                viewModel.sendLoginDeviceSms(loginData?.token ?: "")
            }

            binding.constraintLayout -> {
                hideSoftKeyboard()
            }
        }
    }

    override fun onInitView() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(binding.vTop.root)
            .fitsSystemWindows(false)
            .init()
        initView()
        initObserve()
    }

    fun initView() {
        binding.btnSendSms.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.btnSubmit.setTitleLetterSpacing()
        binding.constraintLayout.setOnClickListener(this)
        binding.constraintLayout.setOnClickListener {
            binding.eetVerificationCode.clearFocus()
            binding.etVerificationCode.clearFocus()

            hideSoftKeyboard()
        }
        binding.eetVerificationCode.apply {
            checkRegisterListener {
                var errMsg = viewModel.checkValidCode(it)
                binding.btnSubmit.isEnabled = errMsg.isNullOrEmpty()
                binding.etVerificationCode.setError(errMsg, true)
            }
        }
    }

    private fun initObserve() {
        viewModel.loginSmsResult.observe(this, Observer {
            if (it.success) {
                showSmeTimer300()
            } else {
                it.msg?.let { msg -> showErrorPromptDialog(msg) {} }
            }
        })
        viewModel.validResult.observe(this, Observer {
            if (it.success) {
//                if (sConfigData?.thirdOpen == FLAG_OPEN)
//                    MainActivity.reStart(this)
//                else
                GlobalScope.launch {
                    LoginRepository.setUpLoginData(loginData)
                }.let {
                    MainTabActivity.reStart(this)
                }

            } else {
                binding.etVerificationCode.setError(
                    getString(R.string.dialog_security_error),
                    false
                )
            }
        })


    }

    override fun onBackPressed() {
        viewModel.doLogoutCleanUser {
//            if (sConfigData?.thirdOpen == FLAG_OPEN)
//                MainActivity.reStart(this)
//            else
            MainTabActivity.reStart(this)
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
                                    R.color.color_AEAEAE_404040
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text =
                                getString(R.string.get_security_code)
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
        return !viewModel.checkValidCode(binding.eetVerificationCode.text.toString())
            .isNullOrEmpty()
    }


}