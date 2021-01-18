package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.MD5Util.MD5Encode

//TODO Dean : 銀行卡綁定完成後因進入的頁面不同而回到不同頁面. Ex:提款、提款設置
class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

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
                edit_create_name.setText(sLoginData?.fullName)
                edit_bank_card_number.setText(initData.cardNo)
                edit_network_point.setText(initData.subAddress)
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
        if (edit_create_name.text.isNotEmpty()) {
            btn_clear_create_name.visibility = View.VISIBLE
        } else {
            btn_clear_create_name.visibility = View.GONE
        }

        if (edit_bank_card_number.text.isNotEmpty()) {
            btn_clear_bank_card_number.visibility = View.VISIBLE
        } else {
            btn_clear_bank_card_number.visibility = View.GONE
        }

        if (edit_network_point.text.isNotEmpty()) {
            btn_clear_network_point.visibility = View.VISIBLE
        } else {
            btn_clear_network_point.visibility = View.GONE
        }
    }

    private fun setupEvent() {
        btn_submit.setOnClickListener {
            viewModel.addBankCard(createBankAddRequest())
        }

        btn_reset.setOnClickListener {
            resetAll()
        }

        btn_delete_bank.setOnClickListener {
            viewModel.deleteBankCard(args.editBankCard?.id.toString())
        }

        btn_clear_create_name.setOnClickListener {
            edit_create_name.setText("")
        }

        btn_clear_bank_card_number.setOnClickListener {
            edit_bank_card_number.setText("")
        }

        btn_clear_network_point.setOnClickListener {
            edit_network_point.setText("")
        }

        edit_create_name.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    btn_clear_create_name.visibility = View.VISIBLE
                } else {
                    btn_clear_create_name.visibility = View.GONE
                }

            }
        }

        edit_bank_card_number.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    btn_clear_bank_card_number.visibility = View.VISIBLE
                } else {
                    btn_clear_bank_card_number.visibility = View.GONE
                }

            }
        }

        edit_network_point.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    btn_clear_network_point.visibility = View.VISIBLE
                } else {
                    btn_clear_network_point.visibility = View.GONE
                }

            }
        }
    }

    private fun setupObserve() {
        viewModel.bankAddResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                //TODO Dean : bind bank card success Event
                //綁定成功後回至銀行卡列表bank card list
                viewModel.clearBankCardFragmentStatus() //若不清除下一次近來時會直接觸發觀察
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
                viewModel.clearBankCardFragmentStatus() //若不清除下一次近來時會直接觸發觀察
                mNavController.popBackStack()
            }
        })
    }

    private fun createBankAddRequest(): BankAddRequest {
        return BankAddRequest(
            bankName = tv_bank_name.text.toString(),
            subAddress = edit_network_point.text.toString(),
            cardNo = edit_bank_card_number.text.toString(),
            fundPwd = MD5Encode(edit_withdraw_password.text.toString()),
            fullName = edit_create_name.text.toString(),
            id = args.editBankCard?.id?.toString(),
            userId = sUserInfo.userId.toString(),
            uwType = "bank" //TODO Dean : 目前只有銀行一種, 還沒有UI可以做選擇, 先暫時寫死.
        )
    }

    private fun resetAll() {
        edit_create_name.setText("")
        edit_bank_card_number.setText("")
        edit_network_point.setText("")
        edit_withdraw_password.setText("")
        this@BankCardFragment.activity?.currentFocus?.clearFocus()
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}