package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ViewRecommendMinigameBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation
import org.cxct.sportlottery.view.OKVideoPlayer.PlayStatusListener
import splitties.systemservices.layoutInflater


class RecommendMiniGame(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    private val binding = ViewRecommendMinigameBinding.inflate(layoutInflater, this)

    init {
        initStyle()
        initPlayer()
    }

    fun bindLifeCycle(lifecycleOwner: LifecycleOwner) = binding.videoPlayer.run {
        setNeedMute(true)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    binding.vCover.show()
                    if (tag != null) {
                        binding.videoPlayer.startPlayLogic()
                    }
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    binding.videoPlayer.onVideoPause()
                    binding.vCover.show()
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    binding.videoPlayer.release()
                    binding.vCover.show()
                }
            }

        })
    }

    private fun initStyle() {
        cardElevation = 0f
        radius = 8.dp.toFloat()
        setCardBackgroundColor(getColor(R.color.color_F7FAFE))
        binding.tvJackPotAmount.setPrefixString(showCurrencySign)
        binding.tvBetToWin.background = ShapeDrawable()
            .setSolidColor(getColor(R.color.color_0063FF), getColor(R.color.color_00C2FF))
            .setRadius(30.dp.toFloat())
            .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)

        binding.tvBetToWin.setOnClickListener { updateAwards(200f) }
    }

    private fun initPlayer() = binding.run {
        videoPlayer.setPlayStatusListener(object : PlayStatusListener {
            override fun onPrepare() {
                showCover()
            }
            override fun onPlaying() {
                hideCover()
            }
            override fun onPause() {
                showCover()
            }
            override fun onPlayComplete() {
                showCover()
            }
            override fun onError() {
                showCover()
            }

        })

    }

    private fun showCover() = binding.vCover.show()
    private fun hideCover() = binding.vCover.hide()

    fun startPlay(url: String) {
        val videoUrl = "https://ballpull.colorbingogo.com/colorlive/colorlive.flv"
        binding.videoPlayer.setUp(videoUrl, false, "")
        binding.videoPlayer.startPlayLogic()
        binding.videoPlayer.tag = url
    }

    private fun updateAwards(amount: Float) {
        binding.tvJackPotAmount.setNumberString("99999999")
    }


}