package org.cxct.sportlottery.ui.login.signUp.info

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.checkRegisterListener
import java.util.*

/**
 * 注册补充用户信息
 */
class RegisterInfoActivity : BaseActivity<RegisterInfoViewModel,ActivityRegisterInfoBinding>(RegisterInfoViewModel::class) {

    //生日选择
    private var dateTimePicker: TimePickerView? = null

    //省选择器
    private var provincePicker: OptionsPickerView<String>? = null

    //市选择器
    private var cityPicker: OptionsPickerView<String>? = null

    //薪资来源选择器
    private var salaryPicker: OptionsPickerView<String>? = null


    override fun onInitView() {
        KeyboadrdHideUtil.init(this)
        initToolsBar()
        initView()
        initData()
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        loading()
        viewModel.loginResult = intent.getParcelableExtra("data") as LoginResult?
        //请求地址列表
        viewModel.getAddressData()
        //请求薪资来源列表
        viewModel.getUserSalaryList()

        //地址数据
        viewModel.areaAllList.observe(this) {
            //请求到了空的行政区域
            if(it==null||it.provinces.isEmpty()||it.cities.isEmpty()){
                finishPage()
            }
            val provinceList = viewModel.getProvinceStringList()
            provincePicker?.setPicker(provinceList)
        }
        //薪资来源
        viewModel.salaryList.observe(this) {
            salaryPicker?.setPicker(viewModel.salaryStringList)
            //获取用户基础信息
            viewModel.getUserBasicInfo()
        }

        //基本信息
        viewModel.userBasicInfoEvent.observe(this) {
            hideLoading()
            if (viewModel.provinceInput.isNotEmpty()) {
                binding.etAddress.setText(viewModel.provinceInput)
                cityPicker?.setPicker(viewModel.getCityStringListByProvince())
            }
            if (viewModel.cityInput.isNotEmpty()) {
                binding.etCity.setText(viewModel.cityInput)
            }
            if(viewModel.phoneNumberInput.isNotEmpty()){
                binding.eetPhoneNumber.setText(viewModel.phoneNumberInput)
            }
            if(viewModel.emailInput.isNotEmpty()){
                binding.eetEmail.setText(viewModel.emailInput)
            }

            if (!it.firstName.isEmptyStr() && "N/A" != it.firstName?.toUpperCase()) { // 如果firstName不为空则认为设置过真实姓名，不让再编辑
                binding.eetFirstName.setText(it.firstName)
                binding.eetFirstName.isEnabled = false
                binding.eedtMiddleName.isEnabled = false
                val haveMiddle = !it.middleName.isEmptyStr() && "N/A" != it.middleName?.toUpperCase()
                binding.cbNoMiddleName.isEnabled = false
                if (haveMiddle) {
                    binding.eedtMiddleName.setText(it.middleName)
                } else {
                    binding.cbNoMiddleName.isChecked = true
                }
                binding.eedtLastName.setText(it.lastName)
                binding.eedtLastName.isEnabled = false
            }

            binding.etBirthday.setText(viewModel.birthdayTimeInput)
            binding.etSource.setText(viewModel.getSalaryNameById())


            if (viewModel.filledEmail) {
                binding.eetEmail.isEnabled = false
                binding.tvEmail.visible()
            }
            if (viewModel.filledPhone) {
                binding.eetPhoneNumber.isEnabled = false
                binding.tvPhoneNumber.visible()
            }
        }

        //提交表单
        viewModel.commitEvent.observe(this) {
            hideLoading()
            if (it) {
                viewModel.loginResult?.let {
                    finishPage()
                }
            } else {
                ToastUtil.showToast(this, viewModel.commitMsg)
            }
        }
    }


    override fun onBackPressed() {
        finishPage()
    }

    private fun finishPage() {
        SPUtil.saveLoginInfoSwitch()
        //返回继续完成登录
        viewModel.loginResult?.let { result ->
            //返回继续完成登录
            EventBusUtil.post(RegisterInfoEvent(result))
        }
        finish()
    }

    private fun initNameLayout() = binding.run {
        eetFirstName.checkRegisterListener {
            viewModel.firstName = it
            checkInput(eetFirstName, etFirstName)
        }
        eedtMiddleName.checkRegisterListener {
            viewModel.middleName = it
            checkInput(eedtMiddleName, edtMiddleName, !cbNoMiddleName.isChecked)
        }
        eedtLastName.checkRegisterListener {
            viewModel.lastName = it
            checkInput(eedtLastName, edtLastName)
        }
        cbNoMiddleName.setOnCheckedChangeListener { _, isChecked ->
            viewModel.noMiddleName = isChecked
            if (isChecked) {
                eedtMiddleName.isEnabled = false
                eedtMiddleName.setText("N/A")
            } else {
                eedtMiddleName.isEnabled = true
                eedtMiddleName.setText("")
                checkStatus()
            }
            edtMiddleName.setError(null, true)
        }
    }

    private fun checkInput(editText: ExtendedEditText, textFormFieldBoxes: TextFormFieldBoxes, needCheck: Boolean = true) {

        if (needCheck) {
            val inputString = editText.text.toString()
            if (inputString.isEmpty()) {
                textFormFieldBoxes.setError(getString(R.string.error_input_empty), false)
            } else {
                textFormFieldBoxes.setError(if (VerifyConstUtil.verifyFullName2(inputString)) "" else getString(R.string.N280), false)
            }
        }

        checkStatus()
    }

    private fun initView() {
        initDateTimeView()
        initAddressPickerView()
        initCityPickerView()
        initSalaryPickerView()
        initNameLayout()

        //选择生日点击
        binding.tvBirthday.setOnClickListener {
            hideSoftKeyboard()
            if (viewModel.filledBirthday) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            dateTimePicker?.show()
        }

        //选择省地址点击
        binding.tvAddress.setOnClickListener {
            hideSoftKeyboard()
            if (viewModel.filledProvince) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            provincePicker?.show()
        }

        binding.tvCity.setOnClickListener {
            hideSoftKeyboard()
            if (viewModel.filledCity) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            if (viewModel.provinceInput.isEmpty()) {
                provincePicker?.show()
                return@setOnClickListener
            }
            cityPicker?.show()
        }

        //选择薪资来源点击
        binding.tvSalary.setOnClickListener {
            hideSoftKeyboard()
            if (viewModel.filledSalary) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            salaryPicker?.show()
        }

        binding.tvPhoneNumber.setOnClickListener {
            ToastUtil.showToastInCenter(this, getString(R.string.N887))
        }
        binding.tvEmail.setOnClickListener {
            ToastUtil.showToastInCenter(this, getString(R.string.N887))
        }

        //提交点击
        binding.btnCommit.setOnClickListener {
            loading()
            viewModel.commitUserBasicInfo()
        }

        setOnClickListeners(binding.btnBack, binding.btnSkip) { finishPage() }

        binding.eetPhoneNumber.checkRegisterListener {
            val msg=viewModel.checkPhone(it)
            binding.etPhoneNumber.setError(msg,false)
        }
        binding.eetEmail.checkRegisterListener {
            val msg=viewModel.checkEmail(it)
            binding.etEmail.setError(msg,false)
        }
        binding.eetPhoneNumber.addTextChangedListener {
            viewModel.phoneNumberInput=binding.eetPhoneNumber.text.toString()
            checkStatus()
        }
        binding.eetEmail.addTextChangedListener {
            viewModel.emailInput=binding.eetEmail.text.toString()
            checkStatus()
        }
        binding.bottomLiences.tvLicense.text = Constants.copyRightString
    }

    private fun initToolsBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(binding.vTop.root)
            .fitsSystemWindows(false)
            .init()
    }


    /**
     * 提交按钮高亮判断
     */
    private fun checkStatus() {
        val status = viewModel.checkInput()
                && (viewModel.firstName.isNotEmpty() && !binding.etFirstName.isOnError)
                && (viewModel.lastName.isNotEmpty() && !binding.edtLastName.isOnError)
                && (viewModel.middleName.isNotEmpty() && !binding.edtMiddleName.isOnError)

        binding.btnCommit.isEnabled = status
        if (status) {
            binding.btnCommit.alpha = 1f
        } else {
            binding.btnCommit.alpha = 0.5f
        }
    }

    /**
     * 初始化地址选择控件
     */
    @SuppressLint("SetTextI18n")
    private fun initAddressPickerView() {
        provincePicker = StringPickerOptions(this)
            .getBuilder { provincePosition, _, _, _ ->
                viewModel.setProvinceData(provincePosition)
                //赋值城市列表
                val cityList = viewModel.getCityStringListByProvince()
                cityPicker?.setPicker(cityList)

                binding.etAddress.setText(viewModel.provinceInput)
                checkStatus()
            }
            .setTitleText(resources.getString(R.string.select_area))
            .setSubmitText(getString(R.string.btn_sure))
            .build()
    }


    private fun initCityPickerView() {
        cityPicker = StringPickerOptions(this)
            .getBuilder { cityPosition, _, _, _ ->
                viewModel.setCityData(cityPosition)
                binding.etCity.setText(viewModel.cityInput)
                checkStatus()
            }
            .setTitleText(resources.getString(R.string.select_city))
            .setSubmitText(getString(R.string.btn_sure))
            .build()
    }


    /**
     * 初始化薪资来源控件
     */
    private fun initSalaryPickerView() {
        salaryPicker = StringPickerOptions(this)
            .getBuilder { options1, _, _, _ ->
                viewModel.salaryList.value?.let {
                    it[options1].let { salary ->
                        viewModel.sourceInput = salary.id
                        binding.etSource.setText(salary.name)
                        checkStatus()
                    }
                }

            }
            .setTitleText(resources.getString(R.string.N848))
            .setSubmitText(getString(R.string.btn_sure))
            .build()
    }

    /**
     * 初始化时间选择控件
     */
    private fun initDateTimeView() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -100)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.YEAR, -21)
        tomorrow.add(Calendar.DAY_OF_MONTH, -1)
        dateTimePicker = DateTimePickerOptions(this).getBuilder { date, _ ->
            TimeUtil.dateToStringFormatYMD(date)?.let {
                viewModel.birthdayTimeInput = it
                binding.etBirthday.setText(it)
                checkStatus()
            }
        }
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()
    }



}