package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_money_transfer.*
import kotlinx.android.synthetic.main.view_account_balance_2.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.refreshMoneyLoading
import org.cxct.sportlottery.util.setTitleLetterSpacing

class MoneyTransferFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val rvAdapter by lazy {
        MoneyTransferAdapter(ItemClickListener {
            it.let { data ->
                view?.findNavController()?.navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferSubFragment(data))
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.setToolbarName(getString(R.string.account_transfer))
        viewModel.showTitleBar(true)
        return inflater.inflate(R.layout.fragment_money_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        rv_plat.adapter = rvAdapter
        btn_recycle.setTitleLetterSpacing()
        tv_currency_type.text = sConfigData?.systemCurrencySign
    }

    private fun initOnclick() {
        btn_recycle.setOnClickListener {
            viewModel.recycleAllMoney()
        }

        layout_balance.btn_refresh.setOnClickListener {
            it.refreshMoneyLoading()
            viewModel.getMoneyAndTransferOut()
        }
    }


    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.recycleAllMoneyResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let {
                val msg = if (it.success) getString(R.string.money_recycle_succeed) else it.msg
                showPromptDialog(
                    title = getString(R.string.prompt),
                    message = msg,
                    success = it.success
                ){}
            }
        }

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            rvAdapter.addFooterAndSubmitList(it)
            btn_recycle.isEnabled = it.any { data -> data.money != 0.0 }

        }
    }
}