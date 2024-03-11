package org.cxct.sportlottery.ui.money.withdraw

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityWithdrawCommissionDetailBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*
/**
 * @app_destination 提款-提款详情
 */
class WithdrawCommissionDetailActivity :
    BaseActivity<WithdrawViewModel, ActivityWithdrawCommissionDetailBinding>() {

    private val commissionDetailAdapter = CommissionDetailAdapter()

    private val zero = 0.0

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        initView()
        setupBackButton()
        initObserve()
        setupData()
    }

    private fun initView()=binding.run {
        tvTime.text = TimeUtil.dateToFormat(Date())

        rvDetail.apply {
            adapter = commissionDetailAdapter
            layoutManager = LinearLayoutManager(this@WithdrawCommissionDetailActivity)
        }

        btnInfo.setOnClickListener {
            CommissionDetailInfoDialog().show(supportFragmentManager)
        }

        tvCurrency.text = sConfigData?.systemCurrencySign
    }

    private fun setupBackButton() {
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
    }

    private fun initObserve() {
        viewModel.deductMoney.observe(this) {
            binding.tvTotal.apply {
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
            commissionDetailAdapter.setList(it)
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