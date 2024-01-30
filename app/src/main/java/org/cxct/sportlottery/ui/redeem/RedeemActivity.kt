package org.cxct.sportlottery.ui.redeem

import android.os.Bundle
import androidx.core.text.toSpanned
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import kotlinx.android.synthetic.main.activity_redeem.rvRedeem
import kotlinx.android.synthetic.main.activity_redeem.viewNoData
import kotlinx.android.synthetic.main.activity_withdraw_commission_detail.custom_tool_bar
import kotlinx.android.synthetic.main.component_date_range_new_selector.view.btn_search
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.databinding.ActivityRedeemBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.TextUtil

class RedeemActivity : BaseSocketActivity<RedeemViewModel>(RedeemViewModel::class) {


    private lateinit var binding: ActivityRedeemBinding
    val redeemAdapter by lazy {
        RedeemAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemBinding.inflate(layoutInflater)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        initView()
        viewModel.newsResult.observe(this) {
            if (it.success) {
                it.entity?.let { entity ->
                    val color = getColor(R.color.color_535D76)
                    val msg = getString(R.string.P170).setSpan(ColorSpan(color))
                        .addSpan(" ₱${entity.rewards?.let { it1 -> TextUtil.format(it1) }}", ColorSpan(getColor(R.color.color_025BE8)))
                        .addSpan(" !", ColorSpan(color)).toSpanned()

                    showPromptDialog(title = getString(R.string.Congratulations),
                        message = msg,
                        positiveClickListener = {})
                }
            } else {
                showPromptDialog(title = resources.getString(R.string.N592),
                    errorMessage = it.msg,
                    buttonText = null,
                    positiveClickListener = {},
                    isError = true)
            }
            hideLoading()
        }
        viewModel.codeHistory.observe(this) {
            if (viewModel.page == 1) {
                redeemAdapter.data.clear()
            }
            it.rows?.let { it1 -> redeemAdapter.addData(it1) }
            viewNoData.isVisible = redeemAdapter.data.isEmpty()
        }
    }


    private fun initView() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
        binding.btnReset.setOnClickListener {
            binding.etRedeemCode.setText("")

        }
        binding.btnSubmit.setOnClickListener {
            val redemmCode = binding.etRedeemCode.text.toString()
            if (redemmCode.isNotEmpty()) {
                loading()
                viewModel.redeemCode(redemmCode)
            }
        }
        binding.rbtnRedeem.setOnClickListener {
            binding.layoutRedemm.isVisible = true
            binding.lineRedeem.isVisible = true
            binding.lineRedeemHis.isVisible = false
            binding.layoutHistoryRedeem.isVisible = false
        }
        binding.rbtnRedeemHis.setOnClickListener {
            viewModel.redeemCodeHistory(
                startTime = binding.dateSearchBar.startTime.toString(),
                endTime = binding.dateSearchBar.endTime.toString(),
                page = 1
            )
            binding.layoutRedemm.isVisible = false
            binding.lineRedeem.isVisible = false
            binding.lineRedeemHis.isVisible = true
            binding.layoutHistoryRedeem.isVisible = true
        }

        binding.dateSearchBar.btn_search.clickDelay {
            viewModel.redeemCodeHistory(
                startTime = binding.dateSearchBar.startTime.toString(),
                endTime = binding.dateSearchBar.endTime.toString(),
                page = 1
            )
        }

        rvRedeem.adapter = redeemAdapter
        rvRedeem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    viewModel.page++
                    viewModel.redeemCodeHistory(
                        startTime = binding.dateSearchBar.startTime.toString(),
                        endTime = binding.dateSearchBar.endTime.toString(),
                        page = viewModel.page
                    )
                }
            }
        })
    }

}