package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_credit_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class CreditRecordActivity :
    BaseSocketActivity<CreditRecordViewModel>(CreditRecordViewModel::class) {

    private val creditRecordAdapter by lazy {
        CreditRecordAdapter()
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount: Int = it.childCount
                    val totalItemCount: Int = it.itemCount
                    val firstVisibleItemPosition: Int =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()

                    viewModel.getCreditRecordNext(
                        visibleItemCount,
                        firstVisibleItemPosition,
                        totalItemCount
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_credit_record)

        setupToolbar()

        setupCreditRecordList()

        initObserver()
    }

    private fun setupToolbar() {

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupCreditRecordList() {
        credit_record_list.apply {

            adapter = creditRecordAdapter

            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initObserver() {
        viewModel.loading.observe(this) {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        }

        viewModel.remainDay.observe(this) {
            credit_record_remain_day.text = String.format(getString(R.string.credit_record_day), it)
        }

        viewModel.userCreditCircleHistory.observe(this) {
            creditRecordAdapter.data = it
        }

        viewModel.quotaAmount.observe(this) {
            credit_record_quota_amount.apply {
                text = it.formatReward

                setTextColor(
                    ContextCompat.getColor(
                        this@CreditRecordActivity, when {
                            (it.reward != null && it.reward > 0) -> R.color.color_08dc6e_08dc6e
                            (it.reward != null && it.reward < 0) -> R.color.color_E44438_e44438
                            else -> R.color.color_909090_666666
                        }
                    )
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getCreditRecord()
    }
}