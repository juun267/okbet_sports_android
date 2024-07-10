package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import org.cxct.sportlottery.view.OKVideoPlayer.OnOkListener
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack
import com.shuyu.gsyvideoplayer.GSYVideoManager
import org.cxct.sportlottery.R

class OKVideoPlayer : GSYVideoPlayer {
    interface OnOkListener {
        fun onStartPrepared()
        fun onPrepared()
        fun onError()
    }

    var onOkListener: OnOkListener? = null

    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag) {}
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun init(context: Context) {
        super.init(context)
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        mIsTouchWiget = true
    }

    override fun getLayoutId(): Int {
        return R.layout.view_video_ok
    }

    override fun startPlayLogic() {
        prepareVideo()
        setVideoAllCallBack(object : VideoAllCallBack {
            override fun onStartPrepared(url: String, vararg objects: Any) {
                GSYVideoManager.instance().isNeedMute = true
                if (onOkListener != null) {
                    onOkListener!!.onStartPrepared()
                }
            }

            override fun onPrepared(url: String, vararg objects: Any) {
                GSYVideoManager.instance().isNeedMute = true
                if (onOkListener != null) {
                    onOkListener!!.onPrepared()
                }
            }

            override fun onClickStartIcon(url: String, vararg objects: Any) {}
            override fun onClickStartError(url: String, vararg objects: Any) {}
            override fun onClickStop(url: String, vararg objects: Any) {}
            override fun onClickStopFullscreen(url: String, vararg objects: Any) {}
            override fun onClickResume(url: String, vararg objects: Any) {}
            override fun onClickResumeFullscreen(url: String, vararg objects: Any) {}
            override fun onClickSeekbar(url: String, vararg objects: Any) {}
            override fun onClickSeekbarFullscreen(url: String, vararg objects: Any) {}
            override fun onAutoComplete(url: String, vararg objects: Any) {}
            override fun onComplete(url: String, vararg objects: Any) {}
            override fun onEnterFullscreen(url: String, vararg objects: Any) {}
            override fun onQuitFullscreen(url: String, vararg objects: Any) {}
            override fun onQuitSmallWidget(url: String, vararg objects: Any) {}
            override fun onEnterSmallWidget(url: String, vararg objects: Any) {}
            override fun onTouchScreenSeekVolume(url: String, vararg objects: Any) {}
            override fun onTouchScreenSeekPosition(url: String, vararg objects: Any) {}
            override fun onTouchScreenSeekLight(url: String, vararg objects: Any) {}
            override fun onPlayError(url: String, vararg objects: Any) {
                if (onOkListener != null) {
                    onOkListener!!.onError()
                }
            }

            override fun onClickStartThumb(url: String, vararg objects: Any) {}
            override fun onClickBlank(url: String, vararg objects: Any) {}
            override fun onClickBlankFullscreen(url: String, vararg objects: Any) {}
        })
    }

    override fun onVideoSizeChanged() {
        super.onVideoSizeChanged()
    }

    override fun showWifiDialog() {}
    override fun showProgressDialog(
        deltaX: Float,
        seekTime: String,
        seekTimePosition: Long,
        totalTime: String,
        totalTimeDuration: Long
    ) {
    }

    override fun dismissProgressDialog() {}
    override fun showVolumeDialog(deltaY: Float, volumePercent: Int) {}
    override fun dismissVolumeDialog() {}
    override fun showBrightnessDialog(percent: Float) {}
    override fun dismissBrightnessDialog() {}
    override fun onClickUiToggle(e: MotionEvent) {}
    override fun hideAllWidget() {}
    override fun changeUiToNormal() {}
    override fun changeUiToPreparingShow() {}
    override fun changeUiToPlayingShow() {}
    override fun changeUiToPauseShow() {}
    override fun changeUiToError() {}
    override fun changeUiToCompleteShow() {}
    override fun changeUiToPlayingBufferingShow() {}
    fun showTranBar(show: Boolean) {
        findViewById<View>(R.id.rl_tran_cover).visibility = if (show) VISIBLE else GONE
    }
}