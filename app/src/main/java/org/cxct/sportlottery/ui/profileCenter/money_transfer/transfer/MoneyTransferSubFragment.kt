package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.content_rv_bank_list_new.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil


class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.setToolbarName(getString(R.string.transfer_info))
        viewModel.showTitleBar(false)
        viewModel.setInSheetDataList(viewModel.platCode, R.string.plat_money)
        viewModel.setOutSheetDataList(viewModel.platCode, R.string.plat_money)

        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initOnclick()
        initObserver()
    }

    private fun initOnclick() {

        val rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        iv_spin.setOnClickListener {
            iv_spin.startAnimation(rotateAnimation)
            viewModel.switchPlat()
        }

        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }
        btn_transfer.setOnClickListener {
            val isReversed = viewModel.isPlatSwitched.value?.peekContent() ?: false
            if (!isReversed) {
                viewModel.transfer(out_account.selectedTag, in_account.selectedTag, et_transfer_money.getText().toLongOrNull())
            } else {
                viewModel.transfer(in_account.selectedTag, out_account.selectedTag, et_transfer_money.getText().toLongOrNull())
            }
        }

        out_account.setOnItemSelectedListener {
            in_account.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
        }

        in_account.setOnItemSelectedListener {
            out_account.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
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

            out_account.selectedText = getString(R.string.plat_money)
            in_account.selectedText = gameDataArg.gameData.showName

            out_account.selectedTag = viewModel.platCode
            in_account.selectedTag = gameDataArg.gameData.code

            out_account.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, in_account.selectedText)
            in_account.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, out_account.selectedText)

        }

        viewModel.transferResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { it ->
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(getString(R.string.prompt))
                    setMessage(it.msg)
                    setNegativeButtonText(null)
                    setTextColor(if (it.success) R.color.colorGray else R.color.colorRedDark)
                }
                dialog.show()

                if (it.success) {
                    view?.findNavController()?.navigate(MoneyTransferSubFragmentDirections.actionMoneyTransferSubFragmentToMoneyTransferFragment())
                }
            }
        }
        viewModel.isPlatSwitched.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { isReversed ->
                moveAnim(isReversed)
            }
        }
    }

    private fun moveAnim(isReversed: Boolean) {

        val constraintSet = ConstraintSet()

        constraintSet.apply {
            if (isReversed) {
                tv_title_in.text = getString(R.string.out_account)
                tv_title_out.text = getString(R.string.in_account)
                clone(constraint_layout)
                connect(R.id.ll_in, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, getDp(10))
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_in, ConstraintSet.BOTTOM, getDp(10))
                connect(R.id.ll_out, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, getDp(10))
            } else {
                tv_title_in.text = getString(R.string.in_account)
                tv_title_out.text = getString(R.string.out_account)
                clone(constraint_layout)
                connect(R.id.ll_out, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, getDp(10))
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_out, ConstraintSet.BOTTOM, getDp(10))
                connect(R.id.ll_in, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, getDp(10))
            }
        }
        constraintSet.applyTo(constraint_layout)

    }

    private fun getDp(inputValue: Int): Int {
        val d = context?.resources?.displayMetrics?.density?:0.0f
        return (inputValue * d).toInt() // margin in pixels
    }

}
