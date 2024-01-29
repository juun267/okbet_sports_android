package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.CryptoPayFragmentBinding
import org.cxct.sportlottery.databinding.DialogBottomSheetIconAndTickBinding
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
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @app_destination 存款-虚拟币转账
 */
class CryptoPayFragment : BaseFragment<MoneyRechViewModel,CryptoPayFragmentBinding>(MoneyRechViewModel::class) {

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

    fun setArguments(moneyPayWay: MoneyPayWayData?): CryptoPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    override fun onInitView(view: View) {
        initBottomSheetData()
        initView()
        initButton()
        initObserve()
        initTimePickerForYMD()
        initTimePickerForHMS()
        setCurrencyBottomSheet()
        setAccountBottomSheet()
    }

    private fun initButton()=binding.run {
        //提交
        btnSubmit.setTitleLetterSpacing()
        btnSubmit.setOnClickListener {
            createMoneyAddRequest().let {
                viewModel.rechargeCryptoSubmit(
                    it,
                    mMoneyPayWay?.rechType,
                    mSelectRechCfgs
                )
            }
        }
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,binding.linMaintenance.root) }

        //年月日的时间选择器
        llRechargeTime.setOnClickListener {
            dateTimePicker.show()
        }
        //时分秒的选择器
        llRechargeTime2.setOnClickListener {
            dateTimePickerHMS.show()
        }
        //選擇幣種
        cvCurrency.setOnClickListener {
            currencyBottomSheet.show()
        }
        //充值帳號
        cvAccount.setOnClickListener {
            accountBottomSheet.show()
        }
        //複製
        btnQrCopy.setOnClickListener {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText(null, txvPayee.text)
            clipboard?.setPrimaryClip(clipData)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }
        //上傳照片
        cvUpload.setOnClickListener {
            val dialog = RechargePicSelectorDialog()
            dialog.mSelectListener = mSelectMediaListener
            dialog.show(requireActivity().supportFragmentManager, null)
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

    private fun initView()=binding.run {
        resetEvent()
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(currentCurrency)

        tvRechargeMoney.text = String.format(
            resources.getString(R.string.txv_recharge_money),
            sConfigData?.systemCurrencySign,
            "0.00"
        )

        if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0)  //返利
            tvFeeAmount.text = String.format(
                getString(R.string.hint_feeback_amount),
                sConfigData?.systemCurrencySign,
                "0.00"
            )
        else
            tvFeeAmount.text = String.format(
                getString(R.string.hint_fee_amount),
                sConfigData?.systemCurrencySign,
                "0.00"
            )

    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {

        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner) {
            binding.etRechargeAmount.setError(it)
        }

        //區塊鏈交易ID訊息
        viewModel.hashCodeErrorMsg.observe(viewLifecycleOwner) {
            binding.etTransactionId.setError(it)
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
                    binding.cvUpload.isActivated = true
                    binding.tvUpload.text = url
                    binding.tvUpload.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_F75452_E23434
                        )
                    )
                } else {
                    binding.cvUpload.isActivated = false
                    binding.tvUpload.text = resources.getString(R.string.title_reupload_pic)
                    binding.tvUpload.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_BBBBBB_333333
                        )
                    )
                }
            }
        }

        //上傳支付截圖
        viewModel.voucherUrlResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                Glide.with(this).load(result).into(binding.imgScreenShot)
                binding.icScreenShot.visibility = View.GONE
                binding.imgScreenShot.visibility = View.VISIBLE
                binding.tvClick.visibility = View.GONE
                binding.tvUpload.text = String.format(resources.getString(R.string.title_reupload_pic))
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
                binding.txvRechargeTime.text = TimeUtil.dateToStringFormatYMD(date)
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
                    binding.txvRechargeTime.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    binding.txvRechargeTime.context,
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
                binding.txvRechargeTime2.text = TimeUtil.dateToStringFormatHMS(date)
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
                    binding.txvRechargeTime2.context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    binding.txvRechargeTime2.context,
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
    private fun checkVoucherUrl(url: String)=binding.run {
        if (url.isNotEmpty()) {
            cvUpload.isActivated = false
            tvUpload.text = resources.getString(R.string.title_reupload_pic)
            tvUpload.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_BBBBBB_333333
                )
            )
        } else {
            cvUpload.isActivated = true
            tvUpload.text = resources.getString(R.string.title_upload_pic_plz)
            tvUpload.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
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
            val bottomSheetViewBinding = DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,contentView,false)
            currencyBottomSheet = BottomSheetDialog(this.requireContext())
            currencyBottomSheet.apply {
                setContentView(bottomSheetViewBinding.root)
                currencyBtsAdapter = BtsRvAdapter(
                    mCurrencyBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { bankCard, _ ->
                        currentCurrency = bankCard.bankName.toString()
                        refreshCurrencyType(currentCurrency)
                        resetEvent()
                        dismiss()
                    })
                bottomSheetViewBinding.rvBankItem.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                bottomSheetViewBinding.rvBankItem.adapter = currencyBtsAdapter
                bottomSheetViewBinding.tvGameTypeTitle.text =
                    String.format(resources.getString(R.string.title_choose_currency))
                bottomSheetViewBinding.btnClose.setOnClickListener {
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
            val bottomSheetViewBinding = DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,accountContentView,false)
            accountBottomSheet = BottomSheetDialog(this.requireContext())
            accountBottomSheet.apply {
                setContentView(bottomSheetViewBinding.root)
                accountBtsAdapter = BtsRvAdapter(
                    mAccountBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        refreshAccount(position)
                        resetEvent()
                        dismiss()
                    })
                bottomSheetViewBinding.rvBankItem.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                bottomSheetViewBinding.rvBankItem.adapter = accountBtsAdapter
                bottomSheetViewBinding.tvGameTypeTitle.text =
                    String.format(resources.getString(R.string.title_choose_recharge_account))
                bottomSheetViewBinding.btnClose.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //重置畫面事件
    private fun resetEvent()=binding.run {
        clearFocus()
        etRechargeAmount.setText("")
        etTransactionId.setText("")
        tvUpload.text = String.format(resources.getString(R.string.title_upload_pic))
        tvClick.visibility = View.VISIBLE

        //清空圖片
        icScreenShot.visibility = View.VISIBLE
        imgScreenShot.visibility = View.GONE
        imgScreenShot.invalidate()
        imgScreenShot.setImageBitmap(null)
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
    private fun refreshSelectRechCfgs(selectRechCfgs: RechCfg?)=binding.run {

        try {
            //地址QR code
            if (selectRechCfgs?.qrCode.isNullOrEmpty()) {
                imgQr.visibility = View.INVISIBLE
            } else {
                imgQr.visibility = View.VISIBLE
                Glide.with(requireContext()).load(selectRechCfgs?.qrCode).into(imgQr)
            }
            //充值個數hint
            updateMoneyRange()

            //匯率
            tvRate.text = String.format(
                getString(R.string.hint_rate),
                ArithUtil.toMoneyFormat(selectRechCfgs?.exchangeRate)
            )

            //手續費率/返利
            when {
                selectRechCfgs?.rebateFee == 0.0 || selectRechCfgs?.rebateFee == null -> {
                    tvFeeRate.gone()
                    tvFeeAmount.gone()
                    tvFeeRate.text =
                        String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                    tvFeeAmount.text = String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
                }
                selectRechCfgs.rebateFee > 0.0 -> {
                    tvFeeRate.show()
                    tvFeeAmount.show()
                    tvFeeRate.text = String.format(
                        getString(R.string.hint_feeback_rate),
                        ArithUtil.toMoneyFormat(selectRechCfgs.rebateFee.times(100))
                    ) + "%"
                }
                else -> {
                    tvFeeRate.show()
                    tvFeeAmount.show()
                    tvFeeRate.text = String.format(
                        getString(R.string.hint_fee_rate),
                        ArithUtil.toMoneyFormat(ArithUtil.mul(abs(selectRechCfgs.rebateFee), 100.0))
                    ) + "%"
                }
            }

            //充幣地址
            txvPayee.text = selectRechCfgs?.payee

            //存款时间年月日
            txvRechargeTime.text = TimeUtil.timeFormat(Date().time,TimeUtil.YMD_FORMAT)
            //存款时间时分秒
            txvRechargeTime2.text = TimeUtil.dateToStringFormatHMS(Date())
            //備註
            txvRemark.visibility = View.VISIBLE
            txvRemark.text = selectRechCfgs?.remark

            //更新充值金額&手續費金額
            refreshMoneyDetail(etRechargeAmount.getText())
            mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,binding.linMaintenance.root) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTextChangeEvent()=binding.run {
        viewModel.apply {
            //充值個數
            etRechargeAmount.afterTextChanged {
                if (it.startsWith("0") && it.length > 1) {
                    etRechargeAmount.setText(etRechargeAmount.getText().replace("0", ""))
                    etRechargeAmount.setCursor()
                    return@afterTextChanged
                }

                if (etRechargeAmount.getText().length > 6) {
                    etRechargeAmount.setText(etRechargeAmount.getText().substring(0, 6))
                    etRechargeAmount.setCursor()
                    return@afterTextChanged
                }

                checkRechargeAccount(it, mSelectRechCfgs)

                refreshMoneyDetail(it)

                if (mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                    tvFeeRate.gone()
                    tvFeeAmount.gone()
                    tvFeeRate.text =
                        String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                    tvFeeAmount.text = String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
                }else{
                    tvFeeRate.show()
                    tvFeeAmount.show()
                }
            }

            //區塊鏈交易ID
            etTransactionId.afterTextChanged {
                checkHashCode(it)
            }
        }
    }

    //更新充值金額&手續費金額
    private fun refreshMoneyDetail(rechargeAmount: String)=binding.run {
        if (rechargeAmount.isEmpty() || rechargeAmount.isBlank()) {
            tvRechargeMoney.text =
                String.format(
                    resources.getString(R.string.txv_recharge_money),
                    sConfigData?.systemCurrencySign,
                    "0.00"
                )
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                tvFeeAmount.text =
                    String.format(
                        getString(R.string.hint_feeback_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
            } else {
                tvFeeAmount.text =
                    String.format(
                        getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00"
                    )
            }
        } else {
            //充值金額
            tvRechargeMoney.text = String.format(
                resources.getString(R.string.txv_recharge_money), sConfigData?.systemCurrencySign,
                ArithUtil.toMoneyFormat(
                    rechargeAmount.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                )
            )
            //返利/手續費金額
            if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                tvFeeAmount.text =
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
                tvFeeAmount.text = String.format(
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

    private fun setupFocusEvent()=binding.run {
        viewModel.apply {
            //充值個數
            setupEditTextFocusEvent(etRechargeAmount) {
                checkRechargeAccount(
                    it,
                    mSelectRechCfgs
                )
            }
            //區塊鏈交易ID
            setupEditTextFocusEvent(etTransactionId) { checkHashCode(etTransactionId.getText()) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.getText())
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
        binding.etRechargeAmount.setHint(
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
            depositMoney = if (binding.etRechargeAmount.getText().isNotEmpty()) {
                binding.etRechargeAmount.getText()
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
            payee = binding.txvPayee.text.toString()//充幣地址
            payeeName = binding.txvAccount.text.toString()//火幣網
            txHashCode = binding.etTransactionId.getText()
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

            binding.txvCurrency.text = mSelectRechCfgs?.prodName ?: ""
        } else {
            mSelectRechCfgs = null
        }
    }

    //更新充值帳號
    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            binding.txvAccount.text = mAccountBottomSheetList[position].bankName
        }
        mSelectRechCfgs = filterRechCfgsList[currentCurrency]?.get(position)
        refreshSelectRechCfgs(mSelectRechCfgs)
    }
}