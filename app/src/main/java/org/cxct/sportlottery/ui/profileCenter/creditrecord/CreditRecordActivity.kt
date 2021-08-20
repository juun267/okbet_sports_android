package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_credit_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.SpaceItemDecoration

class CreditRecordActivity :
    BaseSocketActivity<CreditRecordViewModel>(CreditRecordViewModel::class) {

    private val creditRecordAdapter by lazy {
        CreditRecordAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_record)

        setupCreditRecordList()

        initObserver()
    }

    private fun setupCreditRecordList() {
        credit_record_list.apply {
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    resources.getDimensionPixelSize(R.dimen.item_spacing_credit_record)
                )
            )

            adapter = creditRecordAdapter
        }
    }

    private fun initObserver() {
        viewModel.userCreditCircleHistory.observe(this, {
            creditRecordAdapter.data = it
        })
    }

    override fun onStart() {
        super.onStart()

        viewModel.getCreditRecord()
    }
}