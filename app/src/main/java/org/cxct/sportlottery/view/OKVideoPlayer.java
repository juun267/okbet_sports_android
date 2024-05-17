package org.cxct.sportlottery.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import org.cxct.sportlottery.R;

public class OKVideoPlayer extends GSYVideoPlayer {
    public interface OnOkListener {
        void onStartPrepared();
        void onPrepared();
        void onError();
    }

    public interface PlayStatusListener {
        void onPrepare();
        void onPlaying();
        void onPause();
        void onPlayComplete();
        void onError();
    }

    private PlayStatusListener playStatusListener;
    public OnOkListener onOkListener;

    public void setPlayStatusListener(PlayStatusListener playStatusListener) {
        this.playStatusListener = playStatusListener;
    }

    public void setOnOkListener(OnOkListener onOkListener) {
        this.onOkListener = onOkListener;
    }

    public OKVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public OKVideoPlayer(Context context) {
        super(context);
    }

    public OKVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNeedMute(boolean needMute) {
        GSYVideoManager.instance().setNeedMute(needMute);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mIsTouchWiget = true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_video_ok;
    }

    @Override
    public void startPlayLogic() {
        prepareVideo();
        GSYVideoManager.instance().setNeedMute(true);
        setVideoAllCallBack(new VideoAllCallBack() {
            @Override
            public void onStartPrepared(String url, Object... objects) {
                GSYVideoManager.instance().setNeedMute(true);
                if (onOkListener != null) {
                    onOkListener.onStartPrepared();
                }
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                GSYVideoManager.instance().setNeedMute(true);
                if (onOkListener != null) {
                    onOkListener.onPrepared();
                }
            }

            @Override
            public void onClickStartIcon(String url, Object... objects) {

            }

            @Override
            public void onClickStartError(String url, Object... objects) {

            }

            @Override
            public void onClickStop(String url, Object... objects) {

            }

            @Override
            public void onClickStopFullscreen(String url, Object... objects) {

            }

            @Override
            public void onClickResume(String url, Object... objects) {

            }

            @Override
            public void onClickResumeFullscreen(String url, Object... objects) {

            }

            @Override
            public void onClickSeekbar(String url, Object... objects) {

            }

            @Override
            public void onClickSeekbarFullscreen(String url, Object... objects) {

            }

            @Override
            public void onAutoComplete(String url, Object... objects) {

            }

            @Override
            public void onComplete(String url, Object... objects) {

            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {

            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {

            }

            @Override
            public void onQuitSmallWidget(String url, Object... objects) {

            }

            @Override
            public void onEnterSmallWidget(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekVolume(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekPosition(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekLight(String url, Object... objects) {

            }

            @Override
            public void onPlayError(String url, Object... objects) {
                if (onOkListener != null) {
                    onOkListener.onError();
                }
            }

            @Override
            public void onClickStartThumb(String url, Object... objects) {

            }

            @Override
            public void onClickBlank(String url, Object... objects) {

            }

            @Override
            public void onClickBlankFullscreen(String url, Object... objects) {

            }
        });
    }

    @Override
    public void onVideoSizeChanged() {
        super.onVideoSizeChanged();
    }

    @Override
    protected void showWifiDialog() {

    }

    @Override
    protected void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {

    }

    @Override
    protected void dismissProgressDialog() {

    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {

    }

    @Override
    protected void dismissVolumeDialog() {

    }

    @Override
    protected void showBrightnessDialog(float percent) {

    }

    @Override
    protected void dismissBrightnessDialog() {

    }

    @Override
    protected void onClickUiToggle(MotionEvent e) {

    }

    @Override
    protected void hideAllWidget() {

    }

    @Override
    protected void changeUiToNormal() {

    }

    @Override
    protected void changeUiToPreparingShow() {

    }

    @Override
    protected void changeUiToPlayingShow() {

    }

    @Override
    protected void changeUiToPauseShow() {

    }

    @Override
    protected void changeUiToError() {

    }

    @Override
    protected void changeUiToCompleteShow() {

    }

    @Override
    protected void changeUiToPlayingBufferingShow() {

    }

    public void showTranBar(Boolean show) {
        findViewById(R.id.rl_tran_cover).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        if (playStatusListener == null) {
            return;
        }

        if (CURRENT_STATE_NORMAL == state || CURRENT_STATE_PREPAREING == state) {
            playStatusListener.onPrepare();
            return;
        }

        if (CURRENT_STATE_PLAYING == state) {
            playStatusListener.onPlaying();
            return;
        }

        if (CURRENT_STATE_PAUSE == state) {
            playStatusListener.onPause();
            return;
        }

        if (CURRENT_STATE_AUTO_COMPLETE == state) {
            playStatusListener.onPlayComplete();
            return;
        }

        if (CURRENT_STATE_ERROR == state) {
            playStatusListener.onError();
        }

    }
}
