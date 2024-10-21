package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_kyc_form.*
import kotlinx.android.synthetic.main.include_kyc_form_select.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.KYCEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityKycFormBinding
import org.cxct.sportlottery.databinding.DialogBottomSelectBinding
import org.cxct.sportlottery.databinding.IncludeKycFormInputBinding
import org.cxct.sportlottery.databinding.IncludeKycFormSelectBinding
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
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.view.dialog.SourceOfIncomeDialog
import org.cxct.sportlottery.view.onFocusChange
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
        binding.toolBar.tvToolbarTitle.setText(R.string.B463)
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
                startActivity(Intent(this,VerifyHandheldActivity::class.java))
                finishWithOK()
            }
        }
        profileModel.userDetail.observe(this) {
            uide = it.t
            setIdentityDetail(it)
            setEnableButton()
        }
        viewModel.kycVerifyConfigResult.observe(this) {
            if (it.success){
                it.getData()?.let {
                    LogUtil.toJson(it)
                    kycVerifyConfig = it
                    setKYCVerifyConfig(it)
                    setEnableButton()
                }
            }else{
                showErrorPromptDialog(it.msg){}
            }
        }
    }

    private fun bindOCRInfo(ocr: OCRInfo) = binding.run {
        itemFirstName.etInput.setText(ocr.firstName)
        itemMiddleName.etInput.setText(ocr.middleName)
        itemLastName.etInput.setText(ocr.lastName)
        itemBirthday.etInput.setText(ocr.birthday?.replace("/", "-"))
        setEnableButton()
    }
    private fun setIdentityDetail(it: UserInfoDetailsEntity)=binding.run{
        itemNationality.tvInput.text = it.t.nationality
        itemGender.tvInput.text = profileModel.getGenderName(it.t.gender)
        itemPlaceOfBirth.etInput.setText(it.t.placeOfBirth)
        itemSourceOfIncome.tvInput.text = if (it.t.salarySource?.id == 6) {
            it.t.salarySource?.name
        } else if (it.t.salarySource?.id == null) {
            null
        } else {
            it.t.salarySource?.id?.let { it1 ->
                profileModel.getSalaryName(it1, resources.getString(R.string.set))
            }
        }
        itemNatureOfWork.tvInput.text = it.t.natureOfWork
        itemProvinceCurrent.tvInput.text = it.t.province
        itemCityCurrent.tvInput.text = it.t.city
        itemAddressCurrent.etInput.setText(it.t.address)
        itemZipCodeCurrent.etInput.setText(it.t.zipCode)
        itemProvincePermanent.tvInput.text = it.t.permanentProvince
        itemCityPermanent.tvInput.text = it.t.permanentCity
        itemAddressPermanent.etInput.setText(it.t.permanentAddress)
        itemZipCodePermanent.etInput.setText(it.t.permanentZipCode)
    }

    private fun initEvent() = binding.run {
        toolBar.btnToolbarBack.setOnClickListener { finish() }
        tvIdType.text = idTypeName
        setItemInputStyle(itemFirstName,getString(R.string.P185),1,1){
            setEnableButton()
        }
        setItemInputStyle(itemMiddleName,getString(R.string.P186),1,1){
            setEnableButton()
        }
        setItemInputStyle(itemLastName,getString(R.string.P187),1,1){
            setEnableButton()
        }
        setItemInputStyle(itemBirthday,getString(R.string.J902),1,1){
            setEnableButton()
        }
        itemBirthday.etInput.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isClickable = true
            hint = "YYYY-MM-DD"
            setTypeface(null, Typeface.BOLD)
        }
        setOnClickListeners(itemBirthday.llRoot,itemBirthday.etInput,itemBirthday.ivClear){
            showDateTimePicker()
        }
        itemBirthday.ivClear.setImageResource(R.drawable.ic_date)
        itemBirthday.ivClear.setOnClickListener {
            showDateTimePicker()
        }
        llMiddleNameSwitch.setOnClickListener {
            val isSelected = !it.isSelected
            it.isSelected = isSelected
            setItemInputStyle(itemMiddleName,getString(R.string.P186),1,if(isSelected) 0 else 1){
                setEnableButton()
            }
            itemMiddleName.root.isEnabled = !isSelected
            itemMiddleName.etInput.isEnabled = !isSelected
            ivCheckBox.isSelected = isSelected
            if (isSelected) {
                itemMiddleName.etInput.setText("N/A")
                itemMiddleName.etInput.clearFocus()
                tvHaveMiddelName.setTextColor(getColor(R.color.color_025BE8))
                itemMiddleName.ivClear.gone()
            } else {
                itemMiddleName.etInput.setText("")
                itemMiddleName.etInput.requestFocus()
                tvHaveMiddelName.setTextColor(getColor(R.color.color_667085))
            }
            setEnableButton()
        }

        btnConfirm.setOnClickListener {
            loading()
            viewModel.putKYCInfo(idType,
                ocrInfo?.identityNumber,
                imageUrl,
                itemFirstName.etInput.text.toString(),
                itemMiddleName.etInput.text.toString(),
                itemLastName.etInput.text.toString(),
                itemBirthday.etInput.text.toString(),
                uide = uide
            )
        }
    }
    private fun initExtra()=binding.run{
        cbPermanent.setOnCheckedChangeListener { buttonView, isChecked ->
            linPermanent.isVisible = !isChecked
            setEnableButton()
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
                binding.itemBirthday.etInput.setText(it)
                setEnableButton()
            }
        }
            .setLayoutRes(R.layout.dialog_date_select, object : CustomListener {
                override fun customLayout(v: View) {
                    //自定义布局中的控件初始化及事件处理
                    v.findViewById<View>(R.id.btnBtmCancel).setOnClickListener {
                        dateTimePicker?.dismiss()
                    }
                    v.findViewById<View>(R.id.btnBtmDone).setOnClickListener {
                        dateTimePicker?.returnData()
                        dateTimePicker?.dismiss()
                    }

                }
            })
            .setItemVisibleCount(6)
            .setLineSpacingMultiplier(2.0f)
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()
        dateTimePicker!!.show()
    }
    private fun setEnableButton(){
        binding.btnConfirm.isEnabled = checkComplete()
    }
    private fun checkComplete(): Boolean = binding.run {
        if (!VerifyConstUtil.verifyFullName2(itemFirstName.etInput.text.toString())) {
            return false
        }

        val middleName = itemMiddleName.etInput.text.toString()
        if ("N/A" != middleName && !VerifyConstUtil.verifyFullName2(middleName)) {
            return false
        }

        if (!VerifyConstUtil.verifyFullName2(itemLastName.etInput.text.toString())) {
            return false
        }

        if (itemBirthday.etInput.text.toString().isEmptyStr()) {
            return false
        }
        if (uide.nationality.isEmptyStr() && kycVerifyConfig?.kycVerifyNationalityRequired==1) {
            return false
        }
        if (uide.gender==null && kycVerifyConfig?.kycVerifyGenderRequired==1) {
            return false
        }
        if (uide.placeOfBirth.isEmptyStr() && kycVerifyConfig?.kycVerifyBirthplaceRequired==1) {
            return false
        }
        if (uide.salarySource==null && kycVerifyConfig?.kycVerifyIncomeRequired==1) {
            return false
        }
        if (uide.natureOfWork.isEmptyStr() && kycVerifyConfig?.kycVerifyWorkRequired==1) {
            return false
        }
        if (linAddress.isVisible && kycVerifyConfig?.kycVerifyCurrAddressRequired==1){
            if (uide.province.isEmptyStr()) {
                return false
            }
            if (uide.city.isEmptyStr()) {
                return false
            }
            if (uide.address.isEmptyStr()) {
                return false
            }
            if (uide.zipCode.isEmptyStr()) {
                return false
            }
        }
        if (cbPermanent.isVisible){
            if (cbPermanent.isChecked){
                uide.permanentProvince = uide.province
                uide.permanentCity = uide.city
                uide.permanentAddress = uide.address
                uide.permanentZipCode = uide.zipCode
            }else{
                uide.permanentProvince = itemProvincePermanent.tvInput.text.toString()
                uide.permanentCity = itemCityPermanent.tvInput.text.toString()
                uide.permanentAddress = itemAddressPermanent.etInput.text.toString()
                uide.permanentZipCode = itemZipCodePermanent.etInput.text.toString()
            }
        }
        if (linPermanent.isVisible && kycVerifyConfig?.kycVerifyPermanentAddressRequired==1){
            if (uide.permanentProvince.isEmptyStr()) {
                return false
            }
            if (uide.permanentCity.isEmptyStr()) {
                return false
            }
            if (uide.permanentAddress.isEmptyStr()) {
                return false
            }
            if (uide.permanentZipCode.isEmptyStr()) {
                return false
            }
        }
        return true
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
            binding.itemProvinceCurrent.tvInput.text.toString()
        ) {
            profileModel.updateCityData(it.id)
            if (it.name != binding.itemProvinceCurrent.tvInput.text.toString()) {
                binding.itemCityCurrent.tvInput.text = profileModel.cityList.first().name
                uide.city = profileModel.cityList.first().name
            }
            binding.itemProvinceCurrent.tvInput.text = it.name
            uide.province = it.name
            setEnableButton()
        }
    }

    private fun showProvincePDialog() {
        if (profileModel.areaData == null) {
            return
        }
        showBottomDialog(
            profileModel.provincesPList,
            resources.getString(R.string.J036),
            binding.itemProvincePermanent.tvInput.text.toString()
        ) {
            profileModel.updateCityPData(it.id)
            if (it.name != binding.itemProvincePermanent.tvInput.text.toString()) {
                binding.itemCityPermanent.tvInput.text = profileModel.cityPList.first().name
                uide.permanentCity = profileModel.cityPList.first().name
            }
            binding.itemProvincePermanent.tvInput.text = it.name
            uide.permanentProvince = it.name
            setEnableButton()
        }
    }
    private fun setKYCVerifyConfig(config: KYCVerifyConfig)=binding.run{
        setItemSelectStyle(itemNationality,getString(R.string.P103), config.kycVerifyNationalityShow,config.kycVerifyNationalityRequired){
            showBottomDialog(
                profileModel.nationalityList,
                resources.getString(R.string.P103),
                itemNationality.tvInput.text.toString()
            ) {
                itemNationality.tvInput.text = it.name
                uide.nationality = it.name
                setEnableButton()
            }
        }
        setItemSelectStyle(itemGender,getString(R.string.J905), config.kycVerifyGenderShow,config.kycVerifyGenderRequired){
            showBottomDialog(
                profileModel.genderList,
                resources.getString(R.string.J905),
                itemGender.tvInput.text.toString()
            ) {
                itemGender.tvInput.text = it.name
                uide.gender = it.id
                setEnableButton()
            }
        }
        setItemInputStyle(itemPlaceOfBirth,getString(R.string.P104), config.kycVerifyBirthplaceShow,config.kycVerifyBirthplaceRequired){
              uide.placeOfBirth = it
              setEnableButton()
        }
        setItemSelectStyle(itemSourceOfIncome,getString(R.string.P105), config.kycVerifyIncomeShow,config.kycVerifyIncomeRequired){
            showBottomDialog(
                profileModel.salaryStringList,
                resources.getString(R.string.P105),
                itemSourceOfIncome.tvInput.text.toString(),
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
                            itemSourceOfIncome.tvInput.text = workstr
                            uide.salarySource = SalarySource(
                                it.id,
                                workstr
                            )
                            setEnableButton()
                        }
                    })
                    dialog.show()
                } else {
                    itemSourceOfIncome.tvInput.text = it.name
                    uide.salarySource = SalarySource(
                        it.id,
                        it.name
                    )
                    setEnableButton()
                }
            }
        }
        setItemSelectStyle(itemNatureOfWork,getString(R.string.P106), config.kycVerifyWorkShow,config.kycVerifyWorkRequired){
            showBottomDialog(
                profileModel.workList,
                resources.getString(R.string.P106),
                itemNatureOfWork.tvInput.text.toString()
            ) {
                itemNatureOfWork.tvInput.text = it.name
                uide.natureOfWork = it.name
                setEnableButton()
            }
        }

        linAddress.isVisible = config.kycVerifyCurrAddressShow == 1
        if(config.kycVerifyCurrAddressRequired==1) tvHomeCurrent.setRequiredStyle()
        setItemSelectStyle(itemProvinceCurrent,getString(R.string.J036), config.kycVerifyCurrAddressShow,config.kycVerifyCurrAddressRequired){
            showProvinceDialog()
        }
        setItemSelectStyle(itemCityCurrent,getString(R.string.J901), config.kycVerifyCurrAddressShow,config.kycVerifyCurrAddressRequired){
            if (profileModel.cityList.isEmpty()) {
                showProvinceDialog()
            } else {
                showBottomDialog(
                    profileModel.cityList,
                    resources.getString(R.string.J901),
                    itemCityCurrent.tvInput.text.toString()
                ) {
                    itemCityCurrent.tvInput.text = it.name
                    uide.city = it.name
                    setEnableButton()
                }
            }

        }
        setItemInputStyle(itemAddressCurrent,getString(R.string.M259), config.kycVerifyCurrAddressShow,config.kycVerifyCurrAddressRequired){
            uide.address = it
            setEnableButton()
        }
        setItemInputStyle(itemZipCodeCurrent,getString(R.string.N827), config.kycVerifyCurrAddressShow,config.kycVerifyCurrAddressRequired){
            uide.zipCode = it
            setEnableButton()
        }
        cbPermanent.isVisible = config.kycVerifyCurrAddressShow == 1 && config.kycVerifyPermanentAddressShow == 1
        linPermanent.isVisible = config.kycVerifyPermanentAddressShow == 1
        if(config.kycVerifyPermanentAddressRequired==1) tvHomePermanent.setRequiredStyle()
        setItemSelectStyle(itemProvincePermanent,getString(R.string.J036), config.kycVerifyPermanentAddressShow,config.kycVerifyPermanentAddressRequired){
            showProvincePDialog()
        }
        setItemSelectStyle(itemCityPermanent,getString(R.string.J901), config.kycVerifyPermanentAddressShow,config.kycVerifyPermanentAddressRequired){
            if (profileModel.cityPList.isEmpty()) {
                showProvincePDialog()
            } else {
                showBottomDialog(
                    profileModel.cityPList,
                    resources.getString(R.string.J901),
                    itemCityPermanent.tvInput.text.toString()
                ) {
                    itemCityPermanent.tvInput.text = it.name
                    uide.permanentCity = it.name
                    setEnableButton()
                }
            }
        }
        setItemInputStyle(itemAddressPermanent,getString(R.string.M259), config.kycVerifyPermanentAddressShow,config.kycVerifyPermanentAddressRequired){
            uide.permanentAddress = it
            setEnableButton()
        }
        setItemInputStyle(itemZipCodePermanent,getString(R.string.N827), config.kycVerifyPermanentAddressShow,config.kycVerifyPermanentAddressRequired){
            uide.permanentZipCode = it
            setEnableButton()
        }
        //勾选框可见的情况下，默认选中
        if (cbPermanent.isVisible){
            cbPermanent.isChecked = true
        }
    }
    private fun TextView.setRequiredStyle(){
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_star_required,0,0,0)
        compoundDrawablePadding = 2.dp
    }
    private fun setItemSelectStyle(binding: IncludeKycFormSelectBinding,label: String, needShow: Int, required: Int, onItemClick: ()->Unit) {
        binding.tvLabel.text = label
        binding.root.isVisible = needShow == 1
        if(required==1) binding.tvLabel.setRequiredStyle()
        binding.tvInput.checkRegisterListener{
            binding.tvError.isVisible = required==1 && it.trim().isEmptyStr()
        }
        binding.llRoot.setOnClickListener { onItemClick.invoke() }
    }
    private fun setItemInputStyle(binding: IncludeKycFormInputBinding,label: String, needShow:Int, required: Int, onTextChanged: (String)->Unit) {
        binding.tvLabel.text = label
        binding.etInput.hint = label
        binding.root.isVisible = needShow == 1
        if(required==1) binding.tvLabel.setRequiredStyle()
        binding.etInput.checkRegisterListener{
            binding.tvError.isVisible = required==1 && it.trim().isEmptyStr()
            binding.ivClear.isVisible = it.trim().isNotEmpty()
            onTextChanged.invoke(it)
        }
        binding.ivClear.setOnClickListener {
            binding.etInput.setText(null)
        }
    }
}