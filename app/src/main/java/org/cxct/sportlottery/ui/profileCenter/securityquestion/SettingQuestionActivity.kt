package org.cxct.sportlottery.ui.profileCenter.securityquestion

import android.app.Activity
import android.content.Intent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivitySecurityQuestionBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.profile.DialogBottomDataEntity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.checkRegisterListener

/**
 * @app_destination 密码设置
 */
class SettingQuestionActivity : BaseActivity<SettingQuestionViewModel, ActivitySecurityQuestionBinding>() {

    override fun pageName() = "安全问题设置页面"

    private var safeQuestionType = 1
    private val bottomDialog by lazy { CommonBottomSheetDialog(this){
        safeQuestionType = it.id
        binding.eetQuestion.setText(it.name)
        binding.etQuestion.adjustPanelHeightWrapContet()
    } }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initView()
        setupEditText()
        setupConfirmButton()
        initObserve()
        viewModel.querySafeQuestionType()
    }

    private fun initView() = binding.run {
        customToolBar.setOnBackPressListener { finish() }
        eetCurrentPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        val bottomLineColorRes = R.color.color_80334266_E3E8EE

        etCurrentPassword.binding.bottomLine.setBackgroundResource(bottomLineColorRes)
        etQuestion.binding.bottomLine.setBackgroundResource(bottomLineColorRes)
        etAnswer.binding.bottomLine.setBackgroundResource(bottomLineColorRes)
        btnConfirm.setBtnEnable(false)
        etCurrentPassword.setTransformationMethodEvent(eetCurrentPassword)
        setOnClickListeners(etQuestion,eetQuestion,etQuestion.endIconImageButton) {
           val questionList = viewModel.safeQuestionEvent.value
            if(!questionList.isNullOrEmpty()){
                val items = questionList.map { DialogBottomDataEntity(it.name,false, it.id) }
                bottomDialog.setupData(items, getString(R.string.B180), binding.eetQuestion.text.toString())
                bottomDialog.show()
            }
        }
    }


    private fun setupEditText() {
        binding.eetCurrentPassword.checkRegisterListener {
            binding.etCurrentPassword.setError(if(it.isNullOrEmpty()) getString(R.string.error_input_empty) else null, false)
            updateButtonStatus()
        }
        binding.eetQuestion.checkRegisterListener {
            binding.etQuestion.setError(if(it.isNullOrEmpty()) getString(R.string.error_input_empty) else null, false)
            updateButtonStatus()
        }
        binding.eetAnswer.checkRegisterListener {
            binding.etAnswer.setError(if(it.trim().isNullOrEmpty()) getString(R.string.error_input_empty) else null, false)
            VerifyConstUtil.verifyLengthRange(it,1,18)
            updateButtonStatus()
        }
    }
    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            loading()
            viewModel.setSafeQuestion(safeQuestionType,binding.eetAnswer.text.trim().toString(),binding.eetCurrentPassword.text.toString())
        }
        binding.btnConfirm.setTitleLetterSpacing()
    }

    private fun initObserve() {
        viewModel.safeQuestionEvent.observe(this){
            LogUtil.toJson(it)
        }
        viewModel.setQuestionEvent.observe(this) {
            hideLoading()
            if (it.succeeded()){
                viewModel.getUserInfo()
                showPromptDialog(getString(R.string.J533)){
                    finish()
                }
            }else{
                showErrorPromptDialog(it.msg){}
            }
        }
    }

    private fun updateButtonStatus()=binding.run {
        btnConfirm.setBtnEnable(!eetCurrentPassword.text.isNullOrEmpty() &&
                !eetQuestion.text.isNullOrEmpty() &&
                !eetAnswer.text.trim().isNullOrEmpty())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

}