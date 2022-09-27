package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
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
import org.cxct.sportlottery.databinding.ItemListviewBankCardBinding
import org.cxct.sportlottery.network.money.config.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.widget.boundsEditText.ExtendedEditText

/**
 * @app_destination 新增银行卡
 */
class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {
    private var transferType: TransferType = TransferType.BANK
    //TODO 虚拟币添加后续样式修改
    private lateinit var mBankSelectorBottomSheetDialog: BottomSheetDialog
    private lateinit var mBankSelectorAdapter: BankSelectorAdapter
    private val mNavController by lazy { findNavController() }
    private val args: BankCardFragmentArgs by navArgs()
    private val mBankCardStatus by lazy { args.editBankCard != null } //true: 編輯, false: 新增

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
            view.apply {
                btn_delete_bank.visibility = View.VISIBLE
                tv_bank_name.text = initData.bankName
            }
            return@setupInitData
        }
        view.btn_delete_bank.visibility = View.GONE
    }

    private fun setupTitle() {
        when (mBankCardStatus) {
            true -> {
                when (transferType) {
                    TransferType.BANK -> (activity as BankActivity).changeTitle(getString(R.string.edit_bank_card))
                    TransferType.CRYPTO -> (activity as BankActivity).changeTitle(getString(R.string.edit_crypto_card))
                    TransferType.E_WALLET -> (activity as BankActivity).changeTitle(getString(R.string.edit_e_wallet))
                }
            }
            false -> {
                when (transferType) {
                    TransferType.BANK -> (activity as BankActivity).changeTitle(getString(R.string.add_credit_card))
                    TransferType.CRYPTO -> (activity as BankActivity).changeTitle(getString(R.string.add_crypto_card))
                    TransferType.E_WALLET -> (activity as BankActivity).changeTitle(getString(R.string.add_e_wallet))
                }
            }
        }
    }

    private fun initView() {
        changeTransferType(transferType)

        initEditTextStatus(eet_create_name)
        initEditTextStatus(eet_bank_card_number)
        initEditTextStatus(eet_network_point)

        btn_delete_bank.text = when (transferType) {
            TransferType.BANK -> {
                btn_delete_bank.isAllCaps = true
                getString(R.string.delete_bank_card)
            }
            TransferType.CRYPTO -> {
                btn_delete_bank.isAllCaps = true
                getString(R.string.delete_crypto)
            }
            TransferType.E_WALLET -> {
                when (LanguageManager.getSelectLanguage(context)) {
                    LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> btn_delete_bank.isAllCaps =
                        false
                    else -> btn_delete_bank.isAllCaps = true
                }
                getString(R.string.delete_e_wallet)
            }
            TransferType.STATION -> {
                when (LanguageManager.getSelectLanguage(context)) {
                    LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> btn_delete_bank.isAllCaps =
                        false
                    else -> btn_delete_bank.isAllCaps = true
                }
                getString(R.string.delete_e_wallet)
            }
        }
        btn_submit.setTitleLetterSpacing()
        btn_delete_bank.setTitleLetterSpacing()

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
    }

    private fun initEditTextStatus(setupView: ExtendedEditText) {
//        setupView.apply {
//            clearIsShow = text.isNotEmpty()
//        }
    }

    private fun setupBankSelector(rechCfgData: MoneyRechCfgData) {
        mBankSelectorBottomSheetDialog = BottomSheetDialog(requireContext())
        mBankSelectorBottomSheetDialog.apply {
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


    private fun setupEyeButtonVisibility(setupView: ExtendedEditText, checkFun: (String) -> Unit) {
        setupView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkFun(setupView.text.toString())
        }
    }


    private fun setupClickEvent() {
        tabClickEvent()

        item_usdt_selector.setOnClickListener {
            sv_protocol.invokeClick()
        }

        item_bank_selector.setOnClickListener {
            mBankSelectorBottomSheetDialog.show()
        }

        btn_submit.setOnClickListener {
            modifyFinish()
            viewModel.apply {
                when (transferType) {
                    TransferType.BANK -> {
                        addBankCard(
                            bankName = tv_bank_name.text.toString(),
                            subAddress = eet_network_point.getText().toString(),
                            cardNo = eet_bank_card_number.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            bankCode = args.editBankCard?.bankCode.toString()
                        )
                    }
                    TransferType.CRYPTO -> {
                        addBankCard(
                            bankName = sv_protocol.selectedText ?: "",
                            cardNo = eet_wallet.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                        )
                    }
                    TransferType.E_WALLET -> { //eWallet暫時寫死 與綁定銀行卡相同
                        addBankCard(
                            bankName = tv_bank_name.text.toString(),
                            cardNo = eet_phone_number.getText().toString(),
                            fundPwd = eet_withdrawal_password.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            //后台api报错，tel 说bankcode传值和bankName一样
//                            bankCode = args.editBankCard?.bankCode.toString()
                            bankCode = args.editBankCard?.bankCode.toString()
                        )
                    }
                }

            }
        }

        btn_delete_bank.setOnClickListener {
            modifyFinish()
            val passwordDialog = WithdrawPassWordDialog(WithdrawPasswordDialogListener {
                viewModel.deleteBankCard(args.editBankCard?.id!!.toLong(), it)
            })
            passwordDialog.show(childFragmentManager, null)

        }
    }

    private fun setupTabLayout(transferTypeAddSwitch: TransferTypeAddSwitch?) {
        transferTypeAddSwitch?.apply {
            tab_layout.getTabAt(0)?.view?.visibility = if (bankTransfer) View.VISIBLE else View.GONE
            tab_layout.getTabAt(1)?.view?.visibility =
                if (cryptoTransfer) View.VISIBLE else View.GONE
            tab_layout.getTabAt(2)?.view?.visibility =
                if (walletTransfer) View.VISIBLE else View.GONE
            ll_tab_layout.visibility =
                if ((!(bankTransfer && cryptoTransfer && walletTransfer) && (bankTransfer xor cryptoTransfer xor walletTransfer)) || mBankCardStatus) View.GONE else View.VISIBLE
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
                    }
                    updateBankSelectorList()
                    mBankSelectorBottomSheetDialog.lv_bank_item.scrollToPosition(0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
            /*
            tab_bank_card.setOnClickListener {
                transferType = TransferType.BANK
                clearBankInputFiled()
                changeTransferType(transferType)
            }
            tab_crypto.setOnClickListener {
                transferType = TransferType.CRYPTO
                clearCryptoInputFiled()
                changeTransferType(transferType)
            }
            tab_e_wallet.setOnClickListener {//eWallet暫時寫死 與綁定銀行卡相同
                transferType = TransferType.E_WALLET
                clearBankInputFiled()
                changeTransferType(transferType)
            }
            */
        }
    }

    private fun clearBankInputFiled() {
        eet_bank_card_number.setText("")
        eet_network_point.setText("")
        eet_withdrawal_password.setText("")
        eet_phone_number.setText("")

        et_bank_card_number.setError(null,false)
        et_network_point.setError(null,false)
        et_withdrawal_password.setError(null,false)
        et_phone_number.setError(null,false)
        clearFocus()
    }

    private fun clearCryptoInputFiled() {
        eet_wallet.setText("")
        eet_withdrawal_password.setText("")

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
//                tab_bank_card.isSelected = true
//                tab_crypto.isSelected = false
//                tab_e_wallet.isSelected = false
            }
            TransferType.CRYPTO -> {
                tab_layout.getTabAt(1)?.select()
//                tab_bank_card.isSelected = false
//                tab_crypto.isSelected = true
//                tab_e_wallet.isSelected = false
            }
            TransferType.E_WALLET -> {
                tab_layout.getTabAt(2)?.select()
//                tab_bank_card.isSelected = false
//                tab_crypto.isSelected = false
//                tab_e_wallet.isSelected = true
            }
        }
    }

    private fun changeInputField(type: TransferType) {
        when (type) {
            TransferType.BANK -> {
                block_bank_card_input.visibility = View.VISIBLE
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
                loading()
            else
                hideLoading()
        })

        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            it?.fullName?.let { fullName ->
                if (fullName.isNotEmpty()) eet_create_name.setText(
                    TextUtil.maskFullName(fullName)
                )
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
            mBankSelectorBottomSheetDialog.lv_bank_item.scrollToPosition(0)
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
                if (mBankCardStatus) {
                    val promptMessage = when (transferType) {
                        TransferType.BANK -> getString(R.string.text_bank_card_modify_success)
                        TransferType.CRYPTO -> getString(R.string.text_crypto_modify_success)
                        TransferType.E_WALLET -> getString(R.string.text_e_wallet_modify_success)
                        TransferType.STATION -> getString(R.string.text_e_wallet_modify_success)
                    }
                    ToastUtil.showToast(context, promptMessage)
                    mNavController.popBackStack()
                } else {
                    //綁定成功後回至銀行卡列表bank card list
                    val promptMessage = when (transferType) {
                        TransferType.BANK -> getString(R.string.text_bank_card_add_success)
                        TransferType.CRYPTO -> getString(R.string.text_crypto_add_success)
                        TransferType.E_WALLET -> getString(R.string.text_e_wallet_add_success)
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

        viewModel.bankDeleteResult.observe(this.viewLifecycleOwner) { result ->
            if (result.success) {
                val promptMessage = when (transferType) {
                    TransferType.BANK -> getString(R.string.text_bank_card_delete_success)
                    TransferType.CRYPTO -> getString(R.string.text_crypto_delete_success)
                    TransferType.E_WALLET -> getString(R.string.text_e_wallet_delete_success)
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
            updateButtonStatus(it)
        }
    }

    private fun getBankType(): BankType? = when (transferType) {
        TransferType.BANK -> BankType.BANK
        TransferType.E_WALLET -> BankType.E_WALLET
        else -> null
    }

    private fun updateBankSelectorList() {
        getBankType()?.let { bankType ->
            mBankSelectorAdapter.bankType = bankType
            mBankSelectorAdapter.bankList.firstOrNull()?.let { initBank ->
                updateSelectedBank(initBank)
            }
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