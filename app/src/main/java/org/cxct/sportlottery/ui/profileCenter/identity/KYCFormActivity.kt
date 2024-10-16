package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_profile.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.KYCEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityKycFormBinding
import org.cxct.sportlottery.databinding.DialogBottomSelectBinding
import org.cxct.sportlottery.net.user.data.KYCVerifyConfig
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signUp.info.DateTimePickerOptions
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyHandheldActivity
import org.cxct.sportlottery.ui.profileCenter.profile.*
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.dialog.SourceOfIncomeDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class KYCFormActivity: BaseActivity<ProfileCenterViewModel, ActivityKycFormBinding>() {

    override fun pageName() = "KYC提交页面"

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
    private val profileModel: ProfileModel by viewModel()

    private val defaultBg = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_F9FAFD)
    private val foucedBg = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.white, R.color.color_025BE8)

    private val idType by lazy { intent.getIntExtra("idType", 0) }
    private val idTypeName by lazy { intent.getStringExtra("idTypeName")!! }
    private val imageUrl by lazy { intent.getStringExtra("imageUrl")!! }
    private val ocrInfo by lazy { intent.getParcelableExtra<OCRInfo>("ocrInfo") }

    private val dialogBinding by lazy { DialogBottomSelectBinding.inflate(layoutInflater)  }
    private val bottomSheet by lazy {
        BottomSheetDialog(this).apply {
            dialogBinding.rvBtmData.adapter = dialogBtmAdapter
            dialogBinding.btnBtmCancel.setOnClickListener { this.dismiss() }
            setContentView(dialogBinding.root)
        }
    }
    private var dialogBtmAdapter = DialogBottomDataAdapter()
    private var uide = Uide()
    private var kycVerifyConfig: KYCVerifyConfig? = null

    override fun onInitView() {
        setStatusbar()
        initStyle()
        initEvent()
        initExtra()
        initObserver()
        profileModel.getUserInfo()
        profileModel.getUserSalaryList()
        ocrInfo?.let { bindOCRInfo(it) }
        viewModel.getKycNeedInformation()
     }

    private fun initObserver() {
        viewModel.kycResult.observe(this) {
            hideLoading()
            if (!it.succeeded()) {
                showErrorPromptDialog(it.msg) {  }
            } else {
                EventBusUtil.post(KYCEvent())
                UserInfoRepository.loadUserInfo()
                showPromptDialog(message = getString(R.string.submit_success)) {
                    startActivity(Intent(this,VerifyHandheldActivity::class.java))
                    finishWithOK()
                }
            }
        }
        profileModel.userDetail.observe(this) {
            uide = it.t
            setIdentityDetail(it)
        }
        viewModel.kycVerifyConfigResult.observe(this) {
            if (it.success){
                it.getData()?.let {
                    kycVerifyConfig = it
                    setKYCVerifyConfig(it)
                }
            }else{
                showErrorPromptDialog(it.msg){}
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
    private fun setIdentityDetail(it: UserInfoDetailsEntity)=binding.run{
        tvNationality.text = it.t.nationality
//        tvBirthday.text = checkStr(it.t.birthday)
        etPlaceOfBirth.setText(it.t.placeOfBirth)
        tvSourceOfIncome.text = if (it.t.salarySource?.id == 6) {
            it.t.salarySource?.name
        } else if (it.t.salarySource?.id == null) {
            null
        } else {
            it.t.salarySource?.id?.let { it1 ->
                profileModel.getSalaryName(it1, resources.getString(R.string.set))
            }
        }
        tvNatureOfWork.text = it.t.natureOfWork
        tvProvinceCurrent.text = it.t.province
        tvCityCurrent.text = it.t.city
        etAddressCurrent.setText(it.t.address)
        etZipCodeCurrent.setText(it.t.zipCode)
        tvProvincePermanent.text = it.t.permanentProvince
        tvCityPermanent.text = it.t.permanentCity
        etAddressPermanent.setText(it.t.permanentAddress)
        etZipCodePermanent.setText(it.t.permanentZipCode)
        tvGender.text = profileModel.getGenderName(it.t.gender)
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
        etPlaceOfBirth.afterTextChanged {
            uide.placeOfBirth = it
        }
        etAddressCurrent.afterTextChanged{
            uide.address = it
        }
        etZipCodeCurrent.afterTextChanged{
            uide.zipCode = it
        }
        etAddressPermanent.afterTextChanged{
            uide.permanentAddress = it
        }
        etZipCodePermanent.afterTextChanged{
            uide.permanentZipCode = it
        }
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
                tvBirthday.text.toString(),
                uide = uide
            )
        }
    }
    private fun initExtra()=binding.run{
        llNationality.setOnClickListener {
            showBottomDialog(
                profileModel.nationalityList,
                resources.getString(R.string.P103),
                tvNationality.text.toString()
            ) {
                tvNationality.text = it.name
                uide.nationality = it.name
                checkInput()
            }
        }
        llGender.setOnClickListener {
            showBottomDialog(
                profileModel.genderList,
                resources.getString(R.string.J905),
                tvGender.text.toString()
            ) {
                tvGender.text = it.name
                uide.gender = it.id
            }
        }
        llSourceOfIncome.setOnClickListener {
            showBottomDialog(
                profileModel.salaryStringList,
                resources.getString(R.string.P105),
                tvSourceOfIncome.text.toString(),
                true
            ) {
                if (it.id == 6) {
                    val dialog = SourceOfIncomeDialog(this@KYCFormActivity)
                    dialog.setPositiveClickListener(object :
                        SourceOfIncomeDialog.OnPositiveListener {
                        override fun positiveClick(str: String) {
                            val workstr = str.ifEmpty {
                                resources.getString(R.string.other)
                            }
                            tvSourceOfIncome.text = workstr
                            uide.salarySource = SalarySource(
                                it.id,
                                workstr
                            )
                        }
                    })
                    dialog.show()
                } else {
                    tvSourceOfIncome.text = it.name
                    uide.salarySource = SalarySource(
                        it.id,
                        it.name
                    )
                }
            }
        }
        llNatureOfWork.setOnClickListener {
            showBottomDialog(
                profileModel.workList,
                resources.getString(R.string.P106),
                tvNatureOfWork.text.toString()
            ) {
                tvNatureOfWork.text = it.name
                uide.natureOfWork = it.name
            }
        }
        cbPermanent.setOnCheckedChangeListener { buttonView, isChecked ->
            linPermanent.isVisible = !isChecked
            if (isChecked){
                uide.permanentProvince = uide.province
                uide.permanentCity = uide.city
                uide.permanentAddress = uide.address
                uide.permanentZipCode = uide.zipCode
            }else{
                uide.permanentProvince = tvProvincePermanent.text.toString().trim()
                uide.permanentCity = tvCityPermanent.text.toString().trim()
                uide.permanentAddress = etAddressPermanent.text.toString().trim()
                uide.permanentZipCode = etZipCodePermanent.text.toString().trim()
            }
        }
        llProvinceCurrent.setOnClickListener {
            showProvinceDialog()
        }
        llProvincePermanent.setOnClickListener {
            showProvincePDialog()
        }
        llCityCurrent.setOnClickListener {
            if (profileModel.cityList.isEmpty()) {
                showProvinceDialog()
            } else {
                showBottomDialog(
                    profileModel.cityList,
                    resources.getString(R.string.J901),
                    tvCityCurrent.text.toString()
                ) {
                    tvCityCurrent.text = it.name
                    uide.city = it.name
                }
            }

        }
        llCityPermanent.setOnClickListener {
            if (profileModel.cityPList.isEmpty()) {
                showProvincePDialog()
            } else {
                showBottomDialog(
                    profileModel.cityPList,
                    resources.getString(R.string.J901),
                    tvCityPermanent.text.toString()
                ) {
                    tvCityPermanent.text = it.name
                    uide.permanentCity = it.name
                }
            }

        }
        ivClearAddressCurrent.setOnClickListener {
            etAddressCurrent.text = null
            uide.address = null
        }
        ivClearAddressPermanent.setOnClickListener {
            etAddressPermanent.text = null
            uide.permanentAddress = null
        }
        ivClearZipCodeCurrent.setOnClickListener {
            etZipCodeCurrent.text = null
            uide.zipCode = null
        }
        ivClearZipCodePermanent.setOnClickListener {
            etZipCodePermanent.text = null
            uide.permanentZipCode = null
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
        if (tvNationality.text.toString().isEmptyStr() && kycVerifyConfig?.kycVerifyNationalityRequired==1) {
            return@run getString(R.string.P103)
        }
        if (tvGender.text.toString().isEmptyStr() && kycVerifyConfig?.kycVerifyNationalityRequired==1) {
            return@run getString(R.string.J905)
        }
        if (etPlaceOfBirth.text.toString().trim().isEmptyStr() && kycVerifyConfig?.kycVerifyBirthplaceRequired==1) {
            return@run getString(R.string.P104)
        }
        if (tvSourceOfIncome.text.toString().trim().isEmptyStr() && kycVerifyConfig?.kycVerifyIncomeRequired==1) {
            return@run getString(R.string.P105)
        }
        if (tvNatureOfWork.text.toString().trim().isEmptyStr() && kycVerifyConfig?.kycVerifyWorkRequired==1) {
            return@run getString(R.string.P106)
        }
        if (linAddress.isVisible){
            if (tvProvinceCurrent.text.toString().trim().isEmptyStr()) {
                return@run getString(R.string.J036)
            }
            if (tvCityCurrent.text.toString().trim().isEmptyStr()) {
                return@run getString(R.string.J901)
            }
            if (etAddressCurrent.text.toString().trim().isEmptyStr()) {
                return@run getString(R.string.M259)
            }
            if (etZipCodeCurrent.text.toString().trim().isEmptyStr()) {
                return@run getString(R.string.N827)
            }
        }

        if (cbPermanent.isVisible){
            if (!cbPermanent.isChecked){
                if (tvProvincePermanent.text.toString().trim().isEmptyStr()) {
                    return@run getString(R.string.J036)
                }
                if (tvCityPermanent.text.toString().trim().isEmptyStr()) {
                    return@run getString(R.string.J901)
                }
                if (etAddressPermanent.text.toString().trim().isEmptyStr()) {
                    return@run getString(R.string.M259)
                }
                if (etZipCodePermanent.text.toString().trim().isEmptyStr()) {
                    return@run getString(R.string.N827)
                }
            }
        }

        return@run null
    }

    private fun toastError(filed: String) {
        toast("$filed: ${getString(R.string.N280)}")
    }
    private fun showBottomDialog(
        list: MutableList<DialogBottomDataEntity>,
        title: String,
        currStr: String?,
        sourceOtherFlag: Boolean = false,
        callBack: (item: DialogBottomDataEntity) -> Unit
    ) {
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
        if (sourceOtherFlag && !trueFlag && listNew.isNotEmpty()) {
            listNew.last().flag = true
        }
        dialogBinding.tvBtmTitle.text = title
        dialogBtmAdapter.data = listNew
        dialogBtmAdapter.notifyDataSetChanged()
        dialogBinding.rvBtmData.scrollToPosition(0)
        dialogBtmAdapter.setOnItemClickListener { ater, view, position ->
            dialogBtmAdapter.data.forEach {
                it.flag = false
            }
            item = dialogBtmAdapter.data[position]
            item!!.flag = true
            dialogBtmAdapter.notifyDataSetChanged()
        }
        dialogBinding.btnBtmDone.setOnClickListener {
            item?.let { it1 -> callBack(it1) }
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }
    private fun showProvinceDialog() {
        if (profileModel.areaData == null) {
            return
        }
        showBottomDialog(
            profileModel.provincesList,
            resources.getString(R.string.J036),
            binding.tvProvinceCurrent.text.toString()
        ) {
            profileModel.updateCityData(it.id)
            if (it.name != binding.tvProvinceCurrent.text.toString()) {
                binding.tvCityCurrent.text = profileModel.cityList.first().name
                uide.city = profileModel.cityList.first().name
            }
            binding.tvProvinceCurrent.text = it.name
            uide.province = it.name
        }
    }

    private fun showProvincePDialog() {
        if (profileModel.areaData == null) {
            return
        }
        showBottomDialog(
            profileModel.provincesPList,
            resources.getString(R.string.J036),
            binding.tvProvincePermanent.text.toString()
        ) {
            profileModel.updateCityPData(it.id)
            if (it.name != binding.tvCityPermanent.text.toString()) {
                binding.tvCityPermanent.text = profileModel.cityPList.first().name
                uide.permanentCity = profileModel.cityPList.first().name
            }
            binding.tvProvincePermanent.text = it.name
            uide.permanentProvince = it.name
        }
    }
    fun setKYCVerifyConfig(config: KYCVerifyConfig)=binding.run{
        llNationality.isVisible = config.kycVerifyNationalityShow == 1
        llGender.isVisible = config.kycVerifyGenderShow == 1
        llPlaceOfBirth.isVisible = config.kycVerifyBirthplaceShow == 1
        llSourceOfIncome.isVisible = config.kycVerifyIncomeShow == 1
        llNatureOfWork.isVisible = config.kycVerifyWorkShow == 1
        linAddress.isVisible = config.kycVerifyCurrAddressShow == 1
        linPermanent.isVisible = config.kycVerifyPermanentAddressShow == 1
        cbPermanent.isVisible = config.kycVerifyPermanentAddressShow == 1

        if(config.kycVerifyNationalityRequired==1) tvNationalityLabel.setRequiredStyle()
        if(config.kycVerifyGenderRequired==1) tvGenderLabel.setRequiredStyle()
        if(config.kycVerifyBirthplaceRequired==1) tvPlaceOfBirthLabel.setRequiredStyle()
        if(config.kycVerifyNationalityRequired==1) tvSourceOfIncomeLabel.setRequiredStyle()
        if(config.kycVerifyWorkRequired==1) tvNatureOfWorkLabel.setRequiredStyle()
        if(config.kycVerifyCurrAddressRequired==1) tvHomeCurrent.setRequiredStyle()
        if(config.kycVerifyPermanentAddressRequired==1) tvHomePermanent.setRequiredStyle()
    }
    fun TextView.setRequiredStyle(){
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_star_required,0,0,0)
        compoundDrawablePadding = 2.dp
    }
}