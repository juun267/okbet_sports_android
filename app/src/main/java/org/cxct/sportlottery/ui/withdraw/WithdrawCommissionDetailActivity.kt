package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_withdraw_commission_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.TextUtil

class WithdrawCommissionDetailActivity :
    BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    private val commissionDetailAdapter = CommissionDetailAdapter()

    private val zero = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_commission_detail)

        initView()
        initObserve()
        setupBackButton()
        setupData()
    }

    private fun initView() {
        tv_time.text = viewModel.getNowTime()

        btn_back.setOnClickListener {
            finish()
        }

        rv_detail.apply {
            adapter = commissionDetailAdapter
            layoutManager = LinearLayoutManager(this@WithdrawCommissionDetailActivity)
        }

        btn_info.setOnClickListener {
            CommissionDetailInfoDialog(this).show()
        }

        tv_currency.text = sConfigData?.systemCurrency
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun initObserve() {
        viewModel.deductMoney.observe(this, {
            tv_total.apply {
                text = TextUtil.formatCommissionMoney(zero.minus(it ?: 0.0))
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (zero.minus(it ?: 0.0) > 0) R.color.colorGreen else R.color.colorRed
                    )
                )
            }
        })

        viewModel.commissionCheckList.observe(this, {
            commissionDetailAdapter.setData(it)
        })

        viewModel.loading.observe(this, {
            if (it)
                loading()
            else
                hideLoading()
        })
    }

    private fun setupData() {
        viewModel.apply {
            getUwCheck()
        }
    }

}