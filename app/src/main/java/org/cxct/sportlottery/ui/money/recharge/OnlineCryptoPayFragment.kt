package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.DialogBottomSheetIconAndTickBinding
import org.cxct.sportlottery.databinding.OnlineCryptoPayFragmentBinding
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.view.LoginEditText
import org.cxct.sportlottery.util.*
import kotlin.math.abs

@SuppressLint("SetTextI18n")
class OnlineCryptoPayFragment : BaseFragment<MoneyRechViewModel,OnlineCryptoPayFragmentBinding>() {

    private var mMoneyPayWay: MoneyPayWayData? = null //支付類型

    private var mSelectRechCfgs: RechCfg? = null //選擇的入款帳號

    private var rechCfgsList: List<RechCfg> = mutableListOf()

    //幣種
    private lateinit var currencyBottomSheet: BottomSheetDialog
    private val mCurrencyBottomSheetList = mutableListOf<BtsRvAdapter.SelectBank>()
    private lateinit var currencyBtsAdapter: BtsRvAdapter

    //充值帳戶
    private lateinit var accountBottomSheet: BottomSheetDialog
    private val mAccountBottomSheetList = mutableListOf<BtsRvAdapter.SelectBank>()
    private lateinit var accountBtsAdapter: BtsRvAdapter

    private var CurrentCurrency = ""

    private var filterRechCfgsList = HashMap<String?, ArrayList<RechCfg>>()
    private val bottomSheetViewBinding by lazy { DialogBottomSheetIconAndTickBinding.inflate(layoutInflater) } 

    override fun onInitView(view: View) {
        initBottomSheetData()
        initView()
        initButton()
        initObserve()
        setCurrencyBottomSheet()
        setAccountBottomSheet()
        setupServiceButton()
    }

    //幣種選項
    private fun initBottomSheetData() {
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType == mMoneyPayWay?.onlineType
        } ?: mutableListOf()) as MutableList<RechCfg>

        //幣種
        if (mMoneyPayWay?.rechType == RechType.ONLINEPAYMENT.code) {
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
        CurrentCurrency = filterRechCfgsList.keys.first().toString()

        rechCfgsList[0].prodName?.let { getAccountBottomSheetList(it) }

    }


    private fun initObserve() {
        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner) {
            binding.etRechargeAccount.setError(it)
        }

        //在線充值成功
        viewModel.onlinePayCryptoResult.observe(this.viewLifecycleOwner) {
            resetEvent()
        }
        viewModel.rechCheckMsg.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                mSelectRechCfgs?.let {
                    it.open = 2
                    setupMoneyCfgMaintanince(it, binding.btnSubmit, binding.linMaintenance)
                }
                showErrorPromptDialog(getString(R.string.prompt), it) {}
            }
        }
    }

    private fun initView()=binding.run {
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(CurrentCurrency)

        tvRechargeMoney.text = String.format(resources.getString(R.string.txv_recharge_money),
            sConfigData?.systemCurrencySign,
            "0.00")

        if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0)  //返利
            tvFeeAmount.text = String.format(getString(R.string.hint_feeback_amount),
                sConfigData?.systemCurrencySign,
                "0.00")
        else
            tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                sConfigData?.systemCurrencySign,
                "0.00")

        tvRemark.text = "・${getString(R.string.credit_bet_remark)}："
    }

    private fun initButton()=binding.run {
        btnSubmit.setTitleLetterSpacing()
        btnSubmit.setOnClickListener {
            val depositMoney = if (etRechargeAccount.getText().isNotEmpty()) {
                etRechargeAccount.getText()
            } else {
                ""
            }

            val payee = txvCurrency.text.toString()
            val payeeName = txvAccount.text.toString()
            viewModel.rechargeOnlinePay(requireContext(), mSelectRechCfgs, depositMoney, payee, payeeName,null)
        }
        mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,binding.linMaintenance) }

        cvCurrency.setOnClickListener {
            currencyBottomSheet.show()
        }

        cvAccount.setOnClickListener {
            accountBottomSheet.show()
        }
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): OnlineCryptoPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
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

    @SuppressLint("CutPasteId")
    private fun setCurrencyBottomSheet(){
        try {
            currencyBottomSheet = BottomSheetDialog(this.requireContext())
            currencyBottomSheet.apply {
                setContentView(bottomSheetViewBinding.root)
                currencyBtsAdapter = BtsRvAdapter(
                    mCurrencyBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { bankCard, _ ->
                        CurrentCurrency = bankCard.bankName.toString()
                        refreshCurrencyType(CurrentCurrency)
                        resetEvent()
                        dismiss()
                    })
                bottomSheetViewBinding.rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                bottomSheetViewBinding.rvBankItem.adapter = currencyBtsAdapter
                bottomSheetViewBinding.tvGameTypeTitle.text = String.format(resources.getString(R.string.title_choose_currency))
                bottomSheetViewBinding.btnClose.setOnClickListener {
                    this.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAccountBottomSheet()=bottomSheetViewBinding.run {
        try {
            //支付帳號
            val accountContentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetViewBinding by lazy { DialogBottomSheetIconAndTickBinding.inflate(layoutInflater,accountContentView,false) }
            accountBottomSheet = BottomSheetDialog(requireContext())
            accountBottomSheet.apply {
                setContentView(bottomSheetViewBinding.root)
                accountBtsAdapter = BtsRvAdapter(
                    mAccountBottomSheetList,
                    BtsRvAdapter.BankAdapterListener { _, position ->
                        refreshAccount(position)
                        resetEvent()
                        dismiss()
                    })
                rvBankItem.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                rvBankItem.adapter = accountBtsAdapter
                tvGameTypeTitle.text =
                    String.format(resources.getString(R.string.title_choose_recharge_account))
                bottomSheetViewBinding.btnClose.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun refreshCurrencyType(currency: String) {
        if (filterRechCfgsList[currency]?.size ?: 0 > 0) {
            mSelectRechCfgs=filterRechCfgsList[currency]?.get(0)//預設帶入第一筆帳戶資料
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

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: RechCfg?)=binding.run {
        try {
            //匯率
            tvRate.text = String.format(
                getString(R.string.hint_rate),
                ArithUtil.toMoneyFormat(selectRechCfgs?.exchangeRate)
            )

            //手續費率/返利
            when{
                selectRechCfgs?.rebateFee ==  0.0 || selectRechCfgs?.rebateFee == null -> {
                    tvFeeRate.text =
                        String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                    tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                        sConfigData?.systemCurrencySign,
                        "0.00")
                    tvFeeRate.gone()
                    tvFeeAmount.gone()
                }
                selectRechCfgs.rebateFee ?: 0.0 > 0.0 ->{
                    tvFeeRate.text = String.format(getString(R.string.hint_feeback_rate),  ArithUtil.toMoneyFormat(selectRechCfgs?.rebateFee?.times(100))) + "%"
                    tvFeeRate.show()
                    tvFeeAmount.show()
                }
                else ->{
                    tvFeeRate.text = String.format(getString(R.string.hint_fee_rate),  ArithUtil.toMoneyFormat(ArithUtil.mul(abs(selectRechCfgs?.rebateFee ?: 0.0), 100.0))) + "%"
                    tvFeeRate.show()
                    tvFeeAmount.show()
                }
            }

            //備註
            tvHint.text = selectRechCfgs?.remark

            //充值個數限制
            updateMoneyRange()
            mSelectRechCfgs?.let { setupMoneyCfgMaintanince(it,btnSubmit,binding.linMaintenance) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //修改hint
    private fun updateMoneyRange() {
        binding.etRechargeAccount.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                TextUtil.formatMoneyNoDecimal(mSelectRechCfgs?.minMoney?.toInt() ?: 0),
                TextUtil.formatMoneyNoDecimal(mSelectRechCfgs?.maxMoney?.toInt() ?: 0)
            )
        )
    }

    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            binding.txvAccount.text = mAccountBottomSheetList[position].bankName
        }
        mSelectRechCfgs=filterRechCfgsList[CurrentCurrency]?.get(position)
        refreshSelectRechCfgs(mSelectRechCfgs)
    }

    private fun setupTextChangeEvent()=binding.run {
        viewModel.apply {
            //充值個數
            etRechargeAccount.afterTextChanged {
                if(it.startsWith("0") && it.length>1){
                    etRechargeAccount.setText(etRechargeAccount.getText().replace("0",""))
                    etRechargeAccount.setCursor()
                    return@afterTextChanged
                }

                if(etRechargeAccount.getText().length > 6){
                    etRechargeAccount.setText(etRechargeAccount.getText().substring(0,6))
                    etRechargeAccount.setCursor()
                    return@afterTextChanged
                }

                checkRechargeAccount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tvRechargeMoney.text =
                        String.format(resources.getString(R.string.txv_recharge_money),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tvFeeAmount.text =
                            String.format(getString(R.string.hint_feeback_amount),
                                sConfigData?.systemCurrencySign,
                                "0.00")
                    } else {
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                    }
                } else {
                    //充值金額
                    tvRechargeMoney.text = String.format(
                        resources.getString(R.string.txv_recharge_money), sConfigData?.systemCurrencySign,
                        TextUtil.formatMoney(ArithUtil.toMoneyFormat(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).toDouble())
                    )
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tvFeeAmount.text =
                            String.format(getString(R.string.hint_feeback_amount),sConfigData?.systemCurrencySign,
                                ArithUtil.toMoneyFormat((it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))

                            )
                    } else {
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            ArithUtil.toMoneyFormat(abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0).times(mSelectRechCfgs?.rebateFee?:0.0))))
                    }

                    if(mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null) {
                        tvFeeRate.text =
                            String.format(getString(R.string.hint_fee_rate), "0.00") + "%"
                        tvFeeAmount.text = String.format(getString(R.string.hint_fee_amount),
                            sConfigData?.systemCurrencySign,
                            "0.00")
                        tvFeeRate.gone()
                        tvFeeAmount.gone()
                    }else{
                        tvFeeRate.show()
                        tvFeeAmount.show()
                    }
                }
            }
        }
    }

    private fun setupFocusEvent() {
        viewModel.apply {
            //充值個數
            setupEditTextFocusEvent(binding.etRechargeAccount) { checkRechargeAccount(it, mSelectRechCfgs) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.getText())
        }
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoneyAndTransferOut()
    }

    private fun resetEvent() {
        clearFocus()
        binding.etRechargeAccount.setText("")
        viewModel.clearnRechargeStatus()
    }
    //联系客服
    private fun setupServiceButton() {
        binding.tvServiceOnline.setServiceClick(childFragmentManager)
    }
}