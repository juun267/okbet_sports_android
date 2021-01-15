package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_bank_card.*
import kotlinx.android.synthetic.main.fragment_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [BankCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BankCardFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

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

            setupEvent()
        }
    }

    private fun setupInitData(view: View) {
        val initData = args.editBankCard
        initData?.let {
            view.apply {
                btn_delete_bank_card.visibility = View.VISIBLE
                tv_bank_name.text = initData.bankName
                edit_create_name.setText(sLoginData?.fullName)
                edit_bank_card_number.setText(initData.cardNo)
                edit_network_point.setText("") // TODO Dean : 沒有欄位可以取得, 待釐清
            }
            return@setupInitData
        }
        view.btn_delete_bank_card.visibility = View.GONE
    }

    private fun setupTitle() {
        if (args.editBankCard != null) {
            (this@BankCardFragment.activity as WithdrawActivity).setToolBarName(getString(R.string.edit_bank_card))
        } else {
            (this@BankCardFragment.activity as WithdrawActivity).setToolBarName(getString(R.string.add_credit_card))
        }
    }

    private fun setupEvent() {
        btn_submit.setOnClickListener {
            viewModel.addBankCard(createBankAddRequest())
        }

        btn_reset.setOnClickListener {
            resetAll()
        }
    }

    private fun createBankAddRequest(): BankAddRequest {
        return BankAddRequest(
            bankName = tv_bank_name.text.toString(),
            subAddress = edit_network_point.text.toString(),
            cardNo = edit_bank_card_number.text.toString(),
            fundPwd = edit_withdraw_password.text.toString(),
            fullName = edit_create_name.text.toString(),
            id = args.editBankCard?.id?.toString(),
            userId = sUserInfo.userId.toString(),
            uwType = "bank", //TODO Dean : 目前只有銀行一種, 還沒有UI可以做選擇, 先暫時寫死.
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