package org.cxct.sportlottery.ui.promotion

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.JumpUtil


class PromotionListActivity : BaseActivity<MainHomeViewModel, ActivityPromotionListBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.rvPromotion.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        viewModel.activityImageList.observe(this){
             setListData(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getActivityImageListH5()
    }
    fun setListData(activityDatas: List<ActivityImageList>){
        if (binding.rvPromotion.adapter==null){
            binding.rvPromotion.adapter = PromotionAdapter().apply {
            setList(activityDatas)
            setOnItemClickListener { adapter, view, position ->
                val itemData = data[position]
                if (itemData.imageLink.isNullOrEmpty()){
                    PromotionDetailActivity.start(this@PromotionListActivity, itemData)
                }else{
                    JumpUtil.toInternalWeb(this@PromotionListActivity, itemData.imageLink,getString(R.string.P169))
                }
            }
        }
        }else{
            (binding.rvPromotion.adapter as PromotionAdapter).setList(activityDatas)
        }
    }
}