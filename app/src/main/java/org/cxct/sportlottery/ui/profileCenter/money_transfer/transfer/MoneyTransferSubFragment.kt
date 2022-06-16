package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing


class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()
    private var isPlatReversed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.setToolbarName(getString(R.string.transfer_info))
        viewModel.showTitleBar(false)
        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        moveAnim(isPlatReversed)
        out_account.selectedText = getString(R.string.plat_money)
        in_account.selectedText = gameDataArg.gameData.showName
        layout_balance.tv_currency_type.text = sConfigData?.systemCurrency
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, gameDataArg.gameData.showName)
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, getString(R.string.plat_money))
        btn_transfer.setTitleLetterSpacing()
    }

    private fun initOnclick() {

        val rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        iv_spin.setOnClickListener {
            iv_spin.startAnimation(rotateAnimation)
            isPlatReversed = !isPlatReversed
            moveAnim(isPlatReversed)
        }

        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }
        btn_transfer.setOnClickListener {
            viewModel.transfer(isPlatReversed, out_account.selectedTag, in_account.selectedTag, et_transfer_money.getText().toLongOrNull())
        }

        out_account.setOnItemSelectedListener {
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
        }

        in_account.setOnItemSelectedListener {
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
        }

    }

    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) hideKeyboard()
            et_transfer_money.isEnabled = !it
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.subInPlatSheetList.observe(viewLifecycleOwner) {
            in_account.dataList = it
            in_account.selectedTag = gameDataArg.gameData.code
        }

        viewModel.subOutPlatSheetList.observe(viewLifecycleOwner) {
            out_account.dataList = it
            out_account.selectedTag = viewModel.platCode
        }

        viewModel.transferResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { it ->
                if (it.success) {
                    context?.let { context ->
                        val dialog = CustomAlertDialog(context).apply {
                            setTitle(context.getString(R.string.prompt))
                            setMessage(if (it.success) context.getString(R.string.transfer_money_succeed) else it.msg)
                            setPositiveClickListener {
                                this@MoneyTransferSubFragment.view?.findNavController()
                                    ?.navigate(MoneyTransferSubFragmentDirections.actionMoneyTransferSubFragmentToMoneyTransferFragment())
                            }
                            setNegativeButtonText(null)
                            setTextColor(if (it.success) R.color.color_909090_666666 else R.color.color_F75452_b73a20)
                        }
                        dialog.show(childFragmentManager, null)
                    }
                }
            }
        }
    }

    private fun moveAnim(isReversed: Boolean) {

        val constraintSet = ConstraintSet()

        constraintSet.apply {
            if (isReversed) {
                tv_title_in.text = getString(R.string.out_account)
                tv_title_out.text = getString(R.string.in_account)
                in_account.bottomSheetTitleText = getString(R.string.out_account)
                out_account.bottomSheetTitleText = getString(R.string.in_account)
                clone(constraint_layout)
                connect(R.id.ll_in, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_in, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_out, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            } else {
                tv_title_in.text = getString(R.string.in_account)
                tv_title_out.text = getString(R.string.out_account)
                in_account.bottomSheetTitleText = getString(R.string.in_account)
                out_account.bottomSheetTitleText = getString(R.string.out_account)
                clone(constraint_layout)
                connect(R.id.ll_out, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_out, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_in, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            }
        }
        constraintSet.applyTo(constraint_layout)

    }

}
