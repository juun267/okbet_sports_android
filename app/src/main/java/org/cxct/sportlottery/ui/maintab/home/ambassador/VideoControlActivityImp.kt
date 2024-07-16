package org.cxct.sportlottery.ui.maintab.home.ambassador

import android.content.res.Configuration
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import androidx.viewbinding.ViewBinding
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.utils.OrientationOption
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LogUtil
import java.lang.NullPointerException

/**
 * 详情模式播放页面基础类
 * Created by guoshuyu on 2017/9/14.
 */
 abstract class VideoControlActivityImp<VM : BaseViewModel, VB : ViewBinding> :BaseActivity<VM, VB>() ,
    VideoAllCallBack {
    protected var isPlay = false
    protected var isPause = false
    protected var orientationUtils: OrientationUtils? = null

    /**
     * 选择普通模式
     */
    fun initVideo() {
//        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        //外部辅助的旋转，帮助全屏
        orientationUtils = OrientationUtils(this, gSYVideoPlayer, orientationOption)
        //初始化不打开外部的旋转
        orientationUtils!!.isEnable = false
        if (gSYVideoPlayer!!.fullscreenButton != null) {
            gSYVideoPlayer!!.fullscreenButton.setOnClickListener {
                showFull()
                clickForFullScreen()
            }
        }
    }

    /**
     * 选择builder模式
     */
    fun initVideoBuilderMode() {
        initVideo()
        gSYVideoOptionBuilder.setVideoAllCallBack(this)
            .build(gSYVideoPlayer)
    }

    fun showFull() {
        if (orientationUtils!!.isLand != 1) {
            //直接横屏
            // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
            // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
            orientationUtils!!.resolveByClick()
        }
        //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
        gSYVideoPlayer!!.startWindowFullscreen(this@VideoControlActivityImp,
            hideActionBarWhenFull(),
            hideStatusBarWhenFull())
    }

    override fun onBackPressed() {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils!!.backToProtVideo()
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        gSYVideoPlayer!!.currentPlayer.onVideoPause()
        if (orientationUtils != null) {
            orientationUtils!!.setIsPause(true)
        }
        isPause = true
    }

    override fun onResume() {
        super.onResume()
        gSYVideoPlayer!!.currentPlayer.onVideoResume()
        if (orientationUtils != null) {
            orientationUtils!!.setIsPause(false)
        }
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlay) {
            gSYVideoPlayer!!.currentPlayer.release()
        }
        if (orientationUtils != null) orientationUtils!!.releaseListener()
    }

    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            gSYVideoPlayer!!.onConfigurationChanged(this,
                newConfig,
                orientationUtils,
                hideActionBarWhenFull(),
                hideStatusBarWhenFull())
        }
    }

    override fun onStartPrepared(url: String, vararg objects: Any) {

    }
    override fun onPrepared(url: String, vararg objects: Any) {
        if (orientationUtils == null) {
            throw NullPointerException("initVideo() or initVideoBuilderMode() first")
        }
        //开始播放了才能旋转和全屏
        orientationUtils!!.isEnable = detailOrientationRotateAuto && !isAutoFullWithSize
        isPlay = true
//        onVideoPrepared()
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
    override fun onEnterFullscreen(url: String, vararg objects: Any) {}
    override fun onQuitFullscreen(url: String, vararg objects: Any) {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils!!.backToProtVideo()
        }
    }

    override fun onQuitSmallWidget(url: String, vararg objects: Any) {}
    override fun onEnterSmallWidget(url: String, vararg objects: Any) {}
    override fun onTouchScreenSeekVolume(url: String, vararg objects: Any) {}
    override fun onTouchScreenSeekPosition(url: String, vararg objects: Any) {}
    override fun onTouchScreenSeekLight(url: String, vararg objects: Any) {}
    override fun onPlayError(url: String, vararg objects: Any) {}
    override fun onClickStartThumb(url: String, vararg objects: Any) {}
    override fun onClickBlank(url: String, vararg objects: Any) {}
    override fun onClickBlankFullscreen(url: String, vararg objects: Any) {}
    override fun onComplete(url: String, vararg objects: Any) {}
    fun hideActionBarWhenFull(): Boolean {
        return true
    }

    fun hideStatusBarWhenFull(): Boolean {
        return true
    }

    /**
     * 可配置旋转 OrientationUtils
     */
    val orientationOption: OrientationOption?
        get() = null

    /**
     * 播放控件
     */
    abstract val gSYVideoPlayer: GSYBaseVideoPlayer

    /**
     * 配置播放器
     */
    abstract val gSYVideoOptionBuilder: GSYVideoOptionBuilder

    /**
     * 点击了全屏
     */
    abstract fun clickForFullScreen()

    /**
     * 是否启动旋转横屏，true表示启动
     */
    abstract val detailOrientationRotateAuto: Boolean

    /**
     *视频准备好了，可以拿视频尺寸了
     */
    abstract fun onVideoPrepared()

    /**
     * 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
     */
    val isAutoFullWithSize: Boolean
        get() = false


}