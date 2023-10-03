package org.cxct.sportlottery.ui.money.withdraw

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.BankCardChangeEvent
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.ItemListviewBankCardBinding
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.config.*
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.view.isVisible

/**
 * @app_destination 新增银行卡
 */
class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {
    private var transferType: TransferType = TransferType.BANK

    //TODO 虚拟币添加后续样式修改
    private var mBankSelectorBottomSheetDialog: BottomSheetDialog? = null
    private lateinit var mBankSelectorAdapter: BankSelectorAdapter
    private val mNavController by lazy { findNavController() }
    private val args: BankCardFragmentArgs by navArgs()
    private val mBankCardStatus by lazy { args.editBankCard != null } //true: 編輯, false: 新增
    private var bankCode: String? = null
    private var countDownGoing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bank_card, container, false).apply {
            setupInitData(this)
            setupTitle()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            initView()
            setupEvent()
            setupObserve()
            setupBankList()
        }
    }

    private fun setupBankList() {
        viewModel.getMoneyConfigs()

        sv_protocol.setOnItemSelectedListener{
            tv_usdt_name.text = it.showName
        }
    }
    //编辑银行卡跳转的方法
    private fun setupInitData(view: View) {
        viewModel.clearBankCardFragmentStatus()
        transferType = args.transferType
        val initData = args.editBankCard
        initData?.let {
            bankCode = it.bankCode
            view.apply {
                tv_bank_name.text = initData.bankName
                tv_usdt_name.text = initData.bankName
            }
            return@setupInitData

        }
    }

    private fun setupTitle() {
        when (mBankCardStatus) {
            true -> {
                when (transferType) {
                    TransferType.BANK -> (activity as BankActivity).changeTitle(LocalUtils.getString(R.string.edit_bank_card))
                    TransferType.CRYPTO -> (activity as BankActivity).changeTitle(LocalUtils.getString(R.string.edit_crypto_card))
                    TransferType.E_WALLET -> (activity as BankActivity).changeTitle(LocalUtils.getString(
                        R.string.edit_e_wallet))
                    TransferType.PAYMAYA -> (activity as BankActivity).changeTitle(LocalUtils.getString(
                        R.string.edit_paymaya))
                }
            }
            false -> {
                when (transferType) {
                    TransferType.BANK -> (activity as BankActivity).changeTitle(LocalUtils.getString(R.string.add_credit_card))
                    TransferType.CRYPTO -> (activity as BankActivity).changeTitle(LocalUtils.getString(R.string.add_crypto_card))
                    TransferType.E_WALLET -> (activity as BankActivity).changeTitle(LocalUtils.getString(
                        R.string.add_e_wallet))
                    TransferType.PAYMAYA -> (activity as BankActivity).changeTitle(LocalUtils.getString(
                        R.string.add_paymaya))
                }
            }
        }
    }

    private fun initView() {
        changeTransferType(transferType)

        val ivQuestion = et_create_name.endIconImageButton
        ivQuestion.post {
            val param = ivQuestion.layoutParams as MarginLayoutParams
            param.bottomMargin = 4.dp
            ivQuestion.layoutParams = param
        }

        ivQuestion.setOnClickListener {

            val msg = getString(R.string.P215)
            val cxt = it.context
            CustomAlertDialog(cxt).apply {

                setTitle(cxt.getString(R.string.prompt))
                setMessage(msg)
                setPositiveButtonText(cxt.getString(R.string.btn_confirm))
                setNegativeButtonText(cxt.getString(R.string.live_service))
                setNegativeClickListener(serviceClickListener(this@BankCardFragment.childFragmentManager){
                    this.dismiss()
                })

                setCanceledOnTouchOutside(true)
                isCancelable = true
            }.show(childFragmentManager, msg)
        }

        btn_submit.setTitleLetterSpacing()
        setupTabLayout(args.transferTypeAddSwitch)

        et_withdrawal_password.endIconImageButton.setOnClickListener {
            if (et_withdrawal_password.endIconResourceId == R.drawable.ic_eye_open) {
                eet_withdrawal_password.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                et_withdrawal_password.setEndIcon(R.drawable.ic_eye_close)
            } else {
                et_withdrawal_password.setEndIcon(R.drawable.ic_eye_open)
                eet_withdrawal_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            et_withdrawal_password.hasFocus = true
            eet_withdrawal_password.setSelection(eet_withdrawal_password.text.toString().length)
        }

        btnSend.setOnClickListener {
            val phoneNo = UserInfoRepository.userInfo?.value?.phone
            if (phoneNo.isEmptyStr()) {
                ToastUtil.showToast(context, R.string.set_phone_no)
                return@setOnClickListener
            }
            val verifyCodeDialog = VerifyCodeDialog()
            verifyCodeDialog.callBack = { identity, validCode ->
                loading()
                viewModel.senEmsCode(phoneNo!!, "$identity", validCode)
            }
            verifyCodeDialog.show(childFragmentManager, null)
        }

        eet_sms_code.checkRegisterListener {
            val msg = when {
                it.isNullOrBlank() -> getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyValidCode(it) -> getString(R.string.verification_not_correct)
                else -> null
            }
            et_sms_valid_code.setError(msg, false)
            viewModel.checkInputCompleteByAddBankCard()
        }
    }


    private fun setupBankSelector(rechCfgData: MoneyRechCfgData) {
        mBankSelectorBottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            val bankSelectorBottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
            setContentView(bankSelectorBottomSheetView)
            mBankSelectorAdapter =
                BankSelectorAdapter(
                    lv_bank_item.context,
                    rechCfgData.banks,
                    BankSelectorAdapterListener {
                        updateSelectedBank(it)
                        dismiss()
                    }).apply {
                    bankType = getBankType() ?: BankType.BANK
                }

            with(lv_bank_item) {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = mBankSelectorAdapter
            }
            tv_game_type_title.text = getString(R.string.select_bank)
            btn_close.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun updateSelectedBank(bank: Bank) {
        bankCode = bank.value
        tv_bank_name.text = bank.name
        iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(bank.name ?: ""))
    }

    private fun setupEvent() {
        setupClickEvent()

        setupTextChangeEvent()
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //開戶名
            //真實姓名為空時才可進行編輯see userInfo.observe

            //銀行卡號
            setupClearButtonVisibility(eet_bank_card_number) { checkBankCardNumber(it) }

            //開戶網點
            setupClearButtonVisibility(eet_network_point) { checkNetWorkPoint(it) }

            //錢包地址
            setupClearButtonVisibility(eet_wallet) { checkWalletAddress(it) }

            //電話號碼
            setupClearButtonVisibility(eet_phone_number) { checkPhoneNumber(it) }

            //提款密碼
            setupClearButtonVisibility(eet_withdrawal_password) { checkWithdrawPassword(it) }
        }
    }

    private fun setupClearButtonVisibility(setupView: ExtendedEditText, checkFun: (String) -> Unit) {
        setupView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkFun.invoke(setupView.text.toString())
        }
        setupView.afterTextChanged {
            checkFun.invoke(setupView.text.toString())
        }
    }


    private fun setupClickEvent() {
        tabClickEvent()

        item_usdt_selector.setOnClickListener {
            sv_protocol.invokeClick()
        }

        item_bank_selector.setOnClickListener {
            mBankSelectorBottomSheetDialog?.show()
        }

        btn_submit.setOnClickListener {
            modifyFinish()
            viewModel.apply {
                when (transferType) {
                    TransferType.BANK -> {
                        addBankCard(
                            securityCode = eet_sms_code.text.toString(),
                            bankName = tv_bank_name.text.toString(),
                            subAddress = eet_network_point.getText().toString(),
                            cardNo = eet_bank_card_number.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            bankCode = bankCode
                        )
                    }
                    TransferType.CRYPTO -> {
                        addBankCard(
                            securityCode = eet_sms_code.text.toString(),
                            bankName = sv_protocol.selectedText ?: "",
                            cardNo = eet_wallet.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                        )
                    }
                    TransferType.E_WALLET -> { //eWallet暫時寫死 與綁定銀行卡相同
                        addBankCard(
                            securityCode = eet_sms_code.text.toString(),
                            bankName = tv_bank_name.text.toString(),
                            cardNo = eet_phone_number.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            bankCode = bankCode
                        )
                    }
                    TransferType.PAYMAYA -> { //eWallet暫時寫死 與綁定銀行卡相同
                        MoneyType.PAYMAYA_TYPE
                        addBankCard(
                            securityCode = eet_sms_code.text.toString(),
                            bankName = PAYMAYA,
                            cardNo = eet_phone_number.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = TransferType.E_WALLET.type,
                            bankCode = PAYMAYA,
                        )
                    }
                }
            }
        }

//        btn_delete_bank.setOnClickListener {
//            modifyFinish()
//            val passwordDialog = WithdrawPassWordDialog()
//            passwordDialog.listener = WithdrawPasswordDialogListener {
//                viewModel.deleteBankCard(args.editBankCard?.id!!.toLong(), it)
//            }
//            passwordDialog.show(childFragmentManager, null)
//
//        }
    }

    private fun setupTabLayout(transferTypeAddSwitch: TransferTypeAddSwitch?) {

        transferTypeAddSwitch?.apply {

            tab_layout.getTabAt(0)?.view?.visibility = if (bankTransfer) View.VISIBLE else View.GONE

            tab_layout.getTabAt(1)?.view?.visibility =
                if (cryptoTransfer) View.VISIBLE else View.GONE
            tab_layout.getTabAt(2)?.view?.visibility =
                if (walletTransfer) View.VISIBLE else View.GONE
            tab_layout.getTabAt(3)?.view?.visibility =
                if (paymataTransfer) View.VISIBLE else View.GONE
            var countTabShow = 0
            for(position in 0..3){
                if(tab_layout.getTabAt(position)?.view?.isVisible()==true){
                    countTabShow++
                }
             }
            ll_tab_layout.visibility = if (countTabShow<2 || mBankCardStatus) View.GONE else View.VISIBLE
        }
    }

    private fun tabClickEvent() {
        if (!mBankCardStatus) {

            tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    modifyFinish()
                    when (tab?.position) {
                        0 -> {
                            transferType = TransferType.BANK
                            clearBankInputFiled()
                            changeTransferType(transferType)
                        }
                        1 -> {
                            transferType = TransferType.CRYPTO
                            clearCryptoInputFiled()
                            changeTransferType(transferType)
                        }
                        2 -> {//eWallet暫時寫死 與綁定銀行卡相同
                            transferType = TransferType.E_WALLET
                            clearBankInputFiled()
                            changeTransferType(transferType)
                        }
                        3 -> {//eWallet暫時寫死 與綁定銀行卡相同
                            transferType = TransferType.PAYMAYA
                            clearBankInputFiled()
                            changeTransferType(transferType)
                        }
                    }
                    updateBankSelectorList()
                    mBankSelectorBottomSheetDialog?.lv_bank_item?.scrollToPosition(0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
    }

    private fun clearBankInputFiled() {
        eet_bank_card_number.setText("")
        eet_network_point.setText("")
        eet_withdrawal_password.setText("")
        eet_phone_number.setText("")
        eet_sms_code.setText("")
        bankCode = null
        et_sms_valid_code.setError(null,false)
        et_bank_card_number.setError(null,false)
        et_network_point.setError(null,false)
        et_withdrawal_password.setError(null,false)
        et_phone_number.setError(null,false)
        clearFocus()
    }

    private fun clearCryptoInputFiled() {
        eet_wallet.setText("")
        eet_withdrawal_password.setText("")
        eet_sms_code.setText("")
        et_sms_valid_code.setError(null,false)
        et_wallet.setError(null,false)
        et_withdrawal_password.setError(null,false)
        clearFocus()
    }

    private fun changeTransferType(type: TransferType) {
        changeTab(type)
        setupTitle()
        changeInputField(type)
        viewModel.curTransferType = type
        updateButtonStatus(false)
    }

    private fun changeTab(type: TransferType) {
        when (type) {
            TransferType.BANK -> {
                tab_layout.getTabAt(0)?.select()
            }
            TransferType.CRYPTO -> {
                tab_layout.getTabAt(1)?.select()
            }
            TransferType.E_WALLET -> {
                tab_layout.getTabAt(2)?.select()
            }
            TransferType.PAYMAYA -> {
                tab_layout.getTabAt(3)?.select()
            }
        }
    }

    private fun changeInputField(type: TransferType) {
        when (type) {
            TransferType.BANK -> {
                block_bank_card_input.visibility = View.VISIBLE
                item_bank_selector.visibility = View.VISIBLE
                block_crypto_input.visibility = View.GONE

                //region 顯示Bank欄位
                et_bank_card_number.visibility = View.VISIBLE
                et_network_point.visibility = View.VISIBLE
                //endregion
                //region 隱藏eWallet欄位
                et_phone_number.visibility = View.GONE
                //endregion
            }
            TransferType.CRYPTO -> {
                block_bank_card_input.visibility = View.GONE
                block_crypto_input.visibility = View.VISIBLE
            }
            TransferType.E_WALLET -> {
                block_bank_card_input.visibility = View.VISIBLE
                item_bank_selector.visibility = View.VISIBLE
                block_crypto_input.visibility = View.GONE

                //region 隱藏Bank欄位
                et_bank_card_number.visibility = View.GONE
                et_network_point.visibility = View.GONE
                //endregion
                //region 顯示eWallet欄位
                et_phone_number.visibility = View.VISIBLE
                //endregion
            }
            TransferType.PAYMAYA -> {
                block_bank_card_input.visibility = View.VISIBLE
                item_bank_selector.visibility = View.GONE
                block_crypto_input.visibility = View.GONE
                //region 隱藏Bank欄位
                et_bank_card_number.visibility = View.GONE
                et_network_point.visibility = View.GONE
                //endregion
                //region 顯示eWallet欄位
                et_phone_number.visibility = View.VISIBLE
                //endregion
            }
        }
    }

    private fun setCryptoProtocol(protocol: Detail?) {
        protocol?.contract?.let { sv_protocol.selectedText = it }
    }

    private fun setupObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
              //  loading()
            else
                hideLoading()
        })

        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            it?.fullName?.let { fullName ->
                if (fullName.isNotEmpty()) eet_create_name.setText(
//                    TextUtil.maskFullName(fullName)
                    fullName
                ).also {
                    eet_create_name.isFocusable = false
                }
            } ?: run {
                setupClearButtonVisibility(eet_create_name) { inputFullName ->
                    viewModel.checkCreateName(
                        inputFullName
                    )
                }
            }
        }

        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer { rechCfgData ->
            setupBankSelector(rechCfgData)
            updateBankSelectorList()
            mBankSelectorBottomSheetDialog?.lv_bank_item?.scrollToPosition(0)
            viewModel.getCryptoBindList(args.editBankCard)
        })

        viewModel.addMoneyCardSwitch.observe(this.viewLifecycleOwner) {
            setupTabLayout(it)
        }

        viewModel.addCryptoCardList.observe(this.viewLifecycleOwner) {

            val sheetList: List<StatusSheetData> = it.map { item ->
                StatusSheetData(item.contract, item.contract)
            }
            sv_protocol.dataList = sheetList
            sv_protocol.selectedText = it.firstOrNull()?.contract
            sv_protocol.selectedTag = it.firstOrNull()?.contract

            val modifyMoneyCardDetail =
                it.find { list -> list.contract == args.editBankCard?.bankName }
            setCryptoProtocol(modifyMoneyCardDetail ?: it.firstOrNull())
        }


        viewModel.bankAddResult.observe(this.viewLifecycleOwner) { result ->
            if (result.success) {
                EventBusUtil.post(BankCardChangeEvent())
                if (mBankCardStatus) {
                    val promptMessage = when (transferType) {
                        TransferType.BANK -> getString(R.string.text_bank_card_modify_success)
                        TransferType.CRYPTO -> getString(R.string.text_crypto_modify_success)
                        TransferType.E_WALLET -> getString(R.string.text_e_wallet_modify_success)
                        TransferType.PAYMAYA -> getString(R.string.text_pay_maya_modify_success)
                        TransferType.STATION -> getString(R.string.text_pay_maya_modify_success)
                    }
                    ToastUtil.showToast(context, promptMessage)
                    mNavController.popBackStack()
                } else {
                    //綁定成功後回至銀行卡列表bank card list
                    val promptMessage = when (transferType) {
                        TransferType.BANK -> getString(R.string.text_bank_card_add_success)
                        TransferType.CRYPTO -> getString(R.string.text_crypto_add_success)
                        TransferType.E_WALLET -> getString(R.string.text_e_wallet_add_success)
                        TransferType.PAYMAYA -> getString(R.string.text_paymaya_add_success)
                        TransferType.STATION -> getString(R.string.text_e_wallet_add_success)
                    }
                    showPromptDialog(getString(R.string.prompt), promptMessage) {
                        mNavController.popBackStack()
                    }
                }
            } else {
                showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
            }
        }

        viewModel.bankDeleteResult.observe(this.viewLifecycleOwner) {
            val result = it.second
            if (result.success) {
                EventBusUtil.post(BankCardChangeEvent())
                val promptMessage = when (transferType) {
                    TransferType.BANK -> getString(R.string.text_bank_card_delete_success)
                    TransferType.CRYPTO -> getString(R.string.text_crypto_delete_success)
                    TransferType.E_WALLET -> getString(R.string.text_e_wallet_delete_success)
                    TransferType.PAYMAYA -> getString(R.string.text_pay_maya_delete_success)
                    TransferType.STATION -> getString(R.string.text_e_wallet_delete_success)
                }
                showPromptDialog(
                    getString(R.string.prompt),
                    promptMessage
                ) { mNavController.popBackStack() } //刪除銀行卡成功後回至銀行卡列表bank card list
            } else {
                showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
            }
        }

        //錯誤訊息
        //開戶名
        viewModel.createNameErrorMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_create_name.setError(it , false)
        }

        //銀行卡號
        viewModel.bankCardNumberMsg.observe(
            this.viewLifecycleOwner,
        ) {
            et_bank_card_number.setError(it ,false)
        }

        //開戶網點
        viewModel.networkPointMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_network_point.setError(it,false)
        }

        //錢包地址
        viewModel.walletAddressMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_wallet.setError(it ,false)
        }

        //電話號碼
        viewModel.phoneNumberMsg.observe(this.viewLifecycleOwner) {
            et_phone_number.setError(it ,false)
        }

        //提款密碼
        viewModel.withdrawPasswordMsg.observe(
            this.viewLifecycleOwner
        ) {
            et_withdrawal_password.setError(it ,false)
        }

        viewModel.submitEnable.observe(this.viewLifecycleOwner) {
            updateButtonStatus(it && eet_sms_code.text.length == 4)
        }

        viewModel.onEmsCodeSended.observe(this) {
            hideLoading()

            if (it?.success == true) {
                CountDownUtil.smsCountDown(lifecycleScope, {
                    btnSend.setBtnEnable(false)
                    countDownGoing = true
                }, {
                    btnSend.setBtnEnable(false)
                    btnSend.text = "${it}s"
                }, {
                    btnSend.setBtnEnable(true)
                    btnSend.text = getString(R.string.send)
                    countDownGoing = false
                })
            } else {
                it?.msg?.let { msg -> ToastUtil.showToast(context, msg) }
            }
        }
    }

    private fun getBankType(): BankType? = when (transferType) {
        TransferType.BANK -> BankType.BANK
        TransferType.E_WALLET -> BankType.E_WALLET
        else -> null
    }

    private fun updateBankSelectorList() {
        if (!::mBankSelectorAdapter.isInitialized) {
            return
        }

        val bankType = getBankType() ?: return
        mBankSelectorAdapter.bankType = bankType
        mBankSelectorAdapter.bankList.firstOrNull()?.let { initBank ->
            updateSelectedBank(initBank)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearBankCardFragmentStatus()
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        if (isEnable) {
            btn_submit.isEnabled = true
            btn_submit.alpha = 1.0f
        } else {
            btn_submit.isEnabled = false
            btn_submit.alpha = 0.5f
        }
    }
}

class BankSelectorAdapter(
    private val context: Context,
    private val dataList: List<Bank>,
    private val listener: BankSelectorAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPosition = 0

    val bankList: List<Bank> get() = dataList.filter { it.bankType == bankType.ordinal }

    var bankType: BankType = BankType.BANK
        set(value) {
            field = value
            initSelectStatus()
        }

    private fun initSelectStatus() {
        //選中狀態初始化
        dataList.forEach { bank ->
            bank.isSelected = false
        }

        selectedPosition = 0
        bankList[selectedPosition].isSelected = true

        if (dataList.isNotEmpty()) {
            listener.onSelect(dataList[0])
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BankItemViewHolder(
            ItemListviewBankCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is BankItemViewHolder -> {
            holder.bind(bankList[position], position)
        }
        else -> {
            //do nothing
        }
    }

    override fun getItemCount(): Int = bankList.size

    inner class BankItemViewHolder(val binding: ItemListviewBankCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bank: Bank, position: Int) {
            with(binding) {
                root.setOnClickListener {
                    selectBank(position)
                    listener.onSelect(bank)
                }
                tvBankCard.text = bank.name
                ivBankIcon.setImageResource(MoneyManager.getBankIconByBankName(bank.name ?: ""))
                checkBank.isChecked = selectedPosition == position

                checkBank.setOnClickListener {
                    selectBank(position)
                    listener.onSelect(bank)
                    notifyDataSetChanged()
                }

                if (bank.isSelected) {
                    selectedPosition = position
                    checkBank.isChecked = true
                } else {
                    checkBank.isChecked = false
                }
            }
        }

        private fun selectBank(bankPosition: Int) {
            bankList[selectedPosition].isSelected = false
            notifyItemChanged(selectedPosition)
            selectedPosition = bankPosition
            bankList[bankPosition].isSelected = true
            notifyItemChanged(bankPosition)
        }
    }
}

class BankSelectorAdapterListener(private val selectListener: (item: Bank) -> Unit) {
    fun onSelect(item: Bank) = selectListener(item)
}