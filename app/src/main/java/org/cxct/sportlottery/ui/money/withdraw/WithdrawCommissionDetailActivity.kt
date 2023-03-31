package org.cxct.sportlottery.ui.money.withdraw

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_withdraw_commission_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*
/**
 * @app_destination 提款-提款详情
 */
class WithdrawCommissionDetailActivity :
    BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    private val commissionDetailAdapter = CommissionDetailAdapter()

    private val zero = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_withdraw_commission_detail)

        initView()
        setupBackButton()
        initObserve()
        setupData()
    }

    private fun initView() {
        tv_time.text = TimeUtil.dateToFormat(Date())

        rv_detail.apply {
            adapter = commissionDetailAdapter
            layoutManager = LinearLayoutManager(this@WithdrawCommissionDetailActivity)
        }

        btn_info.setOnClickListener {
            CommissionDetailInfoDialog().show(supportFragmentManager, null)
        }

        tv_currency.text = sConfigData?.systemCurrencySign
    }

    private fun setupBackButton() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    private fun initObserve() {
        viewModel.deductMoney.observe(this) {
            tv_total.apply {
                text = TextUtil.formatMoney(zero.minus(it ?: 0.0))
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (zero.minus(it ?: 0.0) > 0) R.color.color_08dc6e_08dc6e else R.color.color_E44438_e44438
                    )
                )
            }
        }

        viewModel.commissionCheckList.observe(this) {
            commissionDetailAdapter.setData(it)
        }

        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }
    }

    private fun setupData() {
        viewModel.apply {
            getUwCheck()
        }
    }

}