package org.cxct.sportlottery.ui.money.withdraw

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.gms.location.*
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.databinding.FragmentBetStationBinding
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.bettingStation.City
import org.cxct.sportlottery.network.bettingStation.Province
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.LoginEditText
import java.math.RoundingMode
import java.util.*


/**
 * @app_destination 提款-网点预约
 */
class BetStationFragment : BaseFragment<WithdrawViewModel,FragmentBetStationBinding>() {
    
    private val stationAdapter by lazy { BetStationAdapter{
        requireActivity().hideSoftKeyboard()
        selectBettingStation = it
        binding.tvTime.text = getString(R.string.select_time)
        updateStation()
    }}
    private var areaAll: AreaAll? = null
    private var selectProvince: Province? = null
    private var selectCity: City? = null
    private var selectBettingStation: BettingStation? = null
    private var location: Location? = null
    private lateinit var dateTimePicker: TimePickerView
    private lateinit var dateTimePickerHMS: TimePickerView
    var depositDate = Date()
    var depositDate2 = Date()
    var mCalendar: Calendar =Calendar.getInstance()
    private var selectDate: Date? = null
        set(value) {
            binding.tvTime.text =
                (TimeUtil.dateToDateFormat(value, TimeUtil.YMD_HMS_FORMAT) ?: "") + "(GTM+8)"
            field = value
        }
    
    override fun onInitView(view: View) {
        checkPermissionGranted();
        initView()
        setupObserve()
        setupEvent()
        setupServiceButton()
        initTimePickerForYMD()
        initTimePickerForHMS()
    }

    //联系客服
    private fun setupServiceButton() {
        binding.tvServiceShow.setServiceClick(childFragmentManager)
    }

    private fun initView() =binding.run{
        etAmount.apply {
            clearIsShow = false
            getAllIsShow = true
        }

        spinnerArea.setBetStationStyle()
        spinnerCity.setBetStationStyle()

        initEditTextStatus(etAmount)
        initEditTextStatus(etPassword)
        View.OnClickListener { requireActivity().hideSoftKeyboard() }.let {
            spinnerArea.setOnClickListener(it)
            spinnerCity.setOnClickListener(it)
          //  tv_time.setOnClickListener(it)
            txvWithdrawalTime.setOnClickListener(it)
            txvWithdrawalTime2.setOnClickListener(it)
        }
        spinnerArea.setOnItemSelectedListener {
            selectProvince = areaAll?.provinces?.find { province ->
                TextUtils.equals(it.code, province.id.toString())
            }
            setCity(selectProvince)
        }
        spinnerCity.setOnItemSelectedListener {
            selectCity = areaAll?.cities?.find { city ->
                TextUtils.equals(it.code, city.id.toString())
            }
            selectArea()
        }

        btnSubmit.setTitleLetterSpacing()
        with(rvStation) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = stationAdapter
        }

        tvDetail.setOnClickListener {
            startActivity(Intent(activity, WithdrawCommissionDetailActivity::class.java))
        }

        btnInfo.setOnClickListener {
            CommissionInfoDialog().show(childFragmentManager, null)
        }

     //   tv_time.text = TimeUtil.dateToDateFormat(selectDate, TimeUtil.YMD_HMS_FORMAT) ?: ""
        //存款时间年月日
        txvWithdrawalTime.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
        //存款时间时分秒
        txvWithdrawalTime2.text = TimeUtil.dateToStringFormatHMS(Date())
        spinnerArea.setNameGravity(Gravity.CENTER_VERTICAL)
        spinnerCity.setNameGravity(Gravity.CENTER_VERTICAL)
    }

    private fun initEditTextStatus(setupView: LoginEditText) {
        setupView.apply {
            clearIsShow = getText().isNotEmpty()
        }
    }

    private fun updateStation() =binding.run{
        selectBettingStation.let {
            if (it == null) {
                linStation.visibility = View.GONE
                linEmpty.visibility = View.VISIBLE
            } else {
                linStationDetail.visibility = View.VISIBLE
                linEmpty.visibility = View.GONE
                binding.tvStationName.text = it.name
                binding.tvStationAddress.text = it.addr
                var desloc = Location("").apply {
                    latitude = it.lat
                    longitude = it.lon
                }
                var distance = location?.distanceTo(desloc)
                tvStationDistance.text = ArithUtil.round(
                    distance?.div(1000)?.toDouble() ?: 0.0,
                    2,
                    RoundingMode.HALF_UP
                ) + getString(R.string.km)
            }
        }
    }

    private fun setupEvent() {
        setupClickEvent()
        setupTextChangeEvent()
    }

    private fun setCity(province: Province?) {
        var cityList = mutableListOf<StatusSheetData>()
        var citys = areaAll?.cities?.filter {
            it.provinceId == province?.id
        }
        citys?.let {
            for (i in it) {
                if (i.provinceId == province?.id) {
                    cityList.add(StatusSheetData(i.id.toString(), i.name))
                }
            }
        }
        binding.spinnerCity.setItemData(cityList)
        selectCity = citys?.get(0)
        selectArea()
    }

    private fun selectArea() {
        selectCity?.let {
            viewModel.bettingStationQuery(
                sConfigData?.platformId!!,
                it.countryId,
                it.provinceId,
                it.id
            )
        }
    }


    private fun setupTextChangeEvent()=binding.run {
        viewModel.apply {

            etAmount.afterTextChanged { checkWithdrawAmount(null, it) }
            //銀行卡號
            setupClearButtonVisibility(etAmount) { checkWithdrawAmount(null, it) }
            //提款密碼
            setupClearButtonVisibility(etPassword) { checkNetWorkPoint(it) }

        }
    }

    private fun setupClearButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkFun.invoke(setupView.getText())
        }
    }


    private fun setupEyeButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkFun(setupView.getText())
        }
    }


    private fun setupClickEvent() =binding.run{
        /* tv_time.setOnClickListener {
             showDatePicker()
         }*/
        //年月日的时间选择器
        llWithdrawalTime.setOnClickListener {
            dateTimePicker.show()
        }
        txvWithdrawalTime.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        llWithdrawalTime2.setOnClickListener {
            dateTimePickerHMS.show()
        }
        txvWithdrawalTime2.setOnClickListener {
            dateTimePickerHMS.show()
        }
        linStationDetail.setOnClickListener {
            selectBettingStation?.let {
                JumpUtil.toInternalWeb(
                    requireContext(),
                    "https://maps.google.com/?q=@" + it.lat + "," + it.lon,
                    getString(R.string.outlets_address),
                    true,
                    true,
                    it
                )
            }
        }
        btnSubmit.setOnClickListener {
            modifyFinish()
            if(sConfigData?.auditFailureRestrictsWithdrawalsSwitch==1&&(viewModel.uwCheckData?.total?.unFinishValidAmount?:0.0)>0){
                showPromptDialog(getString(R.string.P150),getString(R.string.P149,"${sConfigData?.systemCurrencySign}${(viewModel.uwCheckData?.total?.unFinishValidAmount?:0).toInt()}")){}
                return@setOnClickListener
            }
            viewModel.showCheckDeductMoneyDialog {
                addWithdraw()
            }?.show(childFragmentManager)
        }
    }


    private fun setupObserve() {
        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }
        //提款金額提示訊息
        viewModel.withdrawAmountHint.observe(this) {
            binding.etAmount.setHint(it)
        }
        //提款手續費提示
        viewModel.withdrawRateHint.observe(this) {
            binding.tvTipsHandlingFee.text = it
        }
        viewModel.withdrawAmountMsg.observe(this) {
            binding.etAmount.setError(it ?: "")
        }

        binding.etAmount.getAllButton {
            it.setText(viewModel.getWithdrawAmountLimit().max.toLong().toString())
            binding.etAmount.setSelection()
        }
        //提款密碼
        viewModel.withdrawPasswordMsg.observe(this) {
            binding. etPassword.setError(it ?: "")
        }
        viewModel.WithdrawAppointmentMsg.observe(this) {
            binding.tvTimeError.text = it ?: ""
            binding.tvTimeError.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
        }
        viewModel.userMoney.observe(this) {
            binding.tvBalance.text = sConfigData?.systemCurrency + " " + TextUtil.format(
                ArithUtil.toMoneyFormat(it).toDouble()
            )
            binding.tvCurrentTime.text = TimeUtil.dateToFormat(Date())
        }
        viewModel.areaList.observe(this) {
            areaAll = it
            var provinceList = mutableListOf<StatusSheetData>()
            for (i in it.provinces) {
                provinceList.add(StatusSheetData(i.id.toString(), i.name))
            }
            binding.spinnerArea.setItemData(provinceList)
            setCity(it.provinces[0])
            selectArea()
        }
        viewModel.bettingStationList.observe(this) {
            if (it.isNotEmpty()) {
                binding.linStation.visibility = View.VISIBLE
                binding.linEmpty.visibility = View.GONE
            } else {
                binding.linStation.visibility = View.GONE
                binding.linEmpty.visibility = View.VISIBLE
            }
            stationAdapter.setData(it)
        }
        //提款
        viewModel.withdrawAddResult.observe(this, Observer {
            if (it.success) {
                clearEvent()
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.submit_success)
                ) { viewModel.getMoneyAndTransferOut() }
            } else {
                //流水不达标提醒
                if (it.code == 2280){
                    showPromptDialog(getString(R.string.P150), it.msg) {}
                }else{
                    showErrorPromptDialog(getString(R.string.prompt), it.msg) {}
                }
            }
        })

        viewModel.needCheck.observe(this) {
            binding.llCommission.visibility = if (it) View.VISIBLE else View.GONE
            binding.rvStation.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.commissionCheckList.observe(this) {
            binding.tvDetail.apply {
                isEnabled = it.isNotEmpty()
                isSelected = it.isNotEmpty()
            }
        }

        viewModel.deductMoney.observe(this) {
            val zero = 0.0
            binding.tvCommission.apply {
                text = if (it.isNaN()) "0" else TextUtil.formatMoney(zero.minus(it ?: 0.0))
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (zero.minus(
                                it ?: 0.0
                            ) > 0
                        ) R.color.color_08dc6e_08dc6e else R.color.color_E44438_e44438
                    )
                )
            }
        }

        viewModel.queryArea()

        viewModel.getUwCheck()
    }

    fun addWithdraw(){
        viewModel.addWithdraw(
            null,
            viewModel.getChannelMode(),
            binding.etAmount.getText(),
            binding.etPassword.getText(),
            if (selectBettingStation == null) null else selectBettingStation!!.id,
            TimeUtil.dateToDateFormat(
                mCalendar.time,
                TimeUtil.YMD_FORMAT
            ) ?: "",
            TimeUtil.dateToDateFormat(
                mCalendar.time,
                TimeUtil.HM_FORMAT_SS
            ) ?: "",
        )
    }
    private fun clearEvent() {
        binding.etAmount.setText("")
        binding.etPassword.setText("")
        viewModel.resetWithdrawPage()
        modifyFinish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearBankCardFragmentStatus()
        removeLocationUpdates()
    }

    private fun showDatePicker() {
        val dateTimePicker: TimePickerView = TimePickerBuilder(
            requireContext()
        ) { date, _ ->
            selectDate = date
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(Calendar.getInstance(), null)
            .setDate(Calendar.getInstance().apply {
                selectDate?.let {
                    time = selectDate
                }
            })
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, true, true, false))
            .setCancelText("")
            .setSubmitText(requireContext().getString(R.string.picker_submit))
            .setTitleColor(ContextCompat.getColor(requireContext(), R.color.color_CCCCCC_000000))
            .setTitleBgColor(ContextCompat.getColor(requireContext(), R.color.color_2B2B2B_e2e2e2))
            .setBgColor(ContextCompat.getColor(requireContext(), R.color.color_191919_FCFCFC))
            .setSubmitColor(ContextCompat.getColor(requireContext(), R.color.color_7F7F7F_999999))
            .setCancelColor(ContextCompat.getColor(requireContext(), R.color.color_7F7F7F_999999))
            .isDialog(false)
            .build() as TimePickerView
        dateTimePicker.show()
    }

//    private fun showTimePicker() {
//        var items = mutableListOf<StatusSheetData>()
//        var startCal = Calendar.getInstance()
//        var endCal = Calendar.getInstance()
//
//        selectBettingStation?.let {
//            if (it.officeStartTime.isNotEmpty() && it.officeEndTime.isNotEmpty()) {
//                try {
//                    startCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply {
//                        time = SimpleDateFormat(TimeUtil.HM_FORMAT_SS).parse(it.officeStartTime)
//                    }.get(Calendar.HOUR_OF_DAY))
//                    endCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply {
//                        time = SimpleDateFormat(TimeUtil.HM_FORMAT_SS).parse(it.officeEndTime)
//                    }.get(Calendar.HOUR_OF_DAY))
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            } else {
//                startCal.set(Calendar.HOUR_OF_DAY, 0)
//                endCal.set(Calendar.HOUR_OF_DAY, 23)
//            }
//
//            for (i in startCal.get(Calendar.HOUR_OF_DAY)..endCal.get(Calendar.HOUR_OF_DAY)) {
//                var cal = Calendar.getInstance()
//                cal.set(Calendar.MINUTE, 0)
//                cal.set(Calendar.HOUR_OF_DAY, i)
//                var start = TimeUtil.dateToDateFormat(cal.time, TimeUtil.HM_FORMAT_SS)
//                cal.add(Calendar.HOUR_OF_DAY, 1)
//                var end = TimeUtil.dateToDateFormat(cal.time, TimeUtil.HM_FORMAT_SS)
//                items.add(StatusSheetData(start, "$start~$end"))
//            }
//            showBottomSheetDialog(
//                getString(R.string.select_time),
//                items,
//                items[0],
//                StatusSheetAdapter.ItemCheckedListener { _, data ->
//                    appointmentTime = data.showName!!
//                })
//        }
//    }

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            location = p0.lastLocation
            updateStation()
            if (location != null) removeLocationUpdates()
        }
    }

    private fun removeLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("CheckResult", "MissingPermission")
    private fun checkPermissionGranted() {
        RxPermissions(requireActivity())
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe { aBoolean ->
                if (aBoolean) {
                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    fusedLocationClient?.lastLocation?.addOnSuccessListener {
                        location = it
                        updateStation()

                        //拿不到lastLocation時，要利用requestLocationUpdates取得location
                        if (location == null) {
                            val locationRequest = LocationRequest.create().apply {
                                interval = 10000
                                fastestInterval = 5000
                                priority = Priority.PRIORITY_HIGH_ACCURACY
                            }
                            fusedLocationClient?.requestLocationUpdates(
                                locationRequest, locationCallback, Looper.myLooper()
                            )
                        }
                    }
                } else {
                    ToastUtil.showToast(
                        requireContext(),
                        getString(R.string.allow_location_permission)
                    )
                }
            }
    }

    private fun initTimePickerForYMD() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -30)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, +30)
        dateTimePicker = TimePickerBuilder(activity) { date, _ ->
            try {
                depositDate = date
                binding.txvWithdrawalTime.text = TimeUtil.dateToStringFormatYMD(date)
                upDataTimeYMD()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(yesterday, tomorrow)
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setTitleText(resources.getString(R.string.select_date))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(
                ContextCompat.getColor(
                    binding.txvWithdrawalTime.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    binding.txvWithdrawalTime.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }
    private fun initTimePickerForHMS() {

        dateTimePickerHMS = TimePickerBuilder(activity) { date, _ ->
            try {
                depositDate2 = date
                binding.txvWithdrawalTime2.text = TimeUtil.dateToStringFormatHMS(date)
                upDataTimeHMS()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setTitleText(resources.getString(R.string.select_time))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(
                ContextCompat.getColor(
                    binding.txvWithdrawalTime2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    binding.txvWithdrawalTime2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
            .build() as TimePickerView
    }

    private fun upDataTimeHMS(){
        mCalendar.apply {
            var despsitCal=Calendar.getInstance().apply {
                time=depositDate2
            }
            mCalendar.set(Calendar.HOUR_OF_DAY,despsitCal.get(Calendar.HOUR_OF_DAY))
            mCalendar.set(Calendar.MINUTE,despsitCal.get(Calendar.MINUTE))
            mCalendar.set(Calendar.SECOND,despsitCal.get(Calendar.SECOND))

        }
    }
    private fun upDataTimeYMD(){
        mCalendar.apply {
            var despsitCal=Calendar.getInstance().apply {
                time=depositDate
            }
            mCalendar.set(Calendar.DAY_OF_MONTH,despsitCal.get(Calendar.DAY_OF_MONTH))
            mCalendar.set(Calendar.MONTH,despsitCal.get(Calendar.MONTH))
            mCalendar.set(Calendar.YEAR,despsitCal.get(Calendar.YEAR))

        }
    }
    

}

