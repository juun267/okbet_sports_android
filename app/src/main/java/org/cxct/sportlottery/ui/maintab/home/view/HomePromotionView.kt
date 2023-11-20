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
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHomePromotionBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.home.HomeBettingStationAdapter
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.promotion.PromotionDetailActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.getMarketSwitch
import splitties.systemservices.layoutInflater
import timber.log.Timber

class HomePromotionView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomePromotionBinding.inflate(layoutInflater, this, true)
    lateinit var viewModel: MainHomeViewModel

    init {
        initView()
    }

    private fun initView() = binding.run {

    }

    fun setup(fragment: HomeHotFragment) = binding.run {
        viewModel = fragment.viewModel
        viewModel.activityImageList.observe(fragment){
            //优惠banne让判断是否首页显示
            val promoteImages=it.filter { it.frontPageShow==1 }
            //优惠活动
            setUpBanner(promoteImages)
        }
        viewModel.getActivityImageListH5()
    }
    private fun setUpBanner(datas: List<ActivityImageList>)=binding.banner.run {
        setHandLoop(false)
        setOnItemClickListener { banner, model, view, position ->
            val jumpUrl = (model as ActivityImageList).imageLink
            if (jumpUrl.isNullOrEmpty()){
                PromotionDetailActivity.start(context, model)
            }else{
                JumpUtil.toInternalWeb(context, jumpUrl,context.getString(R.string.P169))
            }
        }
        loadImage { _, model, view, _ ->
            (view as ImageView).load(sConfigData?.resServerHost+(model as ActivityImageList).xBannerUrl, R.drawable.img_banner01)
        }
        setBannerData(datas.toMutableList())
    }

}