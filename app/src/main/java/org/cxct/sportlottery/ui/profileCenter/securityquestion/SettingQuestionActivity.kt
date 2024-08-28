package org.cxct.sportlottery.ui.profileCenter.securityquestion

import android.app.Activity
import android.content.Intent
import android.widget.Button
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.view_global_loading.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivitySecurityQuestionBinding
import org.cxct.sportlottery.databinding.ActivitySettingPasswordBinding
import org.cxct.sportlottery.databinding.DialogBottomSelectBinding
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.foget.ForgetWaysActivity
import org.cxct.sportlottery.ui.profileCenter.modify.ModifyBindInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.ui.profileCenter.profile.DialogBottomDataAdapter
import org.cxct.sportlottery.ui.profileCenter.profile.DialogBottomDataEntity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.checkRegisterListener

/**
 * @app_destination 密码设置
 */
class SettingQuestionActivity : BaseActivity<SettingQuestionViewModel, ActivitySecurityQuestionBinding>() {

    private var safeQuestionType = 1

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
        etQuestion.setOnClickListener {
           val questionList = viewModel.safeQuestionEvent.value
            if(questionList.isNullOrEmpty()) return@setOnClickListener
            val items = questionList.map { DialogBottomDataEntity(it.name,false, it.id) }
             showBottomDialog(items, getString(R.string.B180), null){
                 safeQuestionType = it.id
                 eetQuestion.setText(it.name)
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
            binding.etAnswer.setError(if(it.isNullOrEmpty()) getString(R.string.error_input_empty) else null, false)
            updateButtonStatus()
        }
    }
    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            loading()
            viewModel.setSafeQuestion(safeQuestionType,binding.eetAnswer.text.toString(),binding.eetCurrentPassword.text.toString())
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
                showPromptDialog(getString(R.string.prompt),getString(R.string.J533)){
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
                !eetAnswer.text.isNullOrEmpty())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

    private fun showBottomDialog(
        list: List<DialogBottomDataEntity>,
        title: String,
        currStr: String?,
        callBack: (item: DialogBottomDataEntity) -> Unit
    ) {
        val binding = DialogBottomSelectBinding.inflate(layoutInflater)
        val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }
        val adapter = DialogBottomDataAdapter()
        binding.rvBtmData.adapter = adapter
        binding.btnBtmCancel.setOnClickListener { bottomSheet.dismiss() }

        bottomSheet.setContentView(binding.root)

        var item: DialogBottomDataEntity? = list.find { it.flag }
        val listNew: MutableList<DialogBottomDataEntity> = mutableListOf()
        var trueFlag = false
        list.forEach {
            val ne = it.copy()
            if (ne.name == currStr) {
                ne.flag = true
                trueFlag = true
            }
            listNew.add(ne)
        }
        if (!trueFlag && listNew.isNotEmpty()) {
            listNew.last().flag = true
        }
        binding.tvBtmTitle.text = title
        adapter.data = listNew
        adapter.notifyDataSetChanged()
        binding.rvBtmData.scrollToPosition(0)
        adapter.setOnItemClickListener { ater, view, position ->
            adapter.data.forEach {
                it.flag = false
            }
            item = adapter.data[position]
            item!!.flag = true
            adapter.notifyDataSetChanged()
        }
        binding.btnBtmDone.setOnClickListener {
            item?.let { it1 -> callBack(it1) }
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }
}