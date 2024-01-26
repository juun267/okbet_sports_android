package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_main_right.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentMoneyTransferSubBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.refreshMoneyLoading
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber
import java.math.BigDecimal


class MoneyTransferSubFragment : BindingFragment<MoneyTransferViewModel,FragmentMoneyTransferSubBinding>() {

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()
    private var isPlatReversed = false
    private var gameMoney = 0.0 //第三方遊戲餘額

    private var useDefaultUnit = true
    private val defaultUnit = 1.0 //預設unit
    private val thirdTransferUnit = sConfigData?.thirdTransferUnit

    override fun onInitView(view: View) {
        viewModel.setToolbarName(getString(R.string.transfer_info))
        viewModel.showTitleBar(false)
        initView()
        initOnclick()
        initObserver()
        viewModel.getAllBalance()
    }

    private fun initView()=binding.run{

        tvBanlance.setText(R.string.title_current_money)
        moveAnim(isPlatReversed)
        viewModel.initCode()
        //region 靠左置中＆移除padding
        outAccount.viewBinding.tvSelected.run {
            gravity = Gravity.START or Gravity.CENTER
            setPadding(0, 0, 0, 0)
        }
        inAccount.viewBinding.tvSelected.run {
            gravity = Gravity.START or Gravity.CENTER
            setPadding(0, 0, 0, 0)
        }
        //endregion
        outAccount.selectedText = getString(R.string.plat_money)
        inAccount.selectedText = gameDataArg.gameData.showName
        gameMoney = gameDataArg.gameData.money ?: 0.0
        layoutBalance.tvCurrencyType.text = sConfigData?.systemCurrencySign
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, gameDataArg.gameData.showName)
        viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, getString(R.string.plat_money))
        etTransferMoney.afterTextChanged {
            if (it.isEmptyStr()) {
                etTransferMoney.setError("")
                return@afterTextChanged
            }

            val enable = it.toBigDecimal() > BigDecimal(0)
            btnTransfer.isEnabled = enable
            if (enable) {
                etTransferMoney.setError("")
            } else {
                etTransferMoney.setError(getString(R.string.error_input_amount))
            }
        }
        btnTransfer.setTitleLetterSpacing()
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
        etTransferMoney.apply {
            setMaxLength(20)
            setHint(hint)
            binding.apply {
                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                tvTitle.setTextColor(resources.getColor(R.color.color_535D76))
                tvTitle.setTypeface(Typeface.DEFAULT)
                etInput.minHeight = 50.dp
                vBottomLine.visibility = View.INVISIBLE
                btnClear.setImageResource(R.drawable.ic_clear_gray)
                btnClear.setPadding(2, 2, 2, 2)
            }
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
        binding.outAccount.selectedTag = viewModel.outCode
        binding.inAccount.selectedTag = viewModel.inCode
//        Timber.e("outAccount.selectedTag: ${outAccount.selectedTag}")
//        Timber.e("inAccount.selectedTag: ${inAccount.selectedTag}")
    }

    private fun initOnclick()=binding.run {

        val rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        ivSpin.setOnClickListener {
            ivSpin.startAnimation(rotateAnimation)
            isPlatReversed = !isPlatReversed
            setupSelectedTag()
            moveAnim(isPlatReversed)
        }

        layoutBalance.btnRefresh.setOnClickListener {
            it.refreshMoneyLoading()
            viewModel.getMoneyAndTransferOut()
        }
        btnTransfer.setOnClickListener {
            etTransferMoney.clearFocus()
            val transferMoneyText = etTransferMoney.getText()
            if (transferMoneyText.isEmpty()) {
                etTransferMoney.setError(getString(R.string.error_input_empty))
                return@setOnClickListener
            }
            if (transferMoneyText == "0") {
                etTransferMoney.setError(getString(R.string.error_input_amount))
                return@setOnClickListener
            }
            viewModel.transfer(isPlatReversed, outAccount.selectedTag, inAccount.selectedTag, etTransferMoney.getText().toLongOrNull())
        }

        outAccount.setOnItemSelectedListener {
            viewModel.outCode = it.code
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
            inAccount.excludeSelected("${it.code}")
        }

        inAccount.setOnItemSelectedListener {
            viewModel.inCode = it.code
            viewModel.filterSubList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
            outAccount.excludeSelected("${it.code}")
        }
    }

    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) requireActivity().hideSoftKeyboard()
            binding.etTransferMoney.isEnabled = !it
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                binding.layoutBalance.tvAccountBalance.text = TextUtil.format(it)
            }
        }

        viewModel.subInPlatSheetList.observe(viewLifecycleOwner) {
            binding.inAccount.dataList = it
            setupSelectedTag()
        }

        viewModel.subOutPlatSheetList.observe(viewLifecycleOwner) {
            binding.outAccount.dataList = it
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

    private fun moveAnim(isReversed: Boolean)=binding.run {

        val constraintSet = ConstraintSet()

        constraintSet.apply {
            if (isReversed) {
                tvTitleIn.text = getString(R.string.out_account)
                tvTitleOut.text = getString(R.string.in_account)
                inAccount.bottomSheetTitleText = getString(R.string.select_plat)
                outAccount.bottomSheetTitleText = getString(R.string.select_plat)
                clone(constraintLayout)
                connect(R.id.ll_in,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_in, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_out, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            } else {
                tvTitleIn.text = getString(R.string.in_account)
                tvTitleOut.text = getString(R.string.out_account)
                inAccount.bottomSheetTitleText = getString(R.string.select_plat)
                outAccount.bottomSheetTitleText = getString(R.string.select_plat)
                clone(constraintLayout)
                connect(R.id.ll_out,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    10.dp)
                connect(R.id.iv_spin, ConstraintSet.TOP, R.id.ll_out, ConstraintSet.BOTTOM, 10.dp)
                connect(R.id.ll_in, ConstraintSet.TOP, R.id.iv_spin, ConstraintSet.BOTTOM, 10.dp)
            }
        }
        constraintSet.applyTo(constraintLayout)
    }
}
