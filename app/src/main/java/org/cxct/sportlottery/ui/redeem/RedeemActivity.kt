package org.cxct.sportlottery.ui.redeem

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_redeem.rvRedeem
import kotlinx.android.synthetic.main.activity_withdraw_commission_detail.custom_tool_bar
import kotlinx.android.synthetic.main.component_date_range_new_selector.view.btn_search
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityRedeemBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.view.dialog.RedeemDialog

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
                    showRedeemDialog("You got P${entity.rewards} !", "Congratulations")
                }
            } else {
                showRedeemDialog(it.msg, "Sorry")
            }
            hideLoading()
        }
        viewModel.codeHistory.observe(this) {
            if (viewModel.page == 1) {
                redeemAdapter.data.clear()
            }
            it.rows?.let { it1 -> redeemAdapter.addData(it1) }
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
            loading()
            viewModel.redeemCode(redemmCode)

        }
        binding.rbtnRedeem.setOnClickListener {
            binding.layoutRedemm.isVisible = true
            binding.lineRedeem.isVisible = true
            binding.lineRedeemHis.isVisible = false
            binding.layoutHistoryRedeem.isVisible = false
        }
        binding.rbtnRedeemHis.setOnClickListener {
            viewModel.redeemCodeHistory()
            binding.layoutRedemm.isVisible = false
            binding.lineRedeem.isVisible = false
            binding.lineRedeemHis.isVisible = true
            binding.layoutHistoryRedeem.isVisible = true
        }

        binding.dateSearchBar.btn_search.setOnClickListener {
            avoidFastDoubleClick()
            viewModel.page = 1
            viewModel.redeemCodeHistory(
                startTime = binding.dateSearchBar.startTime.toString(),
                endTime = binding.dateSearchBar.endTime.toString(),
                page = viewModel.page
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

    private fun showRedeemDialog(msg: String, title: String) {
        val dialog = RedeemDialog(this)
        dialog.setContentMsg(msg)
        dialog.setTitle(title)
        dialog.show()
    }

}