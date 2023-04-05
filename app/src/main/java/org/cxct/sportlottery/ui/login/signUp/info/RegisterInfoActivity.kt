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
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.TimeUtil
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


    private fun initData() {
        viewModel.loginData = intent.getSerializableExtra("data") as LoginData?
        viewModel.getAddressData()
        viewModel.getUserSalaryList()
        //地址数据
        viewModel.areaAllList.observe(this) {
            val provinceList = viewModel.getProvinceStringList()
            val cityList = viewModel.getCityStringList(provinceList)
            addressPicker?.setPicker(provinceList, cityList)
        }
        //薪资来源
        viewModel.salaryList.observe(this) {
            salaryPicker?.setPicker(viewModel.salaryStringList)
        }
    }


    private fun initView() {
        setTextColorGradient()
        initDateTimeView()
        initAddressPickerView()
        initSalaryPickerView()

        binding.tvBirthday.setOnClickListener {
            hideSoftKeyboard(this)
            dateTimePicker?.show()
        }

        binding.tvAddress.setOnClickListener {
            hideSoftKeyboard(this)
            addressPicker?.show()
        }

        binding.tvSalary.setOnClickListener {
            hideSoftKeyboard(this)
            salaryPicker?.show()
        }

        binding.etRealName.addTextChangedListener {
            viewModel.realNameInput = binding.etRealName.text.toString()
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


    override fun onPause() {
        super.onPause()
        viewModel.logOut()
    }

    override fun onBackPressed() {
    }


    /**
     * 提交按钮高亮判断
     */
    private fun checkStatus() {
        val status = viewModel.realNameInput.isNotEmpty()
                && viewModel.birthdayTimeInput .isNotEmpty()
                && viewModel.sourceInput>0
                && viewModel.provinceInput.isNotEmpty()
                && viewModel.cityInput.isNotEmpty()
        binding.btnCommit.isEnabled = status
        if (status) {
            binding.btnCommit.alpha = 1f
        } else {
            binding.btnCommit.alpha = 0.5f
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initAddressPickerView() {
        addressPicker = StringPickerOptions(this)
            .getBuilder { provincePosition, cityPosition, _, _ ->
                val provinceList = viewModel.getProvinceStringList()
                val cityList = viewModel.getCityStringList(provinceList)
                viewModel.provinceInput =provinceList[provincePosition]
                viewModel.cityInput=cityList[provincePosition][cityPosition]
                binding.etAddress.setText("${viewModel.provinceInput} ${viewModel.cityInput}")
                checkStatus()
            }
            .setTitleText(resources.getString(R.string.select_area))
            .setSubmitText(getString(R.string.picker_submit))
            .build()
    }


    private fun initSalaryPickerView() {
        salaryPicker = StringPickerOptions(this)
            .getBuilder { options1, _, _, _ ->
                viewModel.salaryList.value?.let {
                    it[options1].let { salary->
                        viewModel.sourceInput=salary.id
                        binding.etSource.setText(viewModel.salaryList.value?.get(options1)?.name)
                        checkStatus()
                    }
                }

            }
            .setTitleText(resources.getString(R.string.select_area))
            .setSubmitText(getString(R.string.picker_submit))
            .build()
    }

    private fun initDateTimeView() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -100)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, -1)
        dateTimePicker = DateTimePickerOptions(this).getBuilder { date, _ ->
            TimeUtil.dateToStringFormatYMD(date)?.let {
                viewModel.birthdayTimeInput=it
                binding.etBirthday.setText(it)
                checkStatus()
            }
        }
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .build()
    }


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