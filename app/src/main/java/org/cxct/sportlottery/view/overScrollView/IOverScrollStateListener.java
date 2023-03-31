package org.cxct.sportlottery.view.overScrollView;

/**
 * A callback-listener enabling over-scroll effect clients to be notified of effect state transitions.
 * <br/>Invoked whenever state is transitioned onto one of {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_IDLE},
 * {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_DRAG_START_SIDE}, {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_DRAG_END_SIDE}
 * or {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_BOUNCE_BACK}.
 *
 * @see me.everything.android.ui.overscroll.IOverScrollUpdateListener
 */
public interface IOverScrollStateListener {

    /**
     * The invoked callback.
     *
     * @param decor The associated over-scroll 'decorator'.
     * @param oldState The old over-scroll state; ID's specified by {@link me.everything.android.ui.overscroll.IOverScrollState}, e.g.
     *                 {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_IDLE}.
     * @param newState The <b>new</b> over-scroll state; ID's specified by {@link me.everything.android.ui.overscroll.IOverScrollState},
     *                 e.g. {@link me.everything.android.ui.overscroll.IOverScrollState#STATE_IDLE}.
     */
    void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState);
}
