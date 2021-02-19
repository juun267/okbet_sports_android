package org.cxct.sportlottery.ui.profileCenter.nickname

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_modify_profile_info.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.LoginEditText

class ModifyProfileInfoActivity : BaseOddButtonActivity<NicknameModel>(NicknameModel::class) {
    private val modifyType by lazy { intent.getSerializableExtra(MODIFY_INFO) }

    companion object {
        const val MODIFY_INFO = "MODIFY_INFO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile_info)

        initSetting()
        setupNickname()
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
            ModifyType.NickName -> {
                ll_nickname.visibility = View.VISIBLE
            }
            ModifyType.QQNumber -> {
                ll_qq_number.visibility = View.VISIBLE
            }
            ModifyType.Email -> {
                ll_e_mail.visibility = View.VISIBLE
            }
            ModifyType.PhoneNumber -> {
                ll_phone_number.visibility = View.VISIBLE
            }
            ModifyType.WeChat -> {
                ll_wechat.visibility = View.VISIBLE
            }
        }
    }

    private fun setupNickname() {
        et_nickname.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            if (checkInputData()) {
                editNickName()
            }
        }
    }

    private fun checkInputData(): Boolean {
        return viewModel.nicknameDataChanged(this, et_nickname.getText())
    }

    private fun editNickName() {
        loading()
        viewModel.editNickName(et_nickname.getText())
    }

    private fun initObserve() {
        viewModel.nicknameFormState.observe(this, Observer {
            et_nickname.setError(it.nicknameError)
        })

        viewModel.nicknameResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(nicknameResult: NicknameResult?) {
        hideLoading()
        if (nicknameResult?.success == true) {
            finish()
        } else {
            val errorMsg = nicknameResult?.msg ?: getString(R.string.unknown_error)
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