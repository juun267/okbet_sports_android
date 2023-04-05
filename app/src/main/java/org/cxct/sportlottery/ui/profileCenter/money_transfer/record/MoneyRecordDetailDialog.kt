package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_money_transfer_record_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.*

class MoneyRecordDetailDialog : BaseDialog<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_money_transfer_record_detail, container, false)
    }

    private var data :Row? = null
    get() {
        return arguments?.getParcelable("data")
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data?.apply {
            log_detail_status_subtitle.text = "${getString(R.string.transfer_money)}：${showCurrencySign}"
            log_detail_amount_subtitle.text = "${getString(R.string.in_account)}："
            log_detail_type_subtitle.text = "${getString(R.string.out_account)}："
            tv_order_number.text = orderNo
            tv_datetime.setDateTime(addTime)
            tv_out_account.setPlatName(firmTypeOut)
            tv_in_account.setPlatName(firmTypeIn)
            tv_money.setMoneyFormat(money)
            tv_state.setRecordStatus(status)
            tv_state.setRecordStatusColor(status)
            tv_remark.text = remark
        }

        btn_ok.setOnClickListener {
            dismiss()
        }
    }

}