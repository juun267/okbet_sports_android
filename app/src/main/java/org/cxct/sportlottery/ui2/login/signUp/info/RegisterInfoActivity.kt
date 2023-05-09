package org.cxct.sportlottery.ui2.login.signUp.info

import android.annotation.SuppressLint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.SPUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import java.util.*

/**
 * 注册补充用户信息
 */
class RegisterInfoActivity : BaseActivity<RegisterInfoViewModel>(RegisterInfoViewModel::class) {

    private lateinit var binding: ActivityRegisterInfoBinding

    //生日选择
    private var dateTimePicker: TimePickerView? = null

    //省选择器
    private var provincePicker: OptionsPickerView<String>? = null

    //市选择器
    private var cityPicker: OptionsPickerView<String>? = null

    //薪资来源选择器
    private var salaryPicker: OptionsPickerView<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolsBar()
        initView()
        initData()
    }


    @SuppressLint("SetTextI18n")
    private fun initData() {
        loading()
        viewModel.loginResult = intent.getSerializableExtra("data") as LoginResult?
        //请求地址列表
        viewModel.getAddressData()
        //请求薪资来源列表
        viewModel.getUserSalaryList()

        //地址数据
        viewModel.areaAllList.observe(this) {
            hideLoading()
            //请求到了空的行政区域
            if(it.provinces.isEmpty()||it.cities.isEmpty()){
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
            binding.etRealName.setText(viewModel.realNameInput)

            binding.etBirthday.setText(viewModel.birthdayTimeInput)
            binding.etSource.setText(viewModel.getSalaryNameById())

            if (viewModel.filledName) {
                binding.etRealName.isEnabled = false
                binding.tvRealName.visible()
            }
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
        SPUtil.getInstance(this).saveLoginInfoSwitch()
        //返回继续完成登录
        viewModel.loginResult?.let { result ->
            //返回继续完成登录
            EventBusUtil.post(RegisterInfoEvent(result))
            finish()
        }
    }


    private fun initView() {
        setTextColorGradient()
        initDateTimeView()
        initAddressPickerView()
        initCityPickerView()
        initSalaryPickerView()

        //选择生日点击
        binding.tvBirthday.setOnClickListener {
            hideSoftKeyboard(this)
            if (viewModel.filledBirthday) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            dateTimePicker?.show()
        }

        //选择省地址点击
        binding.tvAddress.setOnClickListener {
            hideSoftKeyboard(this)
            if (viewModel.filledProvince) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            provincePicker?.show()
        }

        binding.tvCity.setOnClickListener {
            hideSoftKeyboard(this)
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
            hideSoftKeyboard(this)
            if (viewModel.filledSalary) {
                ToastUtil.showToastInCenter(this, getString(R.string.N887))
                return@setOnClickListener
            }
            salaryPicker?.show()
        }

        //真实姓名输入
        binding.etRealName.addTextChangedListener {
            viewModel.realNameInput = binding.etRealName.text.toString()
            checkStatus()
        }

        binding.tvRealName.setOnClickListener {
            ToastUtil.showToastInCenter(this, getString(R.string.N887))
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

        binding.btnBack.setOnClickListener {
            finishPage()
        }


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

    }

    private fun initToolsBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
    }


    /**
     * 提交按钮高亮判断
     */
    private fun checkStatus() {
        val status = viewModel.checkInput()
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


    /**
     * 标题颜色渐变
     */
    private fun setTextColorGradient() {
        val width = binding.tvTitle.paint.measureText(binding.tvTitle.text.toString())
        val mLinearGradient = LinearGradient(
            0f, 0f, width, 0f,
            ContextCompat.getColor(this, R.color.color_71ADFF),
            ContextCompat.getColor(this, R.color.color_1971FD),
            Shader.TileMode.CLAMP
        )
        binding.tvTitle.paint.shader = mLinearGradient
        binding.tvTitle.invalidate()
    }


}