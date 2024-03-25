package org.cxct.sportlottery.ui.money.withdraw


import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.BankCardChangeEvent
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentBankCardBinding
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.config.*
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.login.VerifyCallback
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
class BankCardFragment : BaseFragment<WithdrawViewModel,FragmentBankCardBinding>(), VerifyCallback {
    private var transferType: TransferType = TransferType.BANK
    private val mNavController by lazy { findNavController() }
    private val args: BankCardFragmentArgs by navArgs()
    private val mBankCardStatus by lazy { args.editBankCard != null } //true: 編輯, false: 新增
    private var bankCode: String? = null
    private var countDownGoing = false
    private val bankSelectorBottomSheetDialog by lazy { BankSelectorBottomSheetDialog(requireContext()){
          updateSelectedBank(it)
    }}
    override fun onInitView(view: View) {
        initView()
        setupEvent()
        setupObserve()
        setupBankList()
    }

    private fun setupBankList() {
        viewModel.getMoneyConfigs()
        binding.svProtocol.setOnItemSelectedListener{
            binding.tvUsdtName.text = it.showName
        }
    }
    //编辑银行卡跳转的方法
    private fun setupInitData() {
        viewModel.clearBankCardFragmentStatus()
        transferType = args.transferType
        val initData = args.editBankCard
        initData?.let {
            bankCode = it.bankCode
            view.apply {
                binding.tvBankName.text = initData.bankName
                binding.tvUsdtName.text = initData.bankName
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
        setupInitData()
        setupTitle()
        changeTransferType(transferType)

        val ivQuestion = binding.etCreateName.endIconImageButton
        ivQuestion.post {
            val param = ivQuestion.layoutParams as MarginLayoutParams
            param.bottomMargin = 4.dp
            ivQuestion.layoutParams = param
        }

        ivQuestion.setOnClickListener {

            val msg = getString(R.string.P215)
            val cxt = it.context
            CustomAlertDialog().apply {

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

        binding.btnSubmit.setTitleLetterSpacing()
        setupTabLayout(args.transferTypeAddSwitch)

        binding.etWithdrawalPassword.endIconImageButton.setOnClickListener {
            if (binding.etWithdrawalPassword.endIconResourceId == R.drawable.ic_eye_open) {
                binding.eetWithdrawalPassword.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                binding.etWithdrawalPassword.setEndIcon(R.drawable.ic_eye_close)
            } else {
                binding.etWithdrawalPassword.setEndIcon(R.drawable.ic_eye_open)
                binding.eetWithdrawalPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            binding.etWithdrawalPassword.setHasFocus(true)
            binding.eetWithdrawalPassword.setSelection(binding.eetWithdrawalPassword.text.toString().length)
        }

        binding.btnSend.setOnClickListener {
            val phoneNo = UserInfoRepository.userInfo?.value?.phone
            if (phoneNo.isEmptyStr()) {
                ToastUtil.showToast(context, R.string.set_phone_no)
                return@setOnClickListener
            }
            showCaptchaDialog(phoneNo)
        }


        binding.blockSmsValidCode.isVisible = StaticData.isNeedOTPBank()
        binding.eetSmsCode.checkRegisterListener {
            val msg = when {
                it.isNullOrBlank() -> getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyValidCode(it) -> getString(R.string.verification_not_correct)
                else -> null
            }
            binding.etSmsValidCode.setError(msg, false)
            viewModel.checkInputCompleteByAddBankCard()
        }
    }


    private fun updateSelectedBank(bank: Bank) {
        bankCode = bank.value
        binding.tvBankName.text = bank.name
        binding.ivBankIcon.setImageResource(MoneyManager.getBankIconByBankName(bank.name ?: ""))
    }

    private fun setupEvent() {
        setupClickEvent()

        setupTextChangeEvent()
    }

    private fun setupTextChangeEvent()=binding.run{
        viewModel.apply {
            //開戶名
            //真實姓名為空時才可進行編輯see userInfo.observe

            //銀行卡號
            setupClearButtonVisibility(binding.eetBankCardNumber) { checkBankCardNumber(it) }

            //開戶網點
            setupClearButtonVisibility(binding.eetNetworkPoint) { checkNetWorkPoint(it) }

            //錢包地址
            setupClearButtonVisibility(binding.eetWallet) { checkWalletAddress(it) }

            //電話號碼
            setupClearButtonVisibility(binding.eetPhoneNumber) { checkPhoneNumber(it) }

            //提款密碼
            setupClearButtonVisibility(binding.eetWithdrawalPassword) { checkWithdrawPassword(it) }
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


    private fun setupClickEvent()=binding.run {
        tabClickEvent()

        itemUsdtSelector.setOnClickListener {
            svProtocol.invokeClick()
        }

        itemBankSelector.setOnClickListener {
            bankSelectorBottomSheetDialog?.show()
        }

        btnSubmit.setOnClickListener {
            modifyFinish()
            viewModel.apply {
                when (transferType) {
                    TransferType.BANK -> {
                        addBankCard(
                            securityCode = eetSmsCode.text.toString(),
                            bankName = tvBankName.text.toString(),
                            subAddress = eetNetworkPoint.getText().toString(),
                            cardNo = eetBankCardNumber.getText().toString(),
                            fundPwd = eetWithdrawalPassword.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            bankCode = bankCode
                        )
                    }
                    TransferType.CRYPTO -> {
                        addBankCard(
                            securityCode = eetSmsCode.text.toString(),
                            bankName = svProtocol.selectedText ?: "",
                            cardNo = eetWallet.getText().toString(),
                            fundPwd = eetWithdrawalPassword.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                        )
                    }
                    TransferType.E_WALLET -> { //eWallet暫時寫死 與綁定銀行卡相同
                        addBankCard(
                            securityCode = eetSmsCode.text.toString(),
                            bankName = tvBankName.text.toString(),
                            cardNo = eetPhoneNumber.getText().toString(),
                            fundPwd = eetWithdrawalPassword.getText().toString(),
                            id = args.editBankCard?.id?.toString(),
                            uwType = transferType.type,
                            bankCode = bankCode
                        )
                    }
                    TransferType.PAYMAYA -> { //eWallet暫時寫死 與綁定銀行卡相同
                        MoneyType.PAYMAYA_TYPE
                        addBankCard(
                            securityCode = eetSmsCode.text.toString(),
                            bankName = PAYMAYA,
                            cardNo = eetPhoneNumber.getText().toString(),
                            fundPwd = eetWithdrawalPassword.getText().toString(),
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

    private fun setupTabLayout(transferTypeAddSwitch: TransferTypeAddSwitch?) =binding.run{

        transferTypeAddSwitch?.apply {

            tabLayout.getTabAt(0)?.view?.visibility = if (bankTransfer) View.VISIBLE else View.GONE

            tabLayout.getTabAt(1)?.view?.visibility =
                if (cryptoTransfer) View.VISIBLE else View.GONE
            tabLayout.getTabAt(2)?.view?.visibility =
                if (walletTransfer) View.VISIBLE else View.GONE
            tabLayout.getTabAt(3)?.view?.visibility =
                if (paymataTransfer) View.VISIBLE else View.GONE
            var countTabShow = 0
            for(position in 0..3){
                if(tabLayout.getTabAt(position)?.view?.isVisible()==true){
                    countTabShow++
                }
             }
            llTabLayout.visibility = if (countTabShow<2 || mBankCardStatus) View.GONE else View.VISIBLE
        }
    }

    private fun tabClickEvent() {
        if (!mBankCardStatus) {

            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
                    updateBankList()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
    }

    private fun clearBankInputFiled() =binding.run{
        eetBankCardNumber.setText("")
        eetNetworkPoint.setText("")
        eetWithdrawalPassword.setText("")
        eetPhoneNumber.setText("")
        eetSmsCode.setText("")
        bankCode = null
        etSmsValidCode.setError(null,false)
        etBankCardNumber.setError(null,false)
        etNetworkPoint.setError(null,false)
        etWithdrawalPassword.setError(null,false)
        etPhoneNumber.setError(null,false)
        clearFocus()
    }

    private fun clearCryptoInputFiled()=binding.run {
        eetWallet.setText("")
        eetWithdrawalPassword.setText("")
        eetSmsCode.setText("")
        etSmsValidCode.setError(null,false)
        etWallet.setError(null,false)
        etWithdrawalPassword.setError(null,false)
        clearFocus()
    }

    private fun changeTransferType(type: TransferType) {
        changeTab(type)
        setupTitle()
        changeInputField(type)
        viewModel.curTransferType = type
        updateButtonStatus(false)
    }

    private fun changeTab(type: TransferType)=binding.run {
        when (type) {
            TransferType.BANK -> {
                tabLayout.getTabAt(0)?.select()
            }
            TransferType.CRYPTO -> {
                tabLayout.getTabAt(1)?.select()
            }
            TransferType.E_WALLET -> {
                tabLayout.getTabAt(2)?.select()
            }
            TransferType.PAYMAYA -> {
                tabLayout.getTabAt(3)?.select()
            }
            else -> {}
        }
    }

    private fun changeInputField(type: TransferType)=binding.run{
        when (type) {
            TransferType.BANK -> {
                blockBankCardInput.visibility = View.VISIBLE
                itemBankSelector.visibility = View.VISIBLE
                blockCryptoInput.visibility = View.GONE

                //region 顯示Bank欄位
                etBankCardNumber.visibility = View.VISIBLE
                etNetworkPoint.visibility = View.VISIBLE
                //endregion
                //region 隱藏eWallet欄位
                etPhoneNumber.visibility = View.GONE
                //endregion
            }
            TransferType.CRYPTO -> {
                blockBankCardInput.visibility = View.GONE
                blockCryptoInput.visibility = View.VISIBLE
            }
            TransferType.E_WALLET -> {
                blockBankCardInput.visibility = View.VISIBLE
                itemBankSelector.visibility = View.VISIBLE
                blockCryptoInput.visibility = View.GONE

                //region 隱藏Bank欄位
                etBankCardNumber.visibility = View.GONE
                etNetworkPoint.visibility = View.GONE
                //endregion
                //region 顯示eWallet欄位
                etPhoneNumber.visibility = View.VISIBLE
                //endregion
            }
            TransferType.PAYMAYA -> {
                blockBankCardInput.visibility = View.VISIBLE
                itemBankSelector.visibility = View.GONE
                blockCryptoInput.visibility = View.GONE
                //region 隱藏Bank欄位
                etBankCardNumber.visibility = View.GONE
                etNetworkPoint.visibility = View.GONE
                //endregion
                //region 顯示eWallet欄位
                etPhoneNumber.visibility = View.VISIBLE
                //endregion
            }
        }
    }

    private fun setCryptoProtocol(protocol: Detail?) {
        protocol?.contract?.let { binding.svProtocol.selectedText = it }
    }

    private fun setupObserve() {
        viewModel.loading.observe(this){
            if (it)
              //  loading()
            else
                hideLoading()
        }

        viewModel.userInfo.observe(this) {
            it?.fullName?.let { fullName ->
                if (fullName.isNotEmpty()) binding.eetCreateName.setText(
//                    TextUtil.maskFullName(fullName)
                    fullName
                ).also {
                    binding.eetCreateName.isFocusable = false
                }
            } ?: run {
                setupClearButtonVisibility(binding.eetCreateName) { inputFullName ->
                    viewModel.checkCreateName(
                        inputFullName
                    )
                }
            }
        }

        viewModel.rechargeConfigs.observe(this) { rechCfgData ->
            bankSelectorBottomSheetDialog.setBanks(rechCfgData.banks)
            updateBankList()
            viewModel.getCryptoBindList(args.editBankCard)
        }

        viewModel.addMoneyCardSwitch.observe(this) {
            setupTabLayout(it)
        }

        viewModel.addCryptoCardList.observe(this) {

            val sheetList: List<StatusSheetData> = it.map { item ->
                StatusSheetData(item.contract, item.contract)
            }
            binding.svProtocol.dataList = sheetList
            binding.svProtocol.selectedText = it.firstOrNull()?.contract
            binding.svProtocol.selectedTag = it.firstOrNull()?.contract

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

        //開戶名
        viewModel.createNameErrorMsg.observe(
            this.viewLifecycleOwner
        ) {
            binding.etCreateName.setError(it , false)
        }

        //銀行卡號
        viewModel.bankCardNumberMsg.observe(
            this.viewLifecycleOwner,
        ) {
            binding.etBankCardNumber.setError(it ,false)
        }

        //開戶網點
        viewModel.networkPointMsg.observe(
            this.viewLifecycleOwner
        ) {
            binding.etNetworkPoint.setError(it,false)
        }

        //錢包地址
        viewModel.walletAddressMsg.observe(
            this.viewLifecycleOwner
        ) {
            binding.etWallet.setError(it ,false)
        }

        //電話號碼
        viewModel.phoneNumberMsg.observe(this) {
            binding.etPhoneNumber.setError(it ,false)
        }

        //提款密碼
        viewModel.withdrawPasswordMsg.observe(this) {
            binding.etWithdrawalPassword.setError(it ,false)
        }

        viewModel.submitEnable.observe(this) {
            updateButtonStatus(it && (!StaticData.isNeedOTPBank() || binding.eetSmsCode.text.length == 4))
        }

        viewModel.onEmsCodeSended.observe(this) {
            hideLoading()

            if (it?.success == true) {
                CountDownUtil.smsCountDown(lifecycle.coroutineScope, {
                    binding.btnSend.setBtnEnable(false)
                    countDownGoing = true
                }, {
                    binding.btnSend.setBtnEnable(false)
                    binding.btnSend.text = "${it}s"
                }, {
                    binding.btnSend.setBtnEnable(true)
                    binding.btnSend.text = getString(R.string.send)
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
    private fun updateBankList(){
        getBankType()?.let {
            bankSelectorBottomSheetDialog.setBankType(it)
            updateBankSelectorList()
        }
    }
    private fun updateBankSelectorList() {
        bankSelectorBottomSheetDialog?.getSelectedItem()?.let { initBank ->
           updateSelectedBank(initBank)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearBankCardFragmentStatus()
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        binding.btnSubmit.setBtnEnable(isEnable)
    }

    override fun onVerifySucceed(identity: String, validCode: String, phoneNo: String?) {
        loading()
        viewModel.senEmsCode(phoneNo!!, "$identity", validCode)
    }

}
