package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.crypto_pay_fragment.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_icon_and_tick.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants.httpFormat
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs


class CryptoPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: RechCfg? = null //選擇的入款帳號

    //幣種
    private lateinit var currencyBottomSheet: BottomSheetDialog
    private var mCurrencyBottomSheetList = mutableListOf<BtsRvAdapter.SelectBank>()
    private lateinit var currencyBtsAdapter: BtsRvAdapter

    //充值帳戶
    private lateinit var accountBottomSheet: BottomSheetDialog
    private val mAccountBottomSheetList = mutableListOf<BtsRvAdapter.SelectBank>()
    private lateinit var accountBtsAdapter: BtsRvAdapter

    private var rechCfgsList = mutableListOf<RechCfg>()

    private var filterRechCfgsList = HashMap<String?, ArrayList<RechCfg>>()

    private var currentCurrency = ""

    private var voucherUrl: String? = null

    private lateinit var dateTimePicker: TimePickerView

    var depositDate = Date()

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

        initBottomSheetData()
        initView()
        initButton()
        initObserve()
        initTimePicker()
        setCurrencyBottomSheet()
        setAccountBottomSheet()
    }

    private fun initButton() {
        //提交
        btn_submit.setOnClickListener {
            createMoneyAddRequest().let {
                viewModel.rechargeCryptoSubmit(
                    it,
                    mMoneyPayWay?.rechType,
                    mSelectRechCfgs
                )
            }
        }
        //選取日曆
        cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
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
            this.activity?.let { activity ->
                activity.supportFragmentManager.let { fragmentManager ->
                    RechargePicSelectorDialog(activity, mSelectMediaListener).show(
                        fragmentManager, null
                    )
                }
            }
        }
        //去充值
        btn_qr_recharge.setOnClickListener {
            try {
                if (!mSelectRechCfgs?.payUrl.isNullOrEmpty()) {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(mSelectRechCfgs?.payUrl?.httpFormat())
                    )
                    startActivity(browserIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initView() {
        resetEvent()
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(currentCurrency)

        tv_recharge_money.text = String.format(resources.getString(R.string.txv_recharge_money), "0.000")
        
        if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0)  //返利
            tv_fee_amount.text = String.format(getString(R.string.hint_feeback_amount), "0.000")
        else
            tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")

    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner, {
            et_recharge_amount.setError(it)
        })

        //區塊鏈交易ID訊息
        viewModel.hashCodeErrorMsg.observe(viewLifecycleOwner, {
            et_transaction_id.setError(it)
        })
        //API回傳結果
        viewModel.cryptoPayResult.observe(viewLifecycleOwner, {
            if (it.success) {
                resetEvent()
            }
        })
        //支付截圖錯誤訊息
        viewModel.voucherPathErrorMsg.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { url ->
                if (url.isNotEmpty()) {
                    cv_upload.isActivated = true
                    tv_upload.text = url
                    tv_upload.setTextColor(ContextCompat.getColor(tv_upload.context,R.color.colorRedDark))
                } else {
                    cv_upload.isActivated = false
                    tv_upload.text = resources.getString(R.string.title_reupload_pic)
                    tv_upload.setTextColor(ContextCompat.getColor(tv_upload.context,R.color.colorBlackLight))
                }
            }
        })

        //上傳支付截圖
        viewModel.voucherUrlResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                Glide.with(this).load(result).into(img_screen_shot)
                ic_screen_shot.visibility = View.GONE
                img_screen_shot.visibility = View.VISIBLE
                tv_click.visibility = View.GONE
                tv_upload.text = String.format(resources.getString(R.string.title_reupload_pic))
                voucherUrl = result
                checkVoucherUrl(voucherUrl.toString())
            }
        })
    }

    private fun initTimePicker() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -30)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, +30)
        dateTimePicker = TimePickerBuilder(activity,
            OnTimeSelectListener { date, _ ->
                try {
                    depositDate = date
                    txv_recharge_time.text = TimeUtil.stampToDateHMSTimeZone(date)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
            .setRangDate(yesterday, tomorrow)
            .setDate(TimeUtil.toCalendar(Date()))
            .setTimeSelectChangeListener {  }
            .setType(booleanArrayOf(true, true, true, true, true, false))
            .setTitleText(resources.getString(R.string.title_recharge_time))
            .setSubmitColor(ContextCompat.getColor(txv_recharge_time.context,R.color.colorGrayLight))
            .setCancelColor(ContextCompat.getColor(txv_recharge_time.context,R.color.colorGrayLight))
            .isDialog(true)
            .addOnCancelClickListener { }
            .build() as TimePickerView

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM)

        params.leftMargin = 0
        params.rightMargin = 0
        dateTimePicker.dialogContainerLayout.layoutParams = params
        val dialogWindow = dateTimePicker.dialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
            dialogWindow.setGravity(Gravity.BOTTOM)
            dialogWindow.setDimAmount(0.1f)
        }
    }

    private fun  checkVoucherUrl(url:String){
        if (url.isNotEmpty()) {
            cv_upload.isActivated = false
            tv_upload.text = resources.getString(R.string.title_reupload_pic)
            tv_upload.setTextColor(ContextCompat.getColor(tv_upload.context,R.color.colorBlackLight))
        } else {
            cv_upload.isActivated = true
            tv_upload.text = resources.getString(R.string.title_upload_pic_plz)
            tv_upload.setTextColor(ContextCompat.getColor(tv_upload.context,R.color.colorRedDark))
        }
    }

    @SuppressLint("CutPasteId")
    private fun setCurrencyBottomSheet() {
        try {
            //支付渠道
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_icon_and_tick, contentView, false)
            currencyBottomSheet = BottomSheetDialog(this.requireContext())
            currencyBottomSheet.apply {
                setContentView(bottomSheetView)
                currencyBtsAdapter = BtsRvAdapter(
                    mCurrencyBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { bankCard, _ ->
                        currentCurrency = bankCard.bankName.toString()
                        refreshCurrencyType(currentCurrency)
                        resetEvent()
                        dismiss()
                    })
                rv_bank_item.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                rv_bank_item.adapter = currencyBtsAdapter
                tv_game_type_title.text =
                    String.format(resources.getString(R.string.title_choose_currency))
                currencyBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAccountBottomSheet() {
        try {
            //支付帳號
            val accountContentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val accountBottomSheetView =
                layoutInflater.inflate(
                    R.layout.dialog_bottom_sheet_icon_and_tick,
                    accountContentView,
                    false
                )
            accountBottomSheet = BottomSheetDialog(this.requireContext())
            accountBottomSheet.apply {
                setContentView(accountBottomSheetView)
                accountBtsAdapter = BtsRvAdapter(
                    mAccountBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        refreshAccount(position)
                        resetEvent()
                        dismiss()
                    })
                rv_bank_item.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
                rv_bank_item.adapter = accountBtsAdapter
                tv_game_type_title.text =
                    String.format(resources.getString(R.string.title_choose_recharge_account))
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
        et_recharge_amount.setText("")
        et_transaction_id.setText("")
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

    //幣種選項
    private fun initBottomSheetData() {
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType
        } ?: mutableListOf()) as MutableList<RechCfg>

        //幣種
        if (mMoneyPayWay?.rechType == RechType.CRYPTOPAY.code) {
            filterRechCfgsList =
                rechCfgsList.groupBy { it.prodName } as HashMap<String?, ArrayList<RechCfg>>
            filterRechCfgsList.forEach {
                val selectCurrency = BtsRvAdapter.SelectBank(
                    it.key.toString(),
                    null
                )
                mCurrencyBottomSheetList.add(selectCurrency)
            }
        }
        //預設幣種
        currentCurrency = filterRechCfgsList.keys.first().toString()

        rechCfgsList[0].prodName?.let { getAccountBottomSheetList(it) }

    }

    private fun getAccountBottomSheetList(prodName: String) {
        mAccountBottomSheetList.clear()
        filterRechCfgsList[prodName]?.forEach {
            val selectAccount = BtsRvAdapter.SelectBank(
                it.payeeName.toString(),
                null
            )
            mAccountBottomSheetList.add(selectAccount)
        }
        setAccountBottomSheet()
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: RechCfg?) {

        try {
            //地址QR code
            if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
                img_qr.visibility = View.INVISIBLE
            } else {
                img_qr.visibility = View.VISIBLE
                Glide.with(this).load(selectRechCfgs?.qrCode).into(img_qr)
            }
            //充值個數hint
            updateMoneyRange()

            //匯率
            tv_rate.text = String.format(
                getString(R.string.hint_rate),
                ArithUtil.toMoneyFormat(selectRechCfgs?.exchangeRate)
            )

            //手續費率/返利
            when{
                selectRechCfgs?.rebateFee ==  0.0 || selectRechCfgs?.rebateFee == null -> {
                    tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), "0.000") + "%"
                    tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
                }
                selectRechCfgs.rebateFee > 0.0 ->{
                    tv_fee_rate.text = String.format(getString(R.string.hint_feeback_rate), ArithUtil.toMoneyFormat(selectRechCfgs.rebateFee.times(100))) + "%"
                }
                else ->{
                    tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), ArithUtil.toMoneyFormat(ArithUtil.mul(abs(selectRechCfgs.rebateFee), 100.0))) + "%"
                }
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

            //更新充值金額&手續費金額
            refreshMoneyDetail(et_recharge_amount.getText())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值個數
            et_recharge_amount.afterTextChanged {
                if (it.startsWith("0") && it.length > 1) {
                    et_recharge_amount.setText(et_recharge_amount.getText().replace("0", ""))
                    et_recharge_amount.setCursor()
                    return@afterTextChanged
                }

                if (et_recharge_amount.getText().length > 6) {
                    et_recharge_amount.setText(et_recharge_amount.getText().substring(0, 6))
                    et_recharge_amount.setCursor()
                    return@afterTextChanged
                }

                checkRechargeAccount(it, mSelectRechCfgs)

                refreshMoneyDetail(it)

                if (mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                    tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), "0.000") + "%"
                    tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
                }
            }

            //區塊鏈交易ID
            et_transaction_id.afterTextChanged {
                checkHashCode(it)
            }
        }
    }

    //更新充值金額&手續費金額
    private fun refreshMoneyDetail(rechargeAmount:String){
        if (rechargeAmount.isEmpty() || rechargeAmount.isBlank()) {
            tv_recharge_money.text =
                String.format(resources.getString(R.string.txv_recharge_money), "0.000")
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                tv_fee_amount.text =
                    String.format(getString(R.string.hint_feeback_amount), "0.000")
            } else {
                tv_fee_amount.text =
                    String.format(getString(R.string.hint_fee_amount), "0.000")
            }
        } else {
            //充值金額
            tv_recharge_money.text = String.format(
                resources.getString(R.string.txv_recharge_money),
                ArithUtil.toMoneyFormat(
                    rechargeAmount.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                )
            )
            //返利/手續費金額
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                tv_fee_amount.text =
                    String.format(
                        getString(R.string.hint_feeback_amount),
                        ArithUtil.toMoneyFormat(
                            (rechargeAmount.toDouble()
                                .times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(
                                    mSelectRechCfgs?.rebateFee ?: 0.0
                                )
                        )
                    )
            } else {
                tv_fee_amount.text = String.format(
                    getString(R.string.hint_fee_amount),
                    ArithUtil.toMoneyFormat(
                        abs(
                            rechargeAmount.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                                .times(mSelectRechCfgs?.rebateFee ?: 0.0)
                        )
                    )
                )
            }
        }
    }

    private fun setupFocusEvent() {
        viewModel.apply {
            //充值個數
            setupEditTextFocusEvent(et_recharge_amount) {
                checkRechargeAccount(
                    it,
                    mSelectRechCfgs
                )
            }
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
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.VOUCHER)
        viewModel.uploadImage(uploadImgRequest)
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoney()
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_amount.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                mSelectRechCfgs?.minMoney?.toInt(),
                mSelectRechCfgs?.maxMoney?.toInt()
            )
        )
    }

    //創建MoneyAddRequest
    private fun createMoneyAddRequest(): MoneyAddRequest {
        return MoneyAddRequest(
            rechCfgId = mSelectRechCfgs?.id ?: 0,
            bankCode = null,
            depositMoney = if (et_recharge_amount.getText().isNotEmpty()) {
                et_recharge_amount.getText()
            } else {
                ""
            },
            payer = null,
            payerName = "",
            payerBankName = null,
            payerInfo = null,
            depositDate = depositDate.time
        ).apply {
            payee = txv_payee.text.toString()//充幣地址
            payeeName = txv_account.text.toString()//火幣網
            txHashCode = et_transaction_id.getText()
            voucherPath = voucherUrl
        }
    }

    //切換幣種
    private fun refreshCurrencyType(currency: String) {
        if (filterRechCfgsList[currency]?.size ?: 0 > 0) {
            mSelectRechCfgs = filterRechCfgsList[currency]?.get(0)//預設帶入第一筆帳戶資料
            getMoney()
            updateMoneyRange()
            //更新充值帳號
            getAccountBottomSheetList(mSelectRechCfgs?.prodName ?: "")
            refreshAccount(0)//預設帶入第一筆帳戶資料

            txv_currency.text = mSelectRechCfgs?.prodName ?: ""
        } else {
            mSelectRechCfgs = null
        }
    }

    //更新充值帳號
    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            txv_account.text = mAccountBottomSheetList[position].bankName
        }
        mSelectRechCfgs = filterRechCfgsList[currentCurrency]?.get(position)
        refreshSelectRechCfgs(mSelectRechCfgs)
    }
}