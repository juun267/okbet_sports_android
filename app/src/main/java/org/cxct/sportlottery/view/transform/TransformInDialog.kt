package org.cxct.sportlottery.view.transform

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.DialogTransferMoneyBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.commonCheckDialog
import org.cxct.sportlottery.util.setBtnEnable
import splitties.bundle.put

class TransformInDialog: BaseDialog<MoneyTransferViewModel,DialogTransferMoneyBinding>() {

    companion object{
        fun newInstance(firmType: String,
                         thirdGameResult: EnterThirdGameResult,
                         gameBalance: Double): TransformInDialog{
            val args = Bundle()
            args.apply {
                put("firmType",firmType)
                put("thirdGameResult",thirdGameResult)
                put("gameBalance",gameBalance)
            }
            val fragment = TransformInDialog()
            fragment.arguments = args
            return fragment
        }
    }
    private val firmType by lazy { requireArguments().getString("firmType")!! }
    private val thirdGameResult by lazy { requireArguments().getParcelable<EnterThirdGameResult>("thirdGameResult")!!}
    private val gameBalance by lazy { requireArguments().getDouble("gameBalance") }

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onInitView(){
        (binding.root.layoutParams as ViewGroup.MarginLayoutParams?)?.run {
            val m = 30f.dp
            leftMargin = m
            rightMargin = m
        }

        initView()
        initObserver()
    }


    private fun getUserBalance() = LoginRepository.userMoney.value ?: 0 as Double

    private fun initView()=binding.run{

        tvBanlanceTitle.text = "${resources.getString(R.string.platform_user_money)}:"
        tvGameBanlanceTitle.text = "${resources.getString(R.string.N485)}:"
        tvInputTitle.text = "${resources.getString(R.string.N486)}:"

        val systemCurrencySign = sConfigData?.systemCurrencySign
        val formatedBalance = TextUtil.format(getUserBalance())
        tvBanlance.text = "${systemCurrencySign}${formatedBalance}"
        tvGameBanlance.text = "${systemCurrencySign}${TextUtil.format(gameBalance)}"
        ivClose.setOnClickListener{ dismiss() }
        tvEnter.setOnClickListener { enterGame() }
        edtInput.hint = "0~$formatedBalance"
        edtInput.addTextChangedListener {
            val text = it?.toString()
            if (text.isEmptyStr()) {
                return@addTextChangedListener
            }
            val number = text.toFloatS()
            setInputEffective(number <= getUserBalance())
        }
    }

    private fun setInputEffective(effective: Boolean)=binding.run {
        tvEnter.setBtnEnable(effective)
        edtInput.isSelected = !effective

        if (effective) {
            val color = resources.getColor(R.color.color_025BE8)
            edtInput.setTextColor(color)
            tvError.gone()
            return
        }

        val color = resources.getColor(R.color.color_E23434)
        edtInput.setTextColor(color)
        tvError.visible()
    }

    private fun enterGame()=binding.run {
        var num = edtInput.text.toString().toLongS()
        if (num > 0) {
            loading.visible()
            tvEnter.isEnabled = false
            viewModel.transfer(false, "CG", firmType, num)
            return
        }

        enter()
    }

    private fun initObserver() {

        viewModel.transferResult.observe(this) {
            binding.loading.gone()
            binding.tvEnter.isEnabled = true
            val result = it.getContentIfNotHandled()
            if (true == result?.success) {
                enter()
                return@observe
            }

            ToastUtil.showToast(context, result?.msg)
        }

        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleScope) {
            if (firmType == it.firmType && it.maintain == 1) {

                if (context is BaseActivity<*,*>) {
                    val act = context as BaseActivity<*,*>
                    commonCheckDialog(
                        context = act,
                        fm = act.supportFragmentManager,
                        isError = true,
                        isShowDivider = false,
                        title = act.getString(R.string.prompt),
                        errorMessage = act.getString(R.string.hint_game_maintenance),
                        buttonText = act.getString(R.string.btn_confirm),
                        positiveClickListener = { },
                        negativeText = null,
                    )
                }
                dismiss()
            }
        }
    }

    private fun enter() {
        if (dialog?.isShowing == true) {
            dismiss()
            (requireActivity() as? MainTabActivity)?.enterThirdGame(thirdGameResult,firmType)
        }
    }
}