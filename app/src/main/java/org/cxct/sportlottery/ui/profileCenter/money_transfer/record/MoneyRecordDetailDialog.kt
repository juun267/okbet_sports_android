package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogMoneyTransferRecordDetailBinding
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.*

class MoneyRecordDetailDialog : BaseDialog<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }
    private val binding by lazy { DialogMoneyTransferRecordDetailBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private var data :Row? = null
    get() {
        return arguments?.getParcelable("data")
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =binding.run{
        super.onViewCreated(view, savedInstanceState)

        data?.apply {
            logDetailStatusSubtitle.text = "${getString(R.string.transfer_money)}：${showCurrencySign}"
            logDetailAmountSubtitle.text = "${getString(R.string.in_account)}："
            logDetailTypeSubtitle.text = "${getString(R.string.out_account)}："
            tvOrderNumber.text = orderNo
            tvDatetime.setDateTime(addTime)
            tvOutAccount.setPlatName(firmTypeOut)
            tvInAccount.setPlatName(firmTypeIn)
            tvMoney.setMoneyFormat(money)
            tvState.setRecordStatus(status)
            tvState.setRecordStatusColor(status)
            tvRemark.text = remark
        }

        btnOk.setOnClickListener {
            dismiss()
        }
    }

}