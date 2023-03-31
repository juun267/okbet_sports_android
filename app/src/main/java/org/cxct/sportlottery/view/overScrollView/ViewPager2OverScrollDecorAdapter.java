package org.cxct.sportlottery.view.overScrollView;

import android.view.View;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

/**
 * An adapter to enable over-scrolling over object of {@link ViewPager}
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 */
public class ViewPager2OverScrollDecorAdapter implements IOverScrollDecoratorAdapter, ViewPager.OnPageChangeListener {

    protected final ViewPager2 mViewPager;

    protected int mLastPagerPosition = 0;
    protected float mLastPagerScrollOffset;

    public ViewPager2OverScrollDecorAdapter(ViewPager2 viewPager) {
        this.mViewPager = viewPager;

//        mViewPager.addOnPageChangeListener(this);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        mLastPagerPosition = mViewPager.getCurrentItem();
        mLastPagerScrollOffset = 0f;
    }

    @Override
    public View getView() {
        return mViewPager;
    }

    @Override
    public boolean isInAbsoluteStart() {

        return mLastPagerPosition == 0 &&
                mLastPagerScrollOffset == 0f;
    }

    @Override
    public boolean isInAbsoluteEnd() {

        return mLastPagerPosition == mViewPager.getAdapter().getItemCount()-1 &&
                mLastPagerScrollOffset == 0f;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mLastPagerPosition = position;
        mLastPagerScrollOffset = positionOffset;
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
