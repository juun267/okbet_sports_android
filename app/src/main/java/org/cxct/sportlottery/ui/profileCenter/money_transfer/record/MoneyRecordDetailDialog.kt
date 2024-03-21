package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogMoneyTransferRecordDetailBinding
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

class MoneyRecordDetailDialog : BaseDialog<MoneyTransferViewModel,DialogMoneyTransferRecordDetailBinding>() {

    companion object{
        fun newInstance(data: Row) = MoneyRecordDetailDialog().apply {
            arguments = Bundle().apply {
              putParcelable("data",data)
            }
        }
    }
    init {
        marginHorizontal = 40.dp
    }
    private val data by  lazy { requireArguments().getParcelable<Row>("data")!! }

    override fun onInitView()=binding.run {
        data.apply {
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