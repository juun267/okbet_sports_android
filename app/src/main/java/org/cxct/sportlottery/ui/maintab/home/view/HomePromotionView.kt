package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ViewHomePromotionBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.home.HomeBettingStationAdapter
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.promotion.PromotionDetailActivity
import org.cxct.sportlottery.util.JumpUtil
import splitties.systemservices.layoutInflater

class HomePromotionView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomePromotionBinding.inflate(layoutInflater, this, true)
    lateinit var viewModel: MainHomeViewModel
    val promoteAdapter = object : BaseQuickAdapter<ActivityImageList, BaseViewHolder>(R.layout.item_promote_view) {
            override fun convert(holder: BaseViewHolder, item: ActivityImageList) {
                val view = holder.getView<ImageView>(R.id.ivItemPromote)
                view.load(sConfigData?.resServerHost+item.indexImage, R.drawable.img_banner01)
            }
        }

    init {
        initView()
    }

    private fun initView() = binding.run {
        rcvPromote.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            adapter = promoteAdapter
            promoteAdapter.setOnItemClickListener{ adapter, view, position ->
                val itemData = promoteAdapter.getItem(position)
                if (itemData.imageLink.isNullOrEmpty()){
                    PromotionDetailActivity.start(context, itemData)
                }else{
                    JumpUtil.toInternalWeb(context, itemData.imageLink,context.getString(R.string.P169))
                }
            }
            if (onFlingListener == null) {
                PagerSnapHelper().attachToRecyclerView(binding.rcvPromote)
            }
        }
    }

    fun setup(fragment: HomeHotFragment) = binding.run {
        viewModel = fragment.viewModel
        viewModel.activityImageList.observe(fragment){
            //优惠banne让判断是否首页显示
            val promoteImages=it.filter { it.frontPageShow==1 }
            //优惠活动
            promoteAdapter.setList(promoteImages)
        }
        viewModel.getActivityImageListH5()
    }

}