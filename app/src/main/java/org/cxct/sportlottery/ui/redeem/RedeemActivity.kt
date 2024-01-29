package org.cxct.sportlottery.ui.redeem

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityRedeemBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.dialog.RedeemDialog

class RedeemActivity : BaseSocketActivity<RedeemViewModel,ActivityRedeemBinding>(RedeemViewModel::class) {

    val redeemAdapter by lazy {
        RedeemAdapter()
    }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        initView()
        viewModel.newsResult.observe(this) {
            if (it.success) {
                it.entity?.let { entity ->
                    var str = getString(R.string.P170)
                    showRedeemDialog(
                        "$str ₱${entity.rewards?.let { it1 -> TextUtil.format(it1) }} !",
                        getString(R.string.Congratulations)
                    )
                }
            } else {
                showRedeemDialog(it.msg, resources.getString(R.string.N592))
            }
            hideLoading()
        }
        viewModel.codeHistory.observe(this) {
            if (viewModel.page == 1) {
                redeemAdapter.data.clear()
            }
            it.rows?.let { it1 -> redeemAdapter.addData(it1) }
            binding.viewNoData.root.isVisible = redeemAdapter.data.isEmpty()
        }
    }

    private fun initView() {
        binding.customToolBar.setOnBackPressListener {
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

        binding.dateSearchBar.setOnClickSearchListener {
            viewModel.redeemCodeHistory(
                startTime = binding.dateSearchBar.startTime.toString(),
                endTime = binding.dateSearchBar.endTime.toString(),
                page = 1
            )
        }

        binding.rvRedeem.adapter = redeemAdapter
        binding.rvRedeem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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