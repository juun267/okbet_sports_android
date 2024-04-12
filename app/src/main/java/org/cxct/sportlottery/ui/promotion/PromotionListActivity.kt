package org.cxct.sportlottery.ui.promotion

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityCategory
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper


class PromotionListActivity : BaseActivity<MainHomeViewModel, ActivityPromotionListBinding>(),
    OnItemClickListener {

    private val adapter by lazy { PromotionAdapter().apply { setOnItemClickListener(this@PromotionListActivity) } }
    private val loadingHolder by lazy { Gloading.wrapView(binding.rvPromotion) }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        bindFinish(binding.customToolBar)
        binding.rvPromotion.setLinearLayoutManager()
        binding.rvPromotion.adapter = adapter
        initTab()
        loadingHolder.withRetry { viewModel.getActivityCategoryList() }
        loadingHolder.go()

        initObserver()
    }

    private fun initObserver() = viewModel.run {
        activityList.observe(this@PromotionListActivity) {
            val activityList = it.second
            if (activityList == null) {
                loadingHolder.showLoadFailed()
                toast(it.first ?: getString(R.string.N655))
                return@observe
            }

            if (activityList.isEmpty()) {
                loadingHolder.showEmpty()
                return@observe
            }

            binding.tabLayout.visible()
            binding.divider.visible()
            adapter.setList(activityList)
            loadingHolder.showLoadSuccess()
        }

        activityCategroyList.observe(this@PromotionListActivity) {
            val list = it.second
            if (list == null) {
                loadingHolder.showLoadFailed()
                toast(it.first ?: getString(R.string.N655))
                return@observe
            }

            if (list.isEmpty()) {
                setupData(listOf())
                return@observe
            }

            setupData(it.second!!)
        }

    }

    private fun initTab() = binding.tabLayout.run {
        OverScrollDecoratorHelper.setUpOverScroll(this)
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val textView = tab.customView as TextView
                textView.setTextColor(Color.WHITE)
                textView.textSize = 16f
                textView.background = ShapeDrawable()
                    .setRadius(51.dp.toFloat())
                    .setSolidColor(getColor(R.color.color_025BE8))
                onTabChanged(textView.tag as ActivityCategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val textView = tab.customView as TextView
                textView.setTextColor(getColor(R.color.color_6D7693))
                textView.textSize = 14f
                textView.background = null
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })
    }

    private fun setupData(list: List<ActivityCategory>) {
        val categoryList = list.toMutableList()
        categoryList.add(0, ActivityCategory(-1, -1, getString(R.string.label_all)))

        val lp = LinearLayout.LayoutParams(-2, -1)
        lp.gravity = Gravity.CENTER
        val dp11 = 11.dp
        val dp74 = 74.dp
        categoryList.forEach {
            val textView = AppCompatTextView(this)
            textView.setPadding(dp11, 0, dp11, 0)
            textView.layoutParams = lp
            textView.minWidth = dp74
            textView.gravity = Gravity.CENTER
            textView.typeface = AppFont.helvetica
            textView.text = it.name
            textView.tag = it
            val tab = binding.tabLayout.newTab()
            tab.setCustomView(textView)
            binding.tabLayout.addTab(tab)
        }
    }

    override fun onItemClick(baseQuickAdapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val itemData = adapter.getItem(position)
        if (itemData.imageLink.isNullOrEmpty()) {
            PromotionDetailActivity.start(this@PromotionListActivity, itemData)
        } else {
            JumpUtil.toInternalWeb(this@PromotionListActivity, itemData.imageLink,getString(R.string.P169))
        }
    }

    private fun onTabChanged(item: ActivityCategory) {
        adapter.setList(null)
        loadingHolder.withRetry{
            loadingHolder.showLoading()
            viewModel.getActivityList(if (item.id > 0) item.id else null)
        }
        loadingHolder.go()
    }

}