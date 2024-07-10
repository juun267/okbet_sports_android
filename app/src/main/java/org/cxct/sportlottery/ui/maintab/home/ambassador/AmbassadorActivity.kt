package org.cxct.sportlottery.ui.maintab.home.ambassador

import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import kotlinx.android.synthetic.main.activity_ambassador.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ActivityAmbassadorBinding
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.SpaceItemDecoration


class AmbassadorActivity : VideoControlActivityImp<MainHomeViewModel, ActivityAmbassadorBinding>() {
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
        initVideoPlay(data.videoUrl,binding.player)
        initBottomBanner(data.bottomBanner)
        okWebView.loadUrl(data.videoUrl)
    }
    private fun initBottomBanner(imageResList: List<Int>)=binding.run{
        banner.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        banner.addItemDecoration(SpaceItemDecoration(this@AmbassadorActivity, R.dimen.margin_14))
        bannerAdapter.setList(imageResList)
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
            banner.smoothScrollToPosition(0)
        }
        ivRightArrow.setOnClickListener {
            banner.smoothScrollToPosition(imageResList.size-1)
        }
    }
    private fun initVideoPlay(url: String?, player: StandardGSYVideoPlayer) {
        if (url == null) {
            return
        }
//        val ivLiveSound = viewBinding.ivLiveSound
//        LogUtil.d("streamUrl=$streamUrl")
//        ivLiveSound.isSelected = true
        GSYVideoManager.instance().isNeedMute = true
//        ivLiveSound.setOnClickListener {
//            it.isSelected = !it.isSelected
//            GSYVideoManager.instance().isNeedMute = !ivLiveSound.isSelected
//        }
        player.titleTextView.gone()
        player.backButton.gone()
        player.fullscreenButton.gone()
        initVideoBuilderMode();
    }
    private fun ImageView.setArrowIconEnabled(enabled: Boolean){
        isEnabled = enabled
        alpha = if(enabled) 1.0f else 0.2f
    }

    override val gSYVideoPlayer: GSYBaseVideoPlayer
        get() = binding.player
    override val gSYVideoOptionBuilder: GSYVideoOptionBuilder
        get() {
            val coverView = AppCompatImageView(this)
             coverView.load(item?.videoCover)
            GSYVideoManager.instance().clearAllDefaultCache(this)
            return GSYVideoOptionBuilder()
                .setThumbImageView(coverView)
                .setUrl(item?.videoUrl)
                .setCacheWithPlay(false)
                .setVideoTitle(" ")
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1.0f)
        }
    override fun clickForFullScreen() {

    }
    override fun onVideoPrepared(): Unit =binding.run {
        player.layoutParams.apply {
            LogUtil.d(player.currentVideoWidth.toString() + "," + player.currentVideoHeight)
            if (player.currentVideoWidth > 0) {
                height = player.width * player.currentVideoHeight / player.currentVideoWidth
            }
            player.layoutParams = this
        }
    }
    override val detailOrientationRotateAuto: Boolean
        get() = false


}