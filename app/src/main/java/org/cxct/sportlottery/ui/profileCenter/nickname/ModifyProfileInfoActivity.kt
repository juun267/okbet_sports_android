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
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.checkRegisterListener

/**
 * @app_destination 修改暱稱
 */
class ModifyProfileInfoActivity : BindingActivity<ModifyProfileInfoViewModel,ActivityModifyProfileInfoBinding>() {

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
                    btn_confirm.isEnabled = isActivated
                    btn_confirm.alpha = if (isActivated) 1f else 0.5f
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
                    btn_confirm.isEnabled = isActivated
                    btn_confirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllZipCode.setText("")
            }

            ModifyType.ZipCodeP -> {
                toolBar.titleText = resources.getString(R.string.N827)
                llZipCode.visibility = View.VISIBLE
                eetllZipCode.checkRegisterListener {
                    val isActivated = !it.isNullOrEmpty()
                    btn_confirm.isEnabled = isActivated
                    btn_confirm.alpha = if (isActivated) 1f else 0.5f
                }
                eetllZipCode.setText("")
            }

            ModifyType.NickName -> {                //暱稱
                setupNickName()
            }

            ModifyType.QQNumber -> {
                toolBar.titleText = getString(R.string.qq_number)
                ll_qq_number.visibility = View.VISIBLE
            }

            ModifyType.Email -> {
                ll_e_mail.visibility = View.VISIBLE
            }

            ModifyType.PhoneNumber -> {
                toolBar.titleText = getString(R.string.phone_number)
                ll_phone_number.visibility = View.VISIBLE
            }

            ModifyType.WeChat -> {
                toolBar.titleText = getString(R.string.we_chat)
                ll_wechat.visibility = View.VISIBLE
            }
        }
    }

    private fun setupInputFieldVerify() {
        //真實姓名
        setEditTextFocusChangeMethod(eet_real_name)
        //QQ號碼
        setEditTextFocusChangeMethod(eet_qq_number)
        //郵箱
        setEditTextFocusChangeMethod(eet_e_mail)
        //手機號碼
        setEditTextFocusChangeMethod(eet_phone_number)
        //微信
        setEditTextFocusChangeMethod(eet_we_chat)
    }

    private fun setupNickName() {
        toolBar.titleText = getString(R.string.change_nickname)
        ll_nickname.visibility = View.VISIBLE
        eet_nickname.filterSpecialCharacters()
        setEditTextFocusChangeMethod(eet_nickname)
        eet_nickname.maxLines = 1
        val nickNameMinLength = 2
        val nickNameMaxLength = 6
        eet_nickname.checkRegisterListener {
            val msg = when {
                it.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyLengthRange(it, nickNameMinLength, nickNameMaxLength) -> {
                    LocalUtils.getLocalizedContext().getString(
                        R.string.error_member_nickname,
                        nickNameMinLength,
                        nickNameMaxLength
                    )
                }

                else -> null
            }
            val isActivated = TextUtils.isEmpty(msg)
            et_nickname.setError(msg, isActivated)
            btn_confirm.isEnabled = isActivated
            btn_confirm.alpha = if (isActivated) 1f else 0.5f
        }
        eet_nickname.setText("") // 设置空触发输入检查
    }

    private fun setEditTextFocusChangeMethod(editText: ExtendedEditText) {
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                viewModel.checkInput(modifyType, editText.text.toString())
        }
    }

    private fun initButton() {
        toolBar.setOnBackPressListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            checkInputData()
        }
//        et_real_name.afterTextChanged {
//            viewModel.checkFullName(applicationContext, it)
//        }
        btn_confirm.setTitleLetterSpacing()
    }

    private fun checkInputData() {
        val inputText = when (modifyType) {
            ModifyType.RealName -> eet_real_name.text
            ModifyType.QQNumber -> eet_qq_number.text
            ModifyType.Email -> eet_e_mail.text
            ModifyType.WeChat -> eet_we_chat.text
            ModifyType.PhoneNumber -> eet_phone_number.text
            ModifyType.NickName -> eet_nickname.text
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
                et_nickname.setError(it, false)
            })

            fullNameErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_real_name.setError(it, false)
            })

            qqErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_qq_number.setError(it, false)
            })

            eMailErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_e_mail.setError(it, false)
            })

            phoneErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_phone_number.setError(it, false)
            })

            weChatErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_we_chat.setError(it, false)
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
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, null)
    }
}