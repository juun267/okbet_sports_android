package org.cxct.sportlottery.ui.login.signUp.info

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
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.EventBusUtil
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

    //地址选择器
    private var addressPicker: OptionsPickerView<String>? = null

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
            val provinceList = viewModel.getProvinceStringList()
            val cityList = viewModel.getCityStringList(provinceList)
            addressPicker?.setPicker(provinceList, cityList)
        }
        //薪资来源
        viewModel.salaryList.observe(this) {
            salaryPicker?.setPicker(viewModel.salaryStringList)

            //获取用户基础信息
            viewModel.getUserBasicInfo()
        }

        //基本信息
        viewModel.userBasicInfoEvent.observe(this){
            if(viewModel.provinceInput.isNotEmpty()){
                binding.etAddress.setText("${viewModel.provinceInput} ${viewModel.cityInput}")
            }
            binding.etRealName.setText(viewModel.realNameInput)
            binding.etBirthday.setText(viewModel.birthdayTimeInput)
            binding.etSource.setText(viewModel.getSalaryNameById())
        }

        //提交表单
        viewModel.commitEvent.observe(this) {
            hideLoading()
            if(it){
                viewModel.loginResult?.let { result ->
                    //返回继续完成登录
                    EventBusUtil.post(RegisterInfoEvent(result))
                    finish()
                }
            }else{
                ToastUtil.showToast(this,viewModel.commitMsg)
            }
        }
    }


    private fun initView() {
        setTextColorGradient()
        initDateTimeView()
        initAddressPickerView()
        initSalaryPickerView()

        //选择生日点击
        binding.tvBirthday.setOnClickListener {
            hideSoftKeyboard(this)
            dateTimePicker?.show()

        }

        //选择地址点击
        binding.tvAddress.setOnClickListener {
            hideSoftKeyboard(this)
            addressPicker?.show()
        }

        //选择薪资来源点击
        binding.tvSalary.setOnClickListener {
            hideSoftKeyboard(this)
            salaryPicker?.show()
        }

        //真实姓名输入
        binding.etRealName.addTextChangedListener {
            viewModel.realNameInput = binding.etRealName.text.toString()
            checkStatus()
        }

        //提交点击
        binding.btnCommit.setOnClickListener {
            loading()
            viewModel.commitUserBasicInfo()
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


    override fun onStart() {
        super.onStart()
        //回到页面，恢复登录信息
        viewModel.restored()
    }
    override fun onStop() {
        super.onStop()
        //未完善退出  注销登录信息
        viewModel.logout()
    }

    override fun onBackPressed() {
        //需求不让回退
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
        addressPicker = StringPickerOptions(this)
            .getBuilder { provincePosition, cityPosition, _, _ ->
                viewModel.setProvinceData(provincePosition)
                viewModel.setCityData(provincePosition, cityPosition)
                binding.etAddress.setText("${viewModel.provinceInput} ${viewModel.cityInput}")
                checkStatus()
            }
            .setTitleText(resources.getString(R.string.select_area))
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
                        binding.etSource.setText(viewModel.salaryList.value?.get(options1)?.name)
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