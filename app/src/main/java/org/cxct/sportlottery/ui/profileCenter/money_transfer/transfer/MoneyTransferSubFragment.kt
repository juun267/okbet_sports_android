package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.layout_balance
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil

class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val gameNameArg by lazy { MoneyTransferSubFragmentArgs.fromBundle(requireArguments()).gameName }

    private val rvOutAdapter by lazy {
        SpinnerOutAdapter(SpinnerOutAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                data.isChecked = true
                out_account.setText(data.showName)
                out_account.dismiss()
            }
        })
    }
    private val rvInAdapter by lazy {
        SpinnerInAdapter(SpinnerInAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                data.isChecked = true
                in_account.setText(data.showName)
                in_account.dismiss()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        out_account.setText(getString(R.string.plat_money))
        in_account.setText(gameNameArg)

        out_account.setAdapter(rvOutAdapter)
        in_account.setAdapter(rvInAdapter)

    }

    private fun initOnclick() {
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

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {

            if (it == null) return@observe


            val resultList = it.toMutableList()
            resultList.add(0, GameData().apply {
                code = "CG"
                showName = getString(R.string.plat_money)
            })

            rvInAdapter.submitList(resultList)
            rvOutAdapter.submitList(resultList)

            /*
            out_account.setSpinnerData(resultList)
            val inList = resultList.toMutableList().apply { remove(SpinnerItem(code = "", showName = gameNameArg)) }
            in_account.setSpinnerData(inList)
            */
//            rvAdapter.addFooterAndSubmitList(resultList)
        }

    }


}
