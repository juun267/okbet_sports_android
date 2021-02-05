package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

class MoneyTransferSubFragment : BaseFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel.getMoney()

        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {

    }

    private fun initOnclick() {
        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }
    }

    private fun initObserver() {

    }


}
