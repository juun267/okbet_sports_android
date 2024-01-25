package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerLib
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.android.synthetic.main.crypto_pay_fragment.*
import kotlinx.android.synthetic.main.crypto_pay_fragment.btn_submit
import kotlinx.android.synthetic.main.crypto_pay_fragment.cv_account
import kotlinx.android.synthetic.main.crypto_pay_fragment.cv_currency
import kotlinx.android.synthetic.main.crypto_pay_fragment.linMaintenance
import kotlinx.android.synthetic.main.crypto_pay_fragment.tv_fee_amount
import kotlinx.android.synthetic.main.crypto_pay_fragment.tv_fee_rate
import kotlinx.android.synthetic.main.crypto_pay_fragment.tv_rate
import kotlinx.android.synthetic.main.crypto_pay_fragment.tv_recharge_money
import kotlinx.android.synthetic.main.crypto_pay_fragment.txv_account
import kotlinx.android.synthetic.main.crypto_pay_fragment.txv_currency
import kotlinx.android.synthetic.main.dialog_bottom_sheet_icon_and_tick.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MoneyManager.getCryptoIconByCryptoName
import org.cxct.sportlottery.view.LoginEditText
import org.cxct.sportlottery.view.isVisible
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @app_destination 存款-虚拟币转账
 */
class CryptoPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = null //支付類型

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
    private lateinit var dateTimePickerHMS: TimePickerView

    var depositDate = Date()
    var depositDate2 = Date()
    var mCalendar: Calendar =Calendar.getInstance()
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
        initTimePickerForYMD()
        initTimePickerForHMS()
        setCurrencyBottomSheet()
        setAccountBottomSheet()
    }

    private fun initButton() {
        //提交
        btn_submit.setTitleLetterSpacing()
        btn_submit.setOnClickListener {
            createMoneyAddRequest().let {
                viewModel.rechargeCryptoSubmit(
                    it,
                    mMoneyPayWay?.rechType,
                    mSelectRechCfgs
                )
            }
        }
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btn_submit,linMaintenance) }

        //年月日的时间选择器
        ll_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        ll_recharge_time2.setOnClickListener {
            dateTimePickerHMS.show()
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
        cv_upload_image.setOnClickListener {
            this.activity?.let {
                val dialog = RechargePicSelectorDialog()
                dialog.mSelectListener = mSelectMediaListener
                dialog.show(it.supportFragmentManager, null)
            }
        }
        //去充值
      /*  btn_qr_recharge.setOnClickListener {
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
        }*/
    }

    private fun initView() {
        resetEvent()
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(currentCurrency)

        tv_recharge_money.text = String.format(
            resources.getString(R.string.txv_recharge_money),
            sConfigData?.systemCurrencySign,
            "0.00"
        )

        if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0)  //返利
            tv_fee_amount.text = String.format(
                getString(R.string.hint_feeback_amount),
                sConfigData?.systemCurrencySign,
                "0.00"
            )
        else
            tv_fee_amount.text = String.format(
                getString(R.string.hint_fee_amount),
                sConfigData?.systemCurrencySign,
                "0.00"
            )

    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner) {
            et_recharge_amount.setError(it)
        }

        //區塊鏈交易ID訊息
        viewModel.hashCodeErrorMsg.observe(viewLifecycleOwner) {
            et_transaction_id.setError(it)
        }
        //API回傳結果
        viewModel.cryptoPayResult.observe(viewLifecycleOwner) {
            if (it.success) {
                resetEvent()
            }
        }
        //支付截圖錯誤訊息
        viewModel.voucherPathErrorMsg.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { url ->
                if (url.isNotEmpty()) {
                    cv_upload.isActivated = true
                    tv_upload.text = url
                    tv_upload.setTextColor(
                        ContextCompat.getColor(
                            tv_upload.context,
                            R.color.color_F75452_E23434
                        )
                    )
                } else {
                    cv_upload.isActivated = false
                    tv_upload.text = resources.getString(R.string.title_reupload_pic)
                    tv_upload.setTextColor(
                        ContextCompat.getColor(
                            tv_upload.context,
                            R.color.color_BBBBBB_333333
                        )
                    )
                }
            }
        }

        //上傳支付截圖
        viewModel.voucherUrlResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                Glide.with(this).load(result).into(img_screen_shot)
                ic_screen_shot.visibility = View.GONE
                img_screen_shot.visibility = View.VISIBLE
                tv_click.visibility = View.GONE
                tv_upload.text = String.format(resources.getString(R.string.title_reupload_pic))
                voucherUrl = result
                checkVoucherUrl(voucherUrl.toString())
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
                txv_recharge_time.text = TimeUtil.dateToStringFormatYMD(date)
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
            .setTitleText(resources.getString(R.string.title_recharge_time))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(
                ContextCompat.getColor(
                    txv_recharge_time.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    txv_recharge_time.context,
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
                txv_recharge_time2.text = TimeUtil.dateToStringFormatHMS(date)
                upDataTimeHMS()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setTitleText(resources.getString(R.string.title_recharge_time))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(
                ContextCompat.getColor(
                    txv_recharge_time2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    txv_recharge_time2.context,
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
    private fun checkVoucherUrl(url: String) {
        if (url.isNotEmpty()) {
            cv_upload.isActivated = false
            tv_upload.text = resources.getString(R.string.title_reupload_pic)
            tv_upload.setTextColor(
                ContextCompat.getColor(
                    tv_upload.context,
                    R.color.color_BBBBBB_333333
                )
            )
        } else {
            cv_upload.isActivated = true
            tv_upload.text = resources.getString(R.string.title_upload_pic_plz)
            tv_upload.setTextColor(
                ContextCompat.getColor(
                    tv_upload.context,
                    R.color.color_F75452_E23434
                )
            )
        }
    }

    @SuppressLint("CutPasteId")
    private fun setCurrencyBottomSheet() {
        try {
            //支付渠道
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetView =
                layoutInflater.inflate(
                    R.layout.dialog_bottom_sheet_icon_and_tick,
                    contentView,
                    false
                )
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
                rv_bank_item.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
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
                rv_bank_item.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
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
                    getCryptoIconByCryptoName(it.key.toString())
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
            when {
                selectRechCfgs?.rebateFee == 0.0 || selectRechCfgs?.rebateFee == null -> {
                    tv_fee_rate.gone()
                    tv_fee_amount.gone()
                    tv_fee_rate.text =
                        String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                    tv_fee_amount.text = String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
                }
                selectRechCfgs.rebateFee > 0.0 -> {
                    tv_fee_rate.show()
                    tv_fee_amount.show()
                    tv_fee_rate.text = String.format(
                        getString(R.string.hint_feeback_rate),
                        ArithUtil.toMoneyFormat(selectRechCfgs.rebateFee.times(100))
                    ) + "%"
                }
                else -> {
                    tv_fee_rate.show()
                    tv_fee_amount.show()
                    tv_fee_rate.text = String.format(
                        getString(R.string.hint_fee_rate),
                        ArithUtil.toMoneyFormat(ArithUtil.mul(abs(selectRechCfgs.rebateFee), 100.0))
                    ) + "%"
                }
            }

            //充幣地址
            txv_payee.text = selectRechCfgs?.payee

            //存款时间年月日
            txv_recharge_time.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
            //存款时间时分秒
            txv_recharge_time2.text = TimeUtil.dateToStringFormatHMS(Date())
            //備註
            txv_remark.visibility = View.VISIBLE
            txv_remark.text = selectRechCfgs?.remark

            //更新充值金額&手續費金額
            refreshMoneyDetail(et_recharge_amount.getText())
            mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btn_submit,linMaintenance) }
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
                    tv_fee_rate.gone()
                    tv_fee_amount.gone()
                    tv_fee_rate.text =
                        String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                    tv_fee_amount.text = String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
                }else{
                    tv_fee_rate.show()
                    tv_fee_amount.show()
                }
            }

            //區塊鏈交易ID
            et_transaction_id.afterTextChanged {
                checkHashCode(it)
            }
        }
    }

    //更新充值金額&手續費金額
    private fun refreshMoneyDetail(rechargeAmount: String) {
        if (rechargeAmount.isEmpty() || rechargeAmount.isBlank()) {
            tv_recharge_money.text =
                String.format(
                    resources.getString(R.string.txv_recharge_money),
                    sConfigData?.systemCurrencySign,
                    "0.00"
                )
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                tv_fee_amount.text =
                    String.format(
                        getString(R.string.hint_feeback_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
            } else {
                tv_fee_amount.text =
                    String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
            }
        } else {
            //充值金額
            tv_recharge_money.text = String.format(
                resources.getString(R.string.txv_recharge_money), sConfigData?.systemCurrencySign,
                ArithUtil.toMoneyFormat(
                    rechargeAmount.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                )
            )
            //返利/手續費金額
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                tv_fee_amount.text =
                    String.format(
                        getString(R.string.hint_feeback_amount), sConfigData?.systemCurrencySign,
                        ArithUtil.toMoneyFormat(
                            (rechargeAmount.toDouble()
                                .times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(
                                    mSelectRechCfgs?.rebateFee ?: 0.0
                                )
                        )

                    )
            } else {
                tv_fee_amount.text = String.format(
                    getString(R.string.hint_fee_amount), sConfigData?.systemCurrencySign,
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
//选择图片回调监听
    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>?) {
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
                FileUtil.getImageType(path!!)
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
    //上传凭证接口
    private fun uploadImg(file: File) {
        val userId = LoginRepository.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.VOUCHER)
        viewModel.uploadImage(uploadImgRequest)
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoneyAndTransferOut()
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_amount.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                TextUtil.formatBetQuota(mSelectRechCfgs?.minMoney?.toInt() ?: 0),
                TextUtil.formatBetQuota(mSelectRechCfgs?.maxMoney?.toInt() ?: 999999)
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
            depositDate = mCalendar.time.time,
            appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()),
            appsFlyerKey = BuildConfig.AF_APPKEY,
            appsFlyerPkgName = BuildConfig.APPLICATION_ID
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