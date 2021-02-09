package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_money_transfer_record_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.bet.record.search.result.setDateTime
import org.cxct.sportlottery.ui.bet.record.search.result.setMoneyFormat
import org.cxct.sportlottery.ui.bet.record.search.result.setPlatName
import org.cxct.sportlottery.ui.bet.record.search.result.setRecordStatus
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

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

    var data :Row? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data?.apply {
            tv_order_number.text = orderNo
            tv_datetime.setDateTime(addTime)
            tv_out_account.setPlatName(firmTypeOut)
            tv_in_account.setPlatName(firmTypeIn)
            tv_money.setMoneyFormat(money)
            tv_state.setRecordStatus(status)
            tv_remark.text = remark
        }

        btn_ok.setOnClickListener {
            dismiss()
        }
    }

/*
    fun setData(data: Row) {
        data.apply {
            tv_order_number.text = orderNo
            tv_datetime.setDateTime(addTime)
            tv_out_account.text = firmTypeOut
            tv_in_account.text = firmTypeIn
            tv_money.setMoneyFormat(money)
            tv_state.setRecordStatus(status)
            tv_remark.text = remark
        }
    }
    */
}