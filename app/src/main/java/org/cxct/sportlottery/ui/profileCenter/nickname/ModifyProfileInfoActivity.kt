package org.cxct.sportlottery.ui.profileCenter.nickname

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_modify_profile_info.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.LoginEditText

class ModifyProfileInfoActivity : BaseOddButtonActivity<ModifyProfileInfoViewModel>(ModifyProfileInfoViewModel::class) {
    private val modifyType by lazy { intent.getSerializableExtra(MODIFY_INFO) }

    companion object {
        const val MODIFY_INFO = "MODIFY_INFO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile_info)

        initSetting()
        setupInputFieldVerify()
        initButton()
        initObserve()
    }

    private fun initSetting() {
        initView()
    }

    private fun initView() {
        //預設將所有輸入欄位隱藏
        val allEditText = ll_root.children
        allEditText.forEach { if (it is LinearLayout) it.visibility = View.GONE }

        //根據傳入的ModifyType當前編輯的欄位做顯示
        when (modifyType) {
            ModifyType.RealName -> {
                tv_title.text = getString(R.string.real_name)
                ll_real_name.visibility = View.VISIBLE
            }
            ModifyType.NickName -> {
                tv_title.text = getString(R.string.change_nickname)
                ll_nickname.visibility = View.VISIBLE
            }
            ModifyType.QQNumber -> {
                tv_title.text = getString(R.string.qq_number)
                ll_qq_number.visibility = View.VISIBLE
            }
            ModifyType.Email -> {
                tv_title.text = getString(R.string.e_mail)
                ll_e_mail.visibility = View.VISIBLE
            }
            ModifyType.PhoneNumber -> {
                tv_title.text = getString(R.string.phone_number)
                ll_phone_number.visibility = View.VISIBLE
            }
            ModifyType.WeChat -> {
                tv_title.text = getString(R.string.we_chat)
                ll_wechat.visibility = View.VISIBLE
            }
        }
    }

    private fun setupInputFieldVerify() {
        //暱稱
        setEditTextFocusChangeMethod(et_nickname)
        //真實姓名
        setEditTextFocusChangeMethod(et_real_name)
        //QQ號碼
        setEditTextFocusChangeMethod(et_qq_number)
        //郵箱
        setEditTextFocusChangeMethod(et_e_mail)
        //手機號碼
        setEditTextFocusChangeMethod(et_phone_number)
        //微信
        setEditTextFocusChangeMethod(et_we_chat)
    }

    private fun setEditTextFocusChangeMethod(editText: LoginEditText) {
        editText.apply {
            setEditTextOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus)
                    viewModel.checkInput(modifyType as ModifyType, view.et_input.text.toString())
            }
            setupEditTextClearListener() { viewModel.checkInput(modifyType as ModifyType, editText.et_input.text.toString()) }
        }
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            checkInputData()
        }
    }

    private fun checkInputData() {
        val inputText = when (modifyType as ModifyType) {
            ModifyType.RealName -> et_real_name.getText()
            ModifyType.QQNumber -> et_qq_number.getText()
            ModifyType.Email -> et_e_mail.getText()
            ModifyType.WeChat -> et_we_chat.getText()
            ModifyType.PhoneNumber -> et_phone_number.getText()
            ModifyType.NickName -> et_nickname.getText()
        }
        viewModel.confirmProfileInfo(modifyType as ModifyType, inputText)
    }

    private fun initObserve() {
        viewModel.loading.observe(this, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })

        setupEditTextErrorMsgObserve()

        viewModel.nicknameResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.withdrawInfoResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

    private fun setupEditTextErrorMsgObserve() {
        viewModel.apply {
            nickNameErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_nickname.setError(it)
            })

            fullNameErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_real_name.setError(it)
            })

            qqErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_qq_number.setError(it)
            })

            eMailErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_e_mail.setError(it)
            })

            phoneErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_phone_number.setError(it)
            })

            weChatErrorMsg.observe(this@ModifyProfileInfoActivity, Observer {
                et_we_chat.setError(it)
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
        dialog.setCancelable(false)
        dialog.show()
    }
}