package org.cxct.sportlottery.ui.common.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.*
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable

class TransformInDialog(val firmType: String,
                        val thirdGameResult: EnterThirdGameResult,
                        val gameBalance: Double,
                        val callback:(EnterThirdGameResult) -> Unit):
    BaseDialog<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.dialog_transfer_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //不分手机上弹窗宽度会撑满，需重新设置下左右间距
        (view.layoutParams as ViewGroup.MarginLayoutParams?)?.run {
            val m = 30f.dp
            leftMargin = m
            rightMargin = m
        }

        initView()
        initObserver()
    }

    private fun getUserBalance() = LoginRepository.userMoney.value ?: 0 as Double

    private fun initView() {

        tvBanlanceTitle.text = "${resources.getString(R.string.platform_user_money)}:"
        tvGameBanlanceTitle.text = "${resources.getString(R.string.game_account_money)}:"
        tvInputTitle.text = "${resources.getString(R.string.transform_in_amount)}:"

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

    private fun setInputEffective(effective: Boolean) {
        tvEnter.setBtnEnable(effective)
        edtInput.isSelected = !effective

        if (effective) {
            val color = resources.getColor(R.color.color_535D76)
            edtInput.setTextColor(color)
            tvError.gone()
            return
        }

        val color = resources.getColor(R.color.color_E23434)
        edtInput.setTextColor(color)
        tvError.visible()
    }

    private fun enterGame() {
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
            loading.gone()
            tvEnter.isEnabled = true
            val result = it.getContentIfNotHandled()
            if (true == result?.success) {
                enter()
                return@observe
            }

            ToastUtil.showToast(context, result?.msg)
        }
    }

    private fun enter() {
        dismiss()
        callback.invoke(thirdGameResult)
    }
}