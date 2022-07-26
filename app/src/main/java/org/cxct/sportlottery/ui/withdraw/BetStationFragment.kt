package org.cxct.sportlottery.ui.withdraw

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_bank_card.btn_submit
import kotlinx.android.synthetic.main.fragment_bet_station.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemBetStationBinding
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.bettingStation.City
import org.cxct.sportlottery.network.bettingStation.Province
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.permission.GooglePermissionActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


/**
 * @app_destination 新增銀行卡
 */
class BetStationFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private var transferType: TransferType = TransferType.STATION
    private lateinit var stationAdapter: BetStationAdapter
    private var areaAll: AreaAll? = null
    private var selectProvince: Province? = null
    private var selectCity: City? = null
    private var selectBettingStation: BettingStation? = null
    private var location: Location? = null
    private var appointmentDate = ""
        set(value) {
            tv_calendar.text = value
            field = value
        }
    private var appointmentTime = ""
        set(value) {
            tv_hour.text = value
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bet_station, container, false).apply {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            checkPermissionGranted();
            initView()
            setupEvent()
            setupObserve()
        }
    }


    private fun initView() {
        et_amount.setTitle(sConfigData?.systemCurrency)
        et_amount.apply {
            clearIsShow = false
            getAllIsShow = true
            block_editText.setBackgroundResource(R.drawable.effect_edittext_bg_gray)
        }
        et_password.apply {
            tv_title.visibility = View.GONE
            v_divider.visibility = View.GONE
            block_editText.setBackgroundResource(R.drawable.effect_edittext_bg_gray)
        }
        et_password.setTitle(null)
        initEditTextStatus(et_amount)
        initEditTextStatus(et_password)
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
                    appointmentDate = ""
                    appointmentTime = ""
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
                        ) + " KM"
                    }
                })

        with(rv_station) {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(
                GridItemDecoration(
                    8.dp,
                    8.dp,
                    ContextCompat.getColor(context, R.color.color_191919_FCFCFC),
                    false
                )
            )
            adapter = stationAdapter
        }

    }

    private fun initEditTextStatus(setupView: LoginEditText) {
        setupView.apply {
            clearIsShow = getText().isNotEmpty()
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
        tv_calendar.setOnClickListener {
            showDatePicker()
        }
        tv_hour.setOnClickListener {
            showTimePicker()
        }
        lin_station_detail.setOnClickListener {
            selectBettingStation?.let {
                JumpUtil.toInternalWeb(
                    requireContext(),
                    "https://maps.google.com/?q=@" + it.lon + "," + it.lat,
                    getString(R.string.outlets_address),
                    true,
                    true
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
                appointmentDate,
                appointmentTime,
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
            tv_time_error.visibility = if (it.isBlank()) View.INVISIBLE else View.VISIBLE
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
        viewModel.queryArea()
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
    }

    private fun showDatePicker() {
        val dateTimePicker: TimePickerView = TimePickerBuilder(
            requireContext()
        ) { date, _ ->
            appointmentDate = TimeUtil.dateToDateFormat(date, TimeUtil.YMD_FORMAT) ?: ""
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(Calendar.getInstance(), null)
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setCancelText(" ")
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

    private fun showTimePicker() {
        var items = mutableListOf<StatusSheetData>()
        var startCal = Calendar.getInstance()
        var endCal = Calendar.getInstance()

        selectBettingStation?.let {
            if (it.officeStartTime.isNotEmpty() && it.officeEndTime.isNotEmpty()) {
                try {
                    startCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply {
                        time = SimpleDateFormat(TimeUtil.HM_FORMAT).parse(it.officeStartTime)
                    }.get(Calendar.HOUR_OF_DAY))
                    endCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply {
                        time = SimpleDateFormat(TimeUtil.HM_FORMAT).parse(it.officeEndTime)
                    }.get(Calendar.HOUR_OF_DAY))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
            }
        }

        for (i in startCal.get(Calendar.HOUR_OF_DAY)..endCal.get(Calendar.HOUR_OF_DAY)) {
            var cal = Calendar.getInstance()
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.HOUR_OF_DAY, i)
            var start = TimeUtil.dateToDateFormat(cal.time, TimeUtil.HM_FORMAT)
            cal.add(Calendar.HOUR_OF_DAY, 1)
            var end = TimeUtil.dateToDateFormat(cal.time, TimeUtil.HM_FORMAT)
            items.add(StatusSheetData(start, start + "~" + end))
        }
        showBottomSheetDialog(
            getString(R.string.select_time),
            items,
            items[0],
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                appointmentTime = data.showName!!
            })
    }


    private fun checkPermissionGranted() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(activity, GooglePermissionActivity::class.java))
        } else {
            val locationManager: LocationManager? =
                requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager?
            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
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
                tvName.text = station.name

                if (station.isSelected) {
                    selectedPosition = position
                    imgCheck.visibility = View.VISIBLE
                    ivBg.setImageResource(R.drawable.bg_betstation_sel)
                } else {
                    imgCheck.visibility = View.GONE
                    ivBg.setImageResource(R.drawable.bg_betstation_nor)
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
