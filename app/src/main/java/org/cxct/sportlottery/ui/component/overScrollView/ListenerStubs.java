package org.cxct.sportlottery.ui.component.overScrollView;

public interface ListenerStubs {

    class OverScrollStateListenerStub implements IOverScrollStateListener {
        @Override
        public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) { }
    }

    class OverScrollUpdateListenerStub implements IOverScrollUpdateListener {
        @Override
        public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) { }
    }
}
