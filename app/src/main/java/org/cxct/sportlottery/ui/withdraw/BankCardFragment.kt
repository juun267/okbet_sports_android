package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import kotlinx.android.synthetic.main.item_listview_bank_card.view.iv_bank_icon
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.BankUtil
import org.cxct.sportlottery.util.MD5Util.MD5Encode

class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private lateinit var mBankSelectorBottomSheetDialog: BottomSheetDialog
    private lateinit var mBankSelectorAdapter: BankSelectorAdapter
    private val mNavController by lazy { findNavController() }
    private val args: BankCardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bank_card, container, false).apply {

            setupTitle()

            setupInitData(this)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {

            initView()

            setupEvent()

            setupObserve()
        }
    }

    private fun setupInitData(view: View) {
        val initData = args.editBankCard
        initData?.let {
            view.apply {
                btn_delete_bank.visibility = View.VISIBLE
                tv_bank_name.text = initData.bankName
                et_create_name.setText(sLoginData?.fullName)
                et_bank_card_number.setText(initData.cardNo)
                et_network_point.setText(initData.subAddress)
            }
            return@setupInitData
        }
        view.btn_delete_bank.visibility = View.GONE
    }

    private fun setupTitle() {
        when (val currentActivity = this.activity) {
            is WithdrawActivity -> {
                if (args.editBankCard != null) {
                    currentActivity.setToolBarName(getString(R.string.edit_bank_card))
                } else {
                    currentActivity.setToolBarName(getString(R.string.add_credit_card))
                }
            }
            is BankActivity -> {
                if (args.editBankCard != null) {
                    currentActivity.setToolBarName(getString(R.string.edit_bank_card))
                } else {
                    currentActivity.setToolBarName(getString(R.string.add_credit_card))
                }
            }
        }
    }

    private fun initView() {
        et_create_name.apply {
            clearVisibility = if (getText().isNotEmpty()) View.VISIBLE else View.GONE
        }
        initEditTextStatus(et_create_name)
        initEditTextStatus(et_bank_card_number)
        initEditTextStatus(et_network_point)

        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        et_withdrawal_password.eyeVisibility = View.GONE

        setupBankSelector()
    }

    private fun initEditTextStatus(setupView: LoginEditText) {
        setupView.apply {
            clearVisibility = if (getText().isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupBankSelector() {
        mBankSelectorBottomSheetDialog = BottomSheetDialog(requireContext())
        mBankSelectorBottomSheetDialog.apply {
            val bankSelectorBottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, null)
            setContentView(bankSelectorBottomSheetView)
            tv_game_type_title.text = getString(R.string.select_bank)
            mBankSelectorAdapter = BankSelectorAdapter(lv_bank_item.context, mutableListOf(), BankSelectorAdapterListener {
                updateSelectedBank(it)
                dismiss()
            })
            btn_close.setOnClickListener {
                dismiss()
            }
        }
        //TODO Dean : initStatus
        mBankSelectorAdapter.initSelectStatus()
    }

    private fun updateSelectedBank(bank: BankCardList) {
        tv_bank_name.text = bank.bankName
        iv_bank_icon.setImageResource(BankUtil.getBankIconByBankName(bank.bankName))
    }

    private fun setupEvent() {
        setupClickEvent()

        setupTextChangeEvent()
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //開戶名
            setupClearButtonVisibility(et_create_name) { checkCreateName(it) }

            //銀行卡號
            setupClearButtonVisibility(et_bank_card_number) { checkBankCardNumber(it) }

            //開戶網點
            setupClearButtonVisibility(et_network_point) { checkNetWorkPoint(it) }

            //提款密碼
            setupEyeButtonVisibility(et_withdrawal_password) { checkWithdrawPassword(it) }
        }
    }

    private fun setupClearButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.let { view ->
            view.afterTextChanged {
                view.clearVisibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
                checkFun.invoke(it)
            }
        }
    }

    private fun setupEyeButtonVisibility(setupView: LoginEditText, checkFun: (String) -> Unit) {
        setupView.let { view ->
            view.afterTextChanged {
                view.eyeVisibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
                checkFun(it)
            }
        }
    }

    private fun setupClickEvent() {
        item_bank_selector.setOnClickListener {
            mBankSelectorBottomSheetDialog.show()
        }

        btn_submit.setOnClickListener {
            if (checkAllData()) {
                viewModel.addBankCard(createBankAddRequest())
            }
        }

        btn_reset.setOnClickListener {
            resetAll()
        }

        btn_delete_bank.setOnClickListener {
            viewModel.deleteBankCard(args.editBankCard?.id.toString())
        }
    }

    private fun checkAllData(): Boolean {
        viewModel.apply {
            checkCreateName(et_create_name.getText())
            checkBankCardNumber(et_bank_card_number.getText())
            checkNetWorkPoint(et_network_point.getText())
            checkWithdrawPassword(et_withdrawal_password.getText())
            return checkBankCardData()
        }
    }

    private fun setupObserve() {
        viewModel.bankAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                //TODO Dean : bind bank card success Event
                //綁定成功後回至銀行卡列表bank card list
                when (args.navigateFrom) {
                    PageFrom.WITHDRAW -> {
                        val action = BankCardFragmentDirections.actionBankCardFragmentToWithdrawFragment(args.navigateFrom)
                        mNavController.navigate(action)
                    }
                    else -> {
                        mNavController.popBackStack()
                    }
                }
            }
        })

        viewModel.bankDeleteResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                //TODO Dean : delete bank card success Event
                //刪除銀行卡成功後回至銀行卡列表bank card list
                mNavController.popBackStack()
            }
        })

        //錯誤訊息
        //開戶名
        viewModel.createNameErrorMsg.observe(this.viewLifecycleOwner, Observer {
            et_create_name.setError(it ?: "")
        })

        //銀行卡號
        viewModel.bankCardNumberMsg.observe(this.viewLifecycleOwner, Observer {
            et_bank_card_number.setError(it ?: "")
        })

        //開戶網點
        viewModel.networkPointMsg.observe(this.viewLifecycleOwner, Observer {
            et_network_point.setError(it ?: "")
        })

        //提款密碼
        viewModel.withdrawPasswordMsg.observe(this.viewLifecycleOwner, Observer {
            et_withdrawal_password.setError(it ?: "")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearBankCardFragmentStatus()
    }

    private fun createBankAddRequest(): BankAddRequest {
        return BankAddRequest(
            bankName = tv_bank_name.text.toString(),
            subAddress = et_network_point.getText(),
            cardNo = et_bank_card_number.getText(),
            fundPwd = MD5Encode(et_withdrawal_password.getText()),
            fullName = et_create_name.getText(),
            id = args.editBankCard?.id?.toString(),
            userId = sUserInfo.userId.toString(),
            uwType = "bank" //TODO Dean : 目前只有銀行一種, 還沒有UI可以做選擇, 先暫時寫死.
        )
    }

    private fun resetAll() {
        et_create_name.setText("")
        et_bank_card_number.setText("")
        et_network_point.setText("")
        et_withdrawal_password.setText("")
        this@BankCardFragment.activity?.currentFocus?.clearFocus()
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}

class BankSelectorAdapter(private val context: Context, private val dataList: MutableList<BankCardList>, private val listener: BankSelectorAdapterListener) : BaseAdapter() {
    //TODO Dean : dataList的型態待充值的recharcfg/map取得後更改為相同資料型態
    private var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // if remove "if (convertView == null)" will get a warning about reuse view.
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.item_listview_bank_card, parent, false)
            val data = dataList[position]

            view.apply {
                tv_bank_card.text = String.format(context.getString(R.string.selected_bank_card), data.bankName, data.cardNo)
                iv_bank_icon.setImageResource(BankUtil.getBankIconByBankName(data.bankName))
                if (position == selectedPosition)
                    this.ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
                else
                    ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                ll_select_bank_card.setOnClickListener {
                    if (selectedPosition != position) {
                        //                data.isSelected = !data.isSelected
                        selectedPosition = position
                        notifyDataSetChanged()
                        listener.onSelect(data)
                    }
                }
            }
            return view
        }
        return convertView
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun initSelectStatus() {
        selectedPosition = 0
        if (dataList.size > 0) {
            listener.onSelect(dataList[0])
        }
        notifyDataSetChanged()
    }
}

class BankSelectorAdapterListener(private val selectListener: (item: BankCardList) -> Unit) {
    fun onSelect(item: BankCardList) = selectListener(item)
}