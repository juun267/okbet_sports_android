package org.cxct.sportlottery.ui.login.signUp.info

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
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
        val temp = intent.getSerializableExtra("data")

        viewModel.getAddressData()
        viewModel.getConfig()
        //地址数据
        viewModel.areaAllList.observe(this){
            val provinceList=viewModel.getProvinceStringList()
            val cityList=viewModel.getCityStringList(provinceList)
            addressPicker?.setPicker(provinceList,cityList)
        }

        viewModel.salaryList.observe(this){
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
            viewModel.realNameInput=binding.etRealName.text.toString()
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



    fun checkStatus(){
        val status= viewModel.realNameInput.isNotEmpty()
                &&viewModel.birthdayTimeInput>0
                && viewModel.sourceInput.isNotEmpty()
                && viewModel.addressInput.isNotEmpty()
        binding.btnCommit.isEnabled=status
        if(status){
            binding.btnCommit.alpha=1f
        }else{
            binding.btnCommit.alpha=0.5f
        }
    }

    private fun initAddressPickerView() {
        addressPicker = OptionsPickerBuilder(this) { provincePosition, cityPosition, _, _ ->
            val provinceList=viewModel.getProvinceStringList()
            val cityList=viewModel.getCityStringList(provinceList)
            viewModel.addressInput = "${provinceList[provincePosition]} ${cityList[provincePosition][cityPosition]}"
            binding.etAddress.setText(viewModel.addressInput )
            checkStatus()
        }
            .setTitleText(resources.getString(R.string.select_area))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setTitleColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build()
    }


    private  fun initSalaryPickerView(){
        salaryPicker = OptionsPickerBuilder(this) { options1, _, _, _ ->
            viewModel.sourceInput = "${viewModel.salaryList.value?.get(options1)?.name} "
            binding.etSource.setText(viewModel.sourceInput )
            checkStatus()
        }
            .setTitleText(resources.getString(R.string.income_source))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setTitleColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build()
    }

    private fun initDateTimeView() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.YEAR, -92)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, -1)
        dateTimePicker = TimePickerBuilder(this) { date, _ ->
            viewModel.birthdayTimeInput = date.time
            binding.etBirthday.setText(TimeUtil.dateToStringFormatYMD(date))
            checkStatus()
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(yesterday, tomorrow)
            .setDate(tomorrow)
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setTitleText(resources.getString(R.string.select_date))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setTitleColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }


    private fun setTextColorGradient() {
        val width=binding.tvTitle.paint.measureText(binding.tvTitle.text.toString())
        val mLinearGradient = LinearGradient(0f, 0f, width,0f,
            ContextCompat.getColor(this,R.color.color_71ADFF),
            ContextCompat.getColor(this,R.color.color_1971FD),
            Shader.TileMode.CLAMP);
        binding.tvTitle.paint.shader = mLinearGradient
        binding.tvTitle.invalidate()
    }


}