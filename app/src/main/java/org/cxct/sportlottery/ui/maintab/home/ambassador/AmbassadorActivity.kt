package org.cxct.sportlottery.ui.maintab.home.ambassador

import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ActivityAmbassadorBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SpaceItemDecoration


class AmbassadorActivity : BaseActivity<MainHomeViewModel, ActivityAmbassadorBinding>() {
    override fun pageName() = "代言人资料页面"
    companion object{
        const val KEY_AMBASSADOR = "Ambassador"
    }
    private val item by lazy { intent.getParcelableExtra(KEY_AMBASSADOR) as AmbassadorInfo.Ambassador?}
    private val bannerAdapter = AmbassadorBannerAdapter()

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.customToolBar.setOnBackPressListener { finish() }
        binding.bottomView.bindServiceClick(supportFragmentManager)
        binding.bottomView.binding.endView.setPadding(0,0,0,15.dp)
        item?.let {
            setUpView(it)
        }
    }
    private fun setUpView(data: AmbassadorInfo.Ambassador){
        binding.ivTopBanner.setImageResource(data.topBanner)
        binding.ivInfo.setImageResource(data.infoPic)
        binding.tvInfo.text = getString(data.infoDetail)
        binding.tvDesp.text = getString(data.summary)
        binding.okWebView.bindLifecycleOwner(this)
        binding.okWebView.loadUrl(data.videoUrl)
        initBottomBanner(data.bottomBanner)
    }
    private fun initBottomBanner(imageResList: List<Int>)=binding.run{
        banner.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        banner.addItemDecoration(SpaceItemDecoration(this@AmbassadorActivity, R.dimen.margin_14))
        bannerAdapter.setList(imageResList)
        bannerAdapter.setOnItemClickListener{ adapter, view, position ->
            AmbassadorImageDialog.newInstance(ArrayList(imageResList),position).show(supportFragmentManager)
        }
        banner.adapter = bannerAdapter
        banner.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = banner.layoutManager as LinearLayoutManager
                    ivLeftArrow.setArrowIconEnabled(lm.findFirstVisibleItemPosition() >0)
                    ivRightArrow.setArrowIconEnabled(lm.findLastVisibleItemPosition() < bannerAdapter.itemCount-1)
                }
            }
        })
        ivLeftArrow.setArrowIconEnabled(false)
        ivLeftArrow.setOnClickListener {
            val targetPosition = (banner.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()-1
            banner.smoothScrollToPosition(targetPosition)
        }
        ivRightArrow.setOnClickListener {
            val targetPosition = (banner.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()+1
            banner.smoothScrollToPosition(targetPosition)
        }
    }
    private fun ImageView.setArrowIconEnabled(enabled: Boolean){
        isEnabled = enabled
        alpha = if(enabled) 1.0f else 0.2f
    }


}