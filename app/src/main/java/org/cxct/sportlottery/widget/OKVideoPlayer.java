package org.cxct.sportlottery.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import org.cxct.sportlottery.R;

public class OKVideoPlayer extends GSYVideoPlayer {
    public OKVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public OKVideoPlayer(Context context) {
        super(context);
    }

    public OKVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_video_ok;
    }

    @Override
    public void startPlayLogic() {
        prepareVideo();
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
}
