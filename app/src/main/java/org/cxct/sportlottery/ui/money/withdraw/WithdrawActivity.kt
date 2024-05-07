package org.cxct.sportlottery.ui.money.withdraw

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityWithdrawBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 提款
 */
class WithdrawActivity : BaseActivity<WithdrawViewModel, ActivityWithdrawBinding>() {

    private lateinit var marqueeAdapter: MarqueeAdapter

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        initToolbar()
        initMarquee()
        initObserver()
    }

    override fun onInitData() {
        viewModel.getAnnouncement()
    }

    private fun initToolbar() =binding.toolBar.run{
        tvToolbarTitle.setTitleLetterSpacing()
        tvToolbarTitle.text = getString(R.string.withdraw)
        btnToolbarBack.setOnClickListener {
            finish()
        }

        tvToolbarTitleRight.setOnClickListener {
            startActivity(BankActivity::class.java)
        }
        tvToolbarTitleRight.isVisible = viewModel.isVisibleView.value ?: true
        tvToolbarTitleRight.text = getString(R.string.withdraw_setting)
    }

    private fun initMarquee() {
        binding.rvMarquee.bindLifecycler(this)
        val rvMarquee = binding.rvMarquee
        rvMarquee.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        marqueeAdapter = object : MarqueeAdapter() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val viewHolder = super.onCreateViewHolder(viewGroup, viewType)
                if (viewHolder is MarqueeVH) {
                    viewHolder.textView.setTextColor(getColor(R.color.color_313F56))
                }
                return viewHolder
            }
        }

        rvMarquee.adapter = marqueeAdapter
    }

    private fun initObserver() {
        viewModel.isVisibleView.observe(this) {
            binding.toolBar.tvToolbarTitleRight.isVisible = it
        }

        viewModel.withdrawAnnouncement.observe(this) {
            marqueeAdapter.setData(it)
            val haveData = it.isNotEmpty()
            binding.linAnnouncement.isVisible = haveData
            if (haveData) {
                binding.rvMarquee.startAuto(false)
            }
        }
    }

}