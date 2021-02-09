package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

class MoneyTransferRecordFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val rvAdapter by lazy {
        MoneyTransferRecordAdapter(ItemClickListener {

        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {

        viewModel.queryTransfers(1)

        return inflater.inflate(R.layout.fragment_money_transfer_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }


    private fun initView() {
        rv_record.adapter = rvAdapter
    }

    private fun initOnclick() {

    }

    private fun initObserver() {

        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(it.rows)
        }
    }
}