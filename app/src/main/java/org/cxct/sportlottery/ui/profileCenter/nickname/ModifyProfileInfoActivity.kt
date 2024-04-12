package org.cxct.sportlottery.ui.profileCenter.nickname

import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.filterSpecialCharacters
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityModifyProfileInfoBinding
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.checkRegisterListener

/**
 * @app_destination 修改暱稱
 */
class ModifyProfileInfoActivity : BaseActivity<ModifyProfileInfoViewModel,ActivityModifyProfileInfoBinding>() {

    private val modifyType by lazy { intent.getIntExtra(MODIFY_INFO, ModifyType.NickName) }

    companion object {
        const val MODIFY_INFO = "MODIFY_INFO"
    }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initSetting()
        setupInputFieldVerify()
        initButton()
        initObserve()
    }

    private fun initSetting() {
        initView()
    }

    private fun initView()=binding.run {
        //預設將所有輸入欄位隱藏
        val allEditText = llRoot.children
        allEditText.forEach { if (it is LinearLayout) it.visibility = View.GONE }
        //根據傳入的ModifyType當前編輯的欄位做顯示
        when (modifyType) {
            ModifyType.RealName -> {
                toolBar.titleText = getString(R.string.real_name)
                llRealName.visibility = View.VISIBLE
            }

            ModifyType.PlaceOfBirth -> {
                toolBar.titleText = resources.getString(R.string.P104)
                llPlaceOfBirth.visibility = View.VISIBLE

                eetPlaceOfBirth.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btnConfirm.isEnabled = isActivated
                    btnConfirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetPlaceOfBirth.setText("")
            }

            ModifyType.Address -> {
                toolBar.titleText = resources.getString(R.string.M259)
                llAddressParent.visibility = View.VISIBLE
                eetllAddress.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btnConfirm.isEnabled = isActivated
                    btnConfirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllAddress.setText("")
                tvAddressRedTips.text =
                    resources.getString(R.string.J459) +"\t"+ resources.getString(R.string.P111) +"\t"+ resources.getString(
                        R.string.P110
                    )
            }

            ModifyType.AddressP -> {
                toolBar.titleText = resources.getString(R.string.M259)
                llAddressParent.visibility = View.VISIBLE
                eetllAddress.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btnConfirm.isEnabled = isActivated
                    btnConfirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllAddress.setText("")
                tvAddressRedTips.text =
                    resources.getString(R.string.J459) +"\t"+ resources.getString(R.string.P111) +"\t"+ resources.getString(
                        R.string.P110
                    )
            }

            ModifyType.ZipCode -> {
                toolBar.titleText = resources.getString(R.string.N827)
                llZipCode.visibility = View.VISIBLE
                eetllZipCode.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btnConfirm.isEnabled = isActivated
                    btnConfirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllZipCode.setText("")
            }

            ModifyType.ZipCodeP -> {
                toolBar.titleText = resources.getString(R.string.N827)
                llZipCode.visibility = View.VISIBLE
                eetllZipCode.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btnConfirm.isEnabled = isActivated
                    btnConfirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllZipCode.setText("")
            }

            ModifyType.NickName -> {                //暱稱
                setupNickName()
            }

            ModifyType.QQNumber -> {
                toolBar.titleText = getString(R.string.qq_number)
                llQqNumber.visibility = View.VISIBLE
            }

            ModifyType.Email -> {
                llEMail.visibility = View.VISIBLE
            }

            ModifyType.PhoneNumber -> {
                toolBar.titleText = getString(R.string.phone_number)
                llPhoneNumber.visibility = View.VISIBLE
            }

            ModifyType.WeChat -> {
                toolBar.titleText = getString(R.string.we_chat)
                llWechat.visibility = View.VISIBLE
            }
        }
    }

    private fun setupInputFieldVerify()=binding.run {
        //真實姓名
        setEditTextFocusChangeMethod(eetRealName)
        //QQ號碼
        setEditTextFocusChangeMethod(eetQqNumber)
        //郵箱
        setEditTextFocusChangeMethod(eetEMail)
        //手機號碼
        setEditTextFocusChangeMethod(eetPhoneNumber)
        //微信
        setEditTextFocusChangeMethod(eetWeChat)
    }

    private fun setupNickName()=binding.run {
        toolBar.titleText = getString(R.string.change_nickname)
        llNickname.visibility = View.VISIBLE
        eetNickname.filterSpecialCharacters()
        setEditTextFocusChangeMethod(eetNickname)
        eetNickname.maxLines = 1
        eetNickname.checkRegisterListener {
            val msg = when {
                it.isBlank() -> getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyNickname(it) -> {
                    getString(R.string.nickname_match_error)
                }

                else -> null
            }
            val isActivated = TextUtils.isEmpty(msg)
            etNickname.setError(msg, isActivated)
            btnConfirm.isEnabled = isActivated
            btnConfirm.alpha = if (isActivated) 1f else 0.5f
        }
        eetNickname.setText("") // 设置空触发输入检查
    }

    private fun setEditTextFocusChangeMethod(editText: ExtendedEditText) {
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                viewModel.checkInput(modifyType, editText.text.toString())
        }
    }

    private fun initButton()=binding.run {
        toolBar.setOnBackPressListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            checkInputData()
        }
//        et_real_name.afterTextChanged {
//            viewModel.checkFullName(applicationContext, it)
//        }
        btnConfirm.setTitleLetterSpacing()
    }

    private fun checkInputData()=binding.run {
        val inputText = when (modifyType) {
            ModifyType.RealName -> eetRealName.text
            ModifyType.QQNumber -> eetQqNumber.text
            ModifyType.Email -> eetEMail.text
            ModifyType.WeChat -> eetWeChat.text
            ModifyType.PhoneNumber -> eetPhoneNumber.text
            ModifyType.NickName -> eetNickname.text
            ModifyType.PlaceOfBirth -> eetPlaceOfBirth.text
            ModifyType.Address -> eetllAddress.text
            ModifyType.AddressP -> eetllAddress.text
            ModifyType.ZipCode -> eetllZipCode.text
            ModifyType.ZipCodeP -> eetllZipCode.text
            else -> {}
        }
        viewModel.confirmProfileInfo(modifyType, inputText.toString())
    }

    private fun initObserve() {
        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }

        setupEditTextErrorMsgObserve()

        viewModel.nicknameResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.withdrawInfoResult.observe(this) {
            updateUiWithResult(it)
        }
    }

    private fun setupEditTextErrorMsgObserve() {
        viewModel.apply {
            nickNameErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etNickname.setError(it, false)
            })

            fullNameErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etRealName.setError(it, false)
            })

            qqErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etQqNumber.setError(it, false)
            })

            eMailErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etEMail.setError(it, false)
            })

            phoneErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etPhoneNumber.setError(it, false)
            })

            weChatErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                binding.etWeChat.setError(it, false)
            })
        }
    }

    private fun updateUiWithResult(result: BaseResult?) {
        if (result?.success == true) {
            finish()
        } else {
            val errorMsg = result?.msg ?: getString(R.string.unknown_error)
            showErrorDialog(errorMsg)
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog()
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, null)
    }
}