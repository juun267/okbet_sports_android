package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_money_transfer.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil

class MoneyTransferFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val rvAdapter by lazy {
        MoneyTransferAdapter(ItemClickListener {
            it.let { data ->
                view?.findNavController()?.navigate(MoneyTransferFragmentDirections.actionMoneyTransferFragmentToMoneyTransferSubFragment(data))
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

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
    }

    private fun initOnclick() {
        btn_recycle.setOnClickListener {
            viewModel.recycleAllMoney()
        }

        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }
    }


    private fun initObserver() {
        receiver.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.recycleAllMoneyResult.observe(viewLifecycleOwner) {
            it?.apply {
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(getString(R.string.prompt))
                    setMessage(it.msg)
                    setNegativeButtonText(null)
                    setTextColor(if (it.success) R.color.gray6 else R.color.red2)
                }
                dialog.show()
            }
        }

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            rvAdapter.addFooterAndSubmitList(it)
        }

    }
}