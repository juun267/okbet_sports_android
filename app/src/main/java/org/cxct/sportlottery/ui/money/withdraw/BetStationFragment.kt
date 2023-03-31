package org.cxct.sportlottery.ui.money.withdraw

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.gms.location.*
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_bank_card.btn_submit
import kotlinx.android.synthetic.main.fragment_bet_station.*
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemBetStationBinding
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.bettingStation.City
import org.cxct.sportlottery.network.bettingStation.Province
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.*
import java.math.RoundingMode
import java.util.*


/**
 * @app_destination 提款-网点预约
 */
class BetStationFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private var transferType: TransferType = TransferType.STATION
    private lateinit var stationAdapter: BetStationAdapter
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
            tv_time.text =
                (TimeUtil.dateToDateFormat(value, TimeUtil.YMD_HMS_FORMAT) ?: "") + "(GTM+8)"
            field = value
        }

    override fun layoutId() = R.layout.fragment_bet_station

    override fun onBindView(view: View) {
        checkPermissionGranted();
        initView()
        setupEvent()
        setupObserve()
        setupServiceButton()
        initTimePickerForYMD()
        initTimePickerForHMS()
    }

    //联系客服
    private fun setupServiceButton() {
        tv_service_show.setServiceClick(childFragmentManager)
    }

    private fun initView() {
        et_amount.apply {
            clearIsShow = false
            getAllIsShow = true
        }

        spinner_area.setBetStationStyle()
        spinner_city.setBetStationStyle()

        initEditTextStatus(et_amount)
        initEditTextStatus(et_password)
        View.OnClickListener { hideKeyboard() }.let {
            spinner_area.setOnClickListener(it)
            spinner_city.setOnClickListener(it)
          //  tv_time.setOnClickListener(it)
            txv_withdrawal_time.setOnClickListener(it)
            txv_withdrawal_time2.setOnClickListener(it)
        }
        spinner_area.setOnItemSelectedListener {
            selectProvince = areaAll?.provinces?.find { province ->
                TextUtils.equals(it.code, province.id.toString())
            }
            setCity(selectProvince)
        }
        spinner_city.setOnItemSelectedListener {
            selectCity = areaAll?.cities?.find { city ->
                TextUtils.equals(it.code, city.id.toString())
            }
            selectArea()
        }

        btn_submit.setTitleLetterSpacing()
        stationAdapter =
            BetStationAdapter(
                requireContext(),
                BetStationSelectorAdapterListener {
                    hideKeyboard()
                    selectBettingStation = it
                    tv_time.text = getString(R.string.select_time)
                    updateStation()
                })

        with(rv_station) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = stationAdapter
        }

        tv_detail.setOnClickListener {
            startActivity(Intent(activity, WithdrawCommissionDetailActivity::class.java))
        }

        btn_info.setOnClickListener {
            CommissionInfoDialog().show(childFragmentManager, null)
        }

     //   tv_time.text = TimeUtil.dateToDateFormat(selectDate, TimeUtil.YMD_HMS_FORMAT) ?: ""
        //存款时间年月日
        txv_withdrawal_time.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
        //存款时间时分秒
        txv_withdrawal_time2.text = TimeUtil.dateToStringFormatHMS(Date())
        spinner_area.tv_name.gravity = Gravity.CENTER_VERTICAL
        spinner_city.tv_name.gravity = Gravity.CENTER_VERTICAL
    }

    private fun initEditTextStatus(setupView: LoginEditText) {
        setupView.apply {
            clearIsShow = getText().isNotEmpty()
        }
    }

    private fun updateStation() {
        selectBettingStation.let {
            if (it == null) {
                lin_station.visibility = View.GONE
                lin_empty.visibility = View.VISIBLE
            } else {
                lin_station_detail.visibility = View.VISIBLE
                lin_empty.visibility = View.GONE
                tv_station_name.text = it.name
                tv_station_address.text = it.addr
                var desloc = Location("").apply {
                    latitude = it.lat
                    longitude = it.lon
                }
                var distance = location?.distanceTo(desloc)
                tv_station_distance.text = ArithUtil.round(
                    distance?.div(1000)?.toDouble(),
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
        spinner_city.setItemData(cityList)
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


    private fun setupTextChangeEvent() {
        viewModel.apply {

            et_amount.afterTextChanged { checkWithdrawAmount(null, it) }
            //銀行卡號
            setupClearButtonVisibility(et_amount) { checkWithdrawAmount(null, it) }
            //提款密碼
            setupClearButtonVisibility(et_password) { checkNetWorkPoint(it) }

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


    private fun setupClickEvent() {
        /* tv_time.setOnClickListener {
             showDatePicker()
         }*/
        //年月日的时间选择器
        ll_withdrawal_time.setOnClickListener {
            dateTimePicker.show()
        }
        txv_withdrawal_time.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        ll_withdrawal_time2.setOnClickListener {
            dateTimePickerHMS.show()
        }
        txv_withdrawal_time2.setOnClickListener {
            dateTimePickerHMS.show()
        }
        lin_station_detail.setOnClickListener {
            selectBettingStation?.let {
                JumpUtil.toInternalWeb(
                    requireContext(),
                    "https://maps.google.com/?q=@" + it.lon + "," + it.lat,
                    getString(R.string.outlets_address),
                    true,
                    true,
                    it
                )
            }
        }
        btn_submit.setOnClickListener {
            modifyFinish()
            viewModel.addWithdraw(
                null,
                et_amount.getText(),
                et_password.getText(),
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
    }


    private fun setupObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })
        //提款金額提示訊息
        viewModel.withdrawAmountHint.observe(this.viewLifecycleOwner, Observer {
            et_amount.et_input.hint = it
        })
        //提款手續費提示
        viewModel.withdrawRateHint.observe(this.viewLifecycleOwner) {
            tv_tips_handling_fee.text = it
        }
        viewModel.withdrawAmountMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_amount.setError(it ?: "")
        }

        et_amount.getAllButton {
            it.setText(viewModel.getWithdrawAmountLimit().max.toLong().toString())
            et_amount.et_input.apply { setSelection(this.length()) }
        }
        //提款密碼
        viewModel.withdrawPasswordMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_password.setError(it ?: "")
        }
        viewModel.WithdrawAppointmentMsg.observe(
            this.viewLifecycleOwner
        ) {
            tv_time_error.text = it ?: ""
            tv_time_error.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
        }
        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            tv_balance.text = sConfigData?.systemCurrency + " " + TextUtil.format(
                ArithUtil.toMoneyFormat(it).toDouble()
            )
            tv_current_time.text = TimeUtil.dateToFormat(Date())
        })
        viewModel.areaList.observe(this.viewLifecycleOwner) {
            areaAll = it
            var provinceList = mutableListOf<StatusSheetData>()
            for (i in it.provinces) {
                provinceList.add(StatusSheetData(i.id.toString(), i.name))
            }
            spinner_area.setItemData(provinceList)
            setCity(it.provinces[0])
            selectArea()
        }
        viewModel.bettingStationList.observe(this.viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                lin_station.visibility = View.VISIBLE
                lin_empty.visibility = View.GONE
            } else {
                lin_station.visibility = View.GONE
                lin_empty.visibility = View.VISIBLE
            }
            stationAdapter.setData(it)
        }
        //提款
        viewModel.withdrawAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                clearEvent()
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.submit_success)
                ) { viewModel.getMoney() }
            } else {
                showErrorPromptDialog(getString(R.string.prompt), it.msg) {}
            }
        })

        viewModel.needCheck.observe(this.viewLifecycleOwner) {
            ll_commission.visibility = if (it) View.VISIBLE else View.GONE
            rv_station.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.commissionCheckList.observe(this.viewLifecycleOwner) {
            tv_detail.apply {
                isEnabled = it.isNotEmpty()
                isSelected = it.isNotEmpty()
            }
        }

        viewModel.deductMoney.observe(this.viewLifecycleOwner) {
            val zero = 0.0
            tv_commission.apply {
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

    private fun clearEvent() {
        et_amount.setText("")
        et_password.setText("")
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
                txv_withdrawal_time.text = TimeUtil.dateToStringFormatYMD(date)
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
                    txv_withdrawal_time.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    txv_withdrawal_time.context,
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
                txv_withdrawal_time2.text = TimeUtil.dateToStringFormatHMS(date)
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
                    txv_withdrawal_time2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    txv_withdrawal_time2.context,
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

class BetStationAdapter(
    private val context: Context,
    private val listener: BetStationSelectorAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPosition = 0
    private var stationList: List<BettingStation> = listOf()

    fun setData(newData: List<BettingStation>) {
        stationList = newData
        selectedPosition = 0
        if (stationList.isNotEmpty()) {
            stationList[selectedPosition].isSelected = true
            listener.onSelect(stationList[0])
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BetStationItemViewHolder(
            ItemBetStationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is BetStationItemViewHolder -> {
            holder.bind(stationList[position], position)
        }
        else -> {
            //do nothing
        }
    }

    override fun getItemCount(): Int = stationList.size

    inner class BetStationItemViewHolder(val binding: ItemBetStationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(station: BettingStation, position: Int) {
            with(binding) {
                root.setOnClickListener {
                    selectBank(position)
                    listener.onSelect(station)
                }
                tvName.isSelected =  selectedPosition == position
                tvNameNum.isSelected =  selectedPosition == position

                tvName.text = station.name
                tvNameNum.text = itemView.context.getString(R.string.outlet)+"${position+1}"
                if (station.isSelected) {
                    selectedPosition = position
                    imgCheck.visibility = View.VISIBLE
                    llSelectBankCard.setBackgroundResource(R.drawable.ic_bule_site)
                } else {
                    imgCheck.visibility = View.GONE
                    llSelectBankCard.setBackgroundResource(R.drawable.ic_white_site)

                }
            }
        }

        private fun selectBank(bankPosition: Int) {
            stationList[selectedPosition].isSelected = false
            notifyItemChanged(selectedPosition)
            selectedPosition = bankPosition
            stationList[bankPosition].isSelected = true
            notifyItemChanged(bankPosition)
        }
    }
}

class BetStationSelectorAdapterListener(private val selectListener: (item: BettingStation?) -> Unit) {
    fun onSelect(item: BettingStation?) = selectListener(item)
}
