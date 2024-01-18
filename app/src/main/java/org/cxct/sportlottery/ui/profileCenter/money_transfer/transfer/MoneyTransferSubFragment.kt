package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.view_account_balance_2.*
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.refreshMoneyLoading
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber
import java.math.BigDecimal


class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()
    private var isPlatReversed = false
    private var gameMoney = 0.0 //第三方遊戲餘額

    private var useDefaultUnit = true
    private val defaultUnit = 1.0 //預設unit
    private val thirdTransferUnit = sConfigData?.thirdTransferUnit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.setToolbarName(getString(R.string.transfer_info))
        viewModel.showTitleBar(false)
        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
        viewModel.getAllBalance()
    }

    private fun initView() {

        tv_balance.setText(R.string.title_current_money)
        moveAnim(isPlatReversed)
        viewModel.initCode()
        //region 靠左置中＆移除padding
        out_account.tv_selected.gravity = Gravity.START or Gravity.CENTER
        out_account.tv_selected.setPadding(0, 0, 0, 0)
        in_account.tv_selected.gravity = Gravity.START or Gravity.CENTER
        in_account.tv_selected.setPadding(0, 0, 0, 0)
        //endregion
        out_account.selectedText = getString(R.string.plat_money)
        in_account.selectedText = gameDataArg.gameData.showName
        gameMoney = gameDataArg.gameData.money ?: 0.0
        layout_balance.tv_currency_type.text = sConfigData?.systemCurrencySign
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, gameDataArg.gameData.showName)
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, getString(R.string.plat_money))
        et_transfer_money.afterTextChanged {
            if (it.isEmptyStr()) {
                et_transfer_money.setError("")
                return@afterTextChanged
            }

            val enable = it.toBigDecimal() > BigDecimal(0)
            btn_transfer.isEnabled = enable
            if (enable) {
                et_transfer_money.setError("")
            } else {
                et_transfer_money.setError(getString(R.string.error_input_amount))
            }
        }
        btn_transfer.setTitleLetterSpacing()
        val hint =
            if (thirdTransferUnit == null) {
                useDefaultUnit = true
                getString(R.string.transfer_money_minimum,
                    ArithUtil.toMoneyFormatForHint(defaultUnit))
            } else {
                useDefaultUnit = false
                getString(R.string.transfer_money_minimum,
                    ArithUtil.toMoneyFormatForHint(thirdTransferUnit))
            }
        Timber.d("thirdTransferUnit: $thirdTransferUnit, hint: $hint")
        et_transfer_money.setMaxLength(20)
        et_transfer_money.setHint(hint)
        et_transfer_money.apply {
            tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            tv_title.setTextColor(resources.getColor(R.color.color_535D76))
            tv_title.setTypeface(Typeface.DEFAULT)
            et_input.minHeight = 50.dp
            v_bottom_line.visibility = View.INVISIBLE
            btn_clear.setImageResource(R.drawable.ic_clear_gray)
            btn_clear.setPadding(2, 2, 2, 2)
            clearIsShow = false
        }

    }

    private fun setupSelectedTag() {
        if (viewModel.outCode == null) {
            viewModel.outCode = viewModel.platCode
        }
        if (viewModel.inCode == null) {
            viewModel.inCode = gameDataArg.gameData.code
        }
        out_account.selectedTag = viewModel.outCode
        in_account.selectedTag = viewModel.inCode
//        Timber.e("out_account.selectedTag: ${out_account.selectedTag}")
//        Timber.e("in_account.selectedTag: ${in_account.selectedTag}")
    }

    private fun initOnclick() {

        val rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        iv_spin.setOnClickListener {
            iv_spin.startAnimation(rotateAnimation)
            isPlatReversed = !isPlatReversed
            setupSelectedTag()
            moveAnim(isPlatReversed)
        }

        layout_balance.btn_refresh.setOnClickListener {
            it.refreshMoneyLoading()
            viewModel.getMoneyAndTransferOut()
        }
        btn_transfer.setOnClickListener {
            et_transfer_money.clearFocus()
            val transferMoneyText = et_transfer_money.getText()
            if (transferMoneyText.isEmpty()) {
                et_transfer_money.setError(getString(R.string.error_input_empty))
                return@setOnClickListener
            }
            if (transferMoneyText == "0") {
                et_transfer_money.setError(getString(R.string.error_input_amount))
                return@setOnClickListener
            }
            viewModel.transfer(isPlatReversed, out_account.selectedTag, in_account.selectedTag, et_transfer_money.getText().toLongOrNull())
        }

        out_account.setOnItemSelectedListener {
            viewModel.outCode = it.code
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
            in_account.excludeSelected("${it.code}")
        }

        in_account.setOnItemSelectedListener {
            viewModel.inCode = it.code
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
            out_account.excludeSelected("${it.code}")
        }
    }

    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) requireActivity().hideSoftKeyboard()
            et_transfer_money.isEnabled = !it
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.subInPlatSheetList.observe(viewLifecycleOwner) {
            in_account.dataList = it
            setupSelectedTag()
        }

        viewModel.subOutPlatSheetList.observe(viewLifecycleOwner) {
            out_account.dataList = it
            setupSelectedTag()
        }

        viewModel.transferResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { it ->
                context?.let { context ->
                    val dialog = CustomAlertDialog(context).apply {
                        setTitle(context.getString(R.string.prompt))
                        setMessage(if (it.success) context.getString(R.string.transfer_money_succeed) else it.msg)
                        setPositiveClickListener { view ->
                            dismiss()
                            if (it.success) {
                                this@MoneyTransferSubFragment.view?.findNavController()
                                    ?.navigate(MoneyTransferSubFragmentDirections.actionMoneyTransferSubFragmentToMoneyTransferFragment())
                            }
                        }
                        setNegativeButtonText(null)
                        setTextColor(if (it.success) R.color.color_909090_666666 else R.color.color_F75452_E23434)
                    }
                    dialog.show(childFragmentManager, null)
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
                in_account.bottomSheetTitleText = getString(R.string.select_plat)
                out_account.bottomSheetTitleText = getString(R.string.select_plat)
                clone(constraint_layout)
                connect(R.id.ll_in,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_in, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_out, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            } else {
                tv_title_in.text = getString(R.string.in_account)
                tv_title_out.text = getString(R.string.out_account)
                in_account.bottomSheetTitleText = getString(R.string.select_plat)
                out_account.bottomSheetTitleText = getString(R.string.select_plat)
                clone(constraint_layout)
                connect(R.id.ll_out,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_out, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_in, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            }
        }
        constraintSet.applyTo(constraint_layout)
    }
}
