package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.bigkoo.pickerview.view.TimePickerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.KYCEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityKycFormBinding
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signUp.info.DateTimePickerOptions
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.KeyboadrdHideUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import java.util.*

class KYCFormActivity: BaseActivity<ProfileCenterViewModel, ActivityKycFormBinding>() {

    companion object {

        fun start(context: Context, idType: Int, idTypeName: String, imageUrl: String, ocrInfo: OCRInfo?) {
            val intent = Intent(context, KYCFormActivity::class.java)
            intent.putExtra("idType", idType)
            intent.putExtra("idTypeName", idTypeName)
            intent.putExtra("imageUrl", imageUrl)
            ocrInfo?.let { intent.putExtra("ocrInfo", it) }
            context.startActivity(intent)
        }
    }

    private val defaultBg = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_F9FAFD)
    private val foucedBg = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.white, R.color.color_025BE8)

    private val idType by lazy { intent.getIntExtra("idType", 0) }
    private val idTypeName by lazy { intent.getStringExtra("idTypeName")!! }
    private val imageUrl by lazy { intent.getStringExtra("imageUrl")!! }
    private val ocrInfo by lazy { intent.getParcelableExtra<OCRInfo>("ocrInfo") }

    override fun onInitView() {
        setStatusbar()
        initStyle()
        initEvent()
        initObserver()
        ocrInfo?.let { bindOCRInfo(it) }
     }

    private fun initObserver() {
        viewModel.kycResult.observe(this) {
            hideLoading()
            if (!it.succeeded()) {
                showErrorPromptDialog(it.msg) {  }
            } else {
                EventBusUtil.post(KYCEvent())
                UserInfoRepository.loadUserInfo()
                showPromptDialog(message = getString(R.string.submit_success)) { finishWithOK() }
            }
        }
    }

    private fun bindOCRInfo(ocr: OCRInfo) = binding.run {
//        eetFirstName.setText(ocr.firstName)
        edtFirstName.setText(ocr.firstName)
        edtLastName.setText(ocr.lastName)
        tvBirthday.setText(ocr.birthday?.replace("/", "-"))
        edtMiddleName.setText(ocr.middleName)
    }

    private fun initEvent() = binding.run {
        tvIdType.text = idTypeName
        frBirthday.setOnClickListener { showDateTimePicker() }
        tvBirthday.setOnClickListener { showDateTimePicker() }
        toolBar.btnToolbarBack.setOnClickListener { finish() }
        llMiddleName.setOnClickListener {
            val isSelected = !it.isSelected
            it.isSelected = isSelected
            frMiddleName.isEnabled = !isSelected
            edtMiddleName.isEnabled = frMiddleName.isEnabled
            ivCheckBox.isSelected = isSelected
            if (isSelected) {
                edtMiddleName.setText("N/A")
                edtMiddleName.clearFocus()
                tvHaveMiddelName.setTextColor(getColor(R.color.color_025BE8))
            } else {
                edtMiddleName.setText("")
                edtMiddleName.requestFocus()
                tvHaveMiddelName.setTextColor(getColor(R.color.color_667085))
            }
        }

//        val onTextChanged: (String) -> Unit = { checkInput() }
//        edtFirstName.afterTextChanged(onTextChanged)
//        edtMiddleName.afterTextChanged(onTextChanged)
//        edtLastName.afterTextChanged(onTextChanged)
        btnConfirm.setOnClickListener {
            val error = checkInput()
            if (error != null) {
                toastError(error)
                return@setOnClickListener
            }

            loading()
            viewModel.putKYCInfo(idType,
                ocrInfo?.identityNumber,
                imageUrl,
                edtFirstName.text.toString(),
                edtMiddleName.text.toString(),
                edtLastName.text.toString(),
                tvBirthday.text.toString())
        }
    }

    private fun initStyle() = binding.run {
        toolBar.tvToolbarTitle.setText(R.string.identity)
        frIdType.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_F9FAFD)
        frFirstName.background = defaultBg
        frMiddleName.background = defaultBg
        frLastName.background = defaultBg
        frBirthday.background = defaultBg
        setInputStyle(edtFirstName, ivClearFirstName, frFirstName)
        setInputStyle(edtMiddleName, ivClearMiddleName, frMiddleName)
        setInputStyle(edtLastName, ivClearLastName, frLastName)
    }

    private fun setInputStyle(editText: EditText, ivClear: ImageView, parent: View) {
        ivClear.setOnClickListener { editText.setText("") }
        parent.setOnClickListener { editText.requestFocus() }
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.setTextColor(getColor(R.color.color_025BE8))
                ivClear.visible()
                parent.background = foucedBg
            } else {
                editText.setTextColor(Color.BLACK)
                ivClear.gone()
                parent.background = defaultBg
            }
        }
    }

    private var dateTimePicker: TimePickerView? = null
    private fun showDateTimePicker() {
        KeyboadrdHideUtil.hideSoftKeyboard(binding.root)
        if (dateTimePicker != null) {
            dateTimePicker!!.show()
            return
        }
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -100)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.YEAR, -21)
        dateTimePicker = DateTimePickerOptions(this).getBuilder { date, _ ->
            TimeUtil.dateToStringFormatYMD(date)?.let {
                binding.tvBirthday.setText(it)
            }
        }
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()
        dateTimePicker!!.show()
    }

    private fun checkInput(): String? = binding.run {
        if (!VerifyConstUtil.verifyFullName2(edtFirstName.text.toString())) {
            edtFirstName.requestFocus()
            return@run getString(R.string.P185)
        }

        val middleName = edtMiddleName.text.toString()
        if ("N/A" != middleName && !VerifyConstUtil.verifyFullName2(middleName)) {
            edtMiddleName.requestFocus()
            return@run getString(R.string.P186)
        }

        if (!VerifyConstUtil.verifyFullName2(edtLastName.text.toString())) {
            edtLastName.requestFocus()
            return@run getString(R.string.P187)
        }

        if (tvBirthday.text.toString().isEmptyStr()) {
            return@run getString(R.string.J902)
        }

        return@run null
    }

    private fun toastError(filed: String) {
        toast("$filed: ${getString(R.string.N280)}")
    }
}