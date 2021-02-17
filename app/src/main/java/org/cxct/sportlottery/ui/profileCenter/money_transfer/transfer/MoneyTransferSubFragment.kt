package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.layout_balance
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil

class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()

    private val rvOutAdapter by lazy {
        SpinnerOutAdapter(viewModel.defaultOutPlat, SpinnerOutAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                out_account.setText(data.showName)
                out_account.tag = data.code
                out_account.dismiss()
            }
        })
    }

    private val rvInAdapter by lazy {
        SpinnerInAdapter(viewModel.defaultInPlat, SpinnerInAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                in_account.setText(data.showName)
                in_account.tag = data.code
                in_account.dismiss()
            }
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel.setToolbarName(getString(R.string.transfer_info))
        viewModel.showTitleBar(false)
        viewModel.defaultInPlat = gameDataArg.gameData.code
        viewModel.defaultOutPlat = "CG"
        viewModel.setPlatDataList()

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
        in_account.setText(gameDataArg.gameData.showName)

        out_account.tag = "CG"
        in_account.tag = gameDataArg.gameData.code
    }

    private fun initOnclick() {
        out_account.setOnItemClickListener(rvOutAdapter)

        in_account.setOnItemClickListener(rvInAdapter)

        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }
        btn_transfer.setOnClickListener {
            viewModel.transfer(out_account.tag.toString(), in_account.tag.toString(), et_transfer_money.getText().toLongOrNull())
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
            rvOutAdapter.dataList = viewModel.outPlatDataList
            rvInAdapter.dataList = viewModel.inPlatDataList
        }

        viewModel.transferResult.observe(viewLifecycleOwner) {
            it?.apply {
                    val dialog = CustomAlertDialog(requireActivity()).apply {
                        setTitle(getString(R.string.prompt))
                        setMessage(it.msg)
                        setNegativeButtonText(null)
                        setTextColor(if (it.success) R.color.gray6 else R.color.red2)
                    }
                    dialog.show()

                if (it.success) {
                    view?.findNavController()?.navigate(MoneyTransferSubFragmentDirections.actionMoneyTransferSubFragmentToMoneyTransferFragment())
                }
            }
        }

    }


}
