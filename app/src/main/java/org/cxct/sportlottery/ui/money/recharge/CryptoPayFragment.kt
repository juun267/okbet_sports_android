package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.crypto_pay_fragment.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.abs


class CryptoPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    lateinit var calendarBottomSheet: BottomSheetDialog

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    //幣種
    private lateinit var currencyBottomSheet: BottomSheetDialog
    private val mCurrencyBottomSheetList = mutableListOf<CustomImageAdapter.SelectBank>()
    private lateinit var currencyBtsAdapter: BankBtsAdapter

    //充值帳戶
    private lateinit var accountBottomSheet: BottomSheetDialog
    private val mAccountBottomSheetList = mutableListOf<CustomImageAdapter.SelectBank>()
    private lateinit var accountBtsAdapter: BankBtsAdapter
    private val mapOfAccount = hashMapOf<String?, List<String?>>()

    private var rechCfgsList = mutableListOf<MoneyRechCfg.RechConfig>()

    private var currencyPosition = 0

    private var voucherUrl:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.crypto_pay_fragment, container, false)
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): CryptoPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBottomSheet()
        initView()
        initButton()
        initObserve()
        setPayBankBottomSheet()
    }

    private fun initButton() {
        //提交
        btn_submit.setOnClickListener {
            createMoneyAddRequest()?.let {
                viewModel.rechargeCryptoSubmit(
                    it,
                    mMoneyPayWay?.rechType,
                    mSelectRechCfgs
                )
            }
        }
        //選取日曆
        cv_recharge_time.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.start_date)
            calendarBottomSheet.show()
        }
        //選擇幣種
        cv_currency.setOnClickListener {
            currencyBottomSheet.show()
        }
        //充值帳號
        cv_account.setOnClickListener {
            accountBottomSheet.show()
        }
        //複製
        btn_qr_copy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, txv_payee.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }

        //上傳照片
        cv_upload.setOnClickListener {
            this.activity?.let { activity -> fragmentManager?.let { fragmentManager ->
                RechargePicSelectorDialog(activity, mSelectMediaListener).show(
                    fragmentManager, null)
            } }
        }

        //去充值
        btn_qr_recharge.setOnClickListener {
            if (!mSelectRechCfgs?.payUrl.isNullOrEmpty()) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(mSelectRechCfgs?.payUrl ?: "")
                )
                startActivity(browserIntent)
            }
        }
    }

    private fun initView() {
        setupTextChangeEvent()
        setupFocusEvent()
        calendarBottomSheet()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(0)

        tv_recharge_money.text = String.format(resources.getString(R.string.txv_recharge_money), "0")
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner, {
            et_recharge_account.setError(it)
        })

        //區塊鏈交易ID訊息
        viewModel.hashCodeErrorMsg.observe(viewLifecycleOwner, {
            et_transaction_id.setError(it)
        })

        viewModel.apiResult.observe(viewLifecycleOwner, {
            if (it.success) {
                resetEvent()
            }
        })

        viewModel.voucherUrlResult.observe(viewLifecycleOwner,{
            Glide.with(this).load(it).into(img_screen_shot)
            ic_screen_shot.visibility =View.GONE
            img_screen_shot.visibility=View.VISIBLE
            tv_click.visibility=View.GONE
            tv_upload.text = String.format(resources.getString(R.string.title_reupload_pic))
            voucherUrl = it
        })
    }

    private fun setPayBankBottomSheet() {
        try {
            //支付渠道
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, contentView, false)
            currencyBottomSheet = BottomSheetDialog(this.requireContext())
            currencyBottomSheet.apply {
                setContentView(bottomSheetView)
                currencyBtsAdapter = BankBtsAdapter(
                    lv_bank_item.context,
                    mCurrencyBottomSheetList,
                    BankBtsAdapter.BankAdapterListener { _, position ->
                        currencyPosition = position
                        refreshCurrencyType(position)
                        dismiss()
                    })
                lv_bank_item.adapter = currencyBtsAdapter
                currencyBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }
            //支付帳號
            val accountContentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val accountBottomSheetView =
                layoutInflater.inflate(
                    R.layout.dialog_bottom_sheet_bank_card,
                    accountContentView,
                    false
                )
            accountBottomSheet = BottomSheetDialog(this.requireContext())
            accountBottomSheet.apply {
                setContentView(accountBottomSheetView)
                accountBtsAdapter = BankBtsAdapter(
                    lv_bank_item.context,
                    mAccountBottomSheetList,
                    BankBtsAdapter.BankAdapterListener { _, position ->
                        refreshAccount(position)
                        dismiss()
                    })
                lv_bank_item.adapter = accountBtsAdapter
                accountBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //重置畫面事件
    private fun resetEvent() {
        clearFocus()
        et_recharge_account.setText("")
        et_transaction_id.setText("")
        refreshCurrencyType(0)
        tv_upload.text = String.format(resources.getString(R.string.title_upload_pic))
        tv_click.visibility = View.VISIBLE

        //清空圖片
        ic_screen_shot.visibility = View.VISIBLE
        img_screen_shot.visibility = View.GONE
        img_screen_shot.invalidate()
        img_screen_shot.setImageBitmap(null)
        voucherUrl = ""

        viewModel.clearnRechargeStatus()
    }

    override fun onResume() {
        super.onResume()
        resetEvent()
    }

    //幣種選項
    private fun initBottomSheet() {
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType
        } ?: mutableListOf()) as MutableList<MoneyRechCfg.RechConfig>

        //幣種
        if (mMoneyPayWay?.rechType == "cryptoPay") {
            rechCfgsList.forEach {
                val selectCurrency = CustomImageAdapter.SelectBank(
                    it.prodName.toString(),
                    null
                )
                mCurrencyBottomSheetList.add(selectCurrency)

                val selectAccount = CustomImageAdapter.SelectBank(
                    it.payeeName.toString(),
                    null
                )
                mAccountBottomSheetList.add(selectAccount)
            }
        }

        //篩選充值帳號
        val prodNameList = mutableListOf<String?>()//KEY值
        rechCfgsList.distinctBy {
            it.prodName
        }.forEach {
            prodNameList.add(it.prodName)
        }

        prodNameList.forEach { prodName ->
            var accountList = mutableListOf<String?>()
            rechCfgsList.forEach {
                if (prodName == it.prodName) {
                    accountList.add(it.payeeName)
                }
            }
            mapOfAccount[prodName] = accountList
        }

        rechCfgsList[0].prodName?.let { getAccountBottomSheetList(it) }

    }

    private fun getAccountBottomSheetList(prodName: String) {
        mAccountBottomSheetList.clear()
        mapOfAccount[prodName]?.forEach {
            val selectAccount = CustomImageAdapter.SelectBank(
                it.toString(),
                null
            )
            mAccountBottomSheetList.add(selectAccount)
        }
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {

        try {
            //地址QR code
            if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
                img_qr.visibility = View.INVISIBLE
            } else {
                img_qr.visibility = View.VISIBLE
                Glide.with(this).load(selectRechCfgs?.qrCode).into(img_qr)
            }
            //匯率
            tv_rate.text = String.format(
                getString(R.string.hint_rate),
                selectRechCfgs?.exchangeRate.toString()
            )

            //手續費率/返利
            tv_fee_rate.visibility = View.VISIBLE
            if (selectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利
                tv_fee_rate.text = String.format(getString(R.string.hint_feeback_rate), selectRechCfgs?.rebateFee.toString()) + "%"
            } else {
                tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), abs(selectRechCfgs?.rebateFee ?: 0.0).toString()) + "%"
            }

            //充幣地址
            txv_payee.text = selectRechCfgs?.payee

            //存款時間
            txv_recharge_time.text = TimeUtil.stampToDateHMSTimeZone(Date().time)

            //備註
            txv_remark.visibility = View.VISIBLE
            txv_remark.text = String.format(
                getString(R.string.hint_remark),
                selectRechCfgs?.remark
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值個數
            et_recharge_account.afterTextChanged {
                tv_fee_amount.visibility = View.VISIBLE
                checkRechargeAccount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
//                    et_recharge_account.setText("0")
                    tv_recharge_money.text =
                        String.format(resources.getString(R.string.txv_recharge_money), "0")
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), "0")
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0")
                    }
                    tv_fee_amount.text =
                        String.format(resources.getString(R.string.txv_recharge_money), "0")
                } else {
                    //充值金額
                    tv_recharge_money.text = String.format(
                        resources.getString(R.string.txv_recharge_money),
                        it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                    )
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), (it.toDouble().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))
                    }
                }
            }
        }
    }

    private fun setupFocusEvent() {
        viewModel.apply {
            //充值個數
            setupEditTextFocusEvent(et_recharge_account) { checkRechargeAccount(it, mSelectRechCfgs) }
            //區塊鏈交易ID
            setupEditTextFocusEvent(et_transaction_id) { checkHashCode(et_transaction_id.getText()) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.et_input.text.toString())
        }
    }

    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: MutableList<LocalMedia>?) {
            try {
                // 图片选择结果回调
                // LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                val media = result?.firstOrNull() //這裡應當只會有一張圖片
                val path = when {
                    media?.isCompressed == true -> media.compressPath
                    media?.isCut == true -> media.cutPath
                    else -> media?.path
                }

                val file = File(path!!)
                if (file.exists())
                    uploadImg(file)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(activity, getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.loginRepository.userId.toString()
        val uploadImgRequest = UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.VOUCHER)
        viewModel.uploadImage(uploadImgRequest)
    }

    //日曆
    @SuppressLint("InflateParams")
    private fun calendarBottomSheet() {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar_single, null)
        calendarBottomSheet = BottomSheetDialog(this.requireContext())
        calendarBottomSheet.setContentView(bottomSheetView)
        calendarBottomSheet.calendar.setSelectableDateRange(
            getDateInCalendar(30).first,
            getDateInCalendar(30).second
        )
        calendarBottomSheet.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar
            ) {
                calendarBottomSheet.dismiss()
            }

            override fun onDateRangeSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar,
                endDate: Calendar
            ) {
                txv_recharge_time.text = TimeUtil.stampToDateHMSTimeZone(startDate.timeInMillis)
                calendarBottomSheet.dismiss()
            }
        })
    }

    private fun getDateInCalendar(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = TimeUtil.getTodayEndTimeCalendar()
        val minusDaysCalendar = TimeUtil.getTodayStartTimeCalendar()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair(minusDaysCalendar, todayCalendar)
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoney()
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_account.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                mSelectRechCfgs?.minMoney,
                mSelectRechCfgs?.maxMoney
            )
        )
    }

    //創建MoneyAddRequest
    private fun createMoneyAddRequest(): MoneyAddRequest {
        return MoneyAddRequest(
            rechCfgId = mSelectRechCfgs?.id ?: 0,
            bankCode = null,
            depositMoney = if (et_recharge_account.getText().isNotEmpty()) {
                et_recharge_account.getText().toInt()
            } else {
                0
            },
            payer = null,
            payerName = "",
            payerBankName = null,
            payerInfo = null,
            depositDate = calendarBottomSheet.calendar.startDate?.timeInMillis
                ?: Date().time
        ).apply {
            payee = txv_payee.text.toString()//充幣地址
            payeeName = txv_account.text.toString()//火閉網
            txHashCode = et_transaction_id.getText()
            voucherPath = voucherUrl
        }
    }

    private fun refreshCurrencyType(position: Int) {
        if (rechCfgsList.size > 0) {
            mSelectRechCfgs = rechCfgsList[position]
            refreshSelectRechCfgs(mSelectRechCfgs)
            getMoney()
            updateMoneyRange()
            //更新充值帳號
            getAccountBottomSheetList(mSelectRechCfgs?.prodName ?: "")
            refreshAccount(0)

            txv_currency.text = mSelectRechCfgs?.prodName ?: ""
        } else {
            mSelectRechCfgs = null
        }
    }

    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            txv_account.text = mAccountBottomSheetList[position].bankName
        }
    }
}