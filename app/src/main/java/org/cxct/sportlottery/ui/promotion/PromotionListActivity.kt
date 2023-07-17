package org.cxct.sportlottery.ui.promotion

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


class PromotionListActivity : BindingActivity<MainHomeViewModel, ActivityPromotionListBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.rvPromotion.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        viewModel.getActivityImageListH5()
        viewModel.activityImageList.observe(this){
             setListData(it)
        }
    }
    fun setListData(activityDatas: List<ActivityImageList>){
        binding.rvPromotion.adapter = PromotionAdapter().apply {
            setList(activityDatas)
            setOnItemClickListener { adapter, view, position ->
                PromotionDetailActivity.start(this@PromotionListActivity, activityDatas[position])
            }
        }
    }
}