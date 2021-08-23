package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.os.Bundle
import android.view.View
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
        viewModel.userInfo.observe(this, {
            credit_record_remain_border.visibility = when (it?.creditStatus) {
                1 -> View.VISIBLE
                else -> View.GONE
            }

            credit_record_remain.visibility = when (it?.creditStatus) {
                1 -> View.VISIBLE
                else -> View.GONE
            }

            credit_record_remain_day.visibility = when (it?.creditStatus) {
                1 -> View.VISIBLE
                else -> View.GONE
            }
        })

        viewModel.remainDay.observe(this, {
            credit_record_remain_day.text = String.format(getString(R.string.credit_record_day), it)
        })

        viewModel.userCreditCircleHistory.observe(this, {
            creditRecordAdapter.data = it
        })
    }

    override fun onStart() {
        super.onStart()

        viewModel.getCreditRecord()
    }
}