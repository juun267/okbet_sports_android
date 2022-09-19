package org.cxct.sportlottery.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentHelper {
    FragmentManager fragmentManager;
    private int viewId;
    private Fragment[] fragments;
    private int curPos = -1;

    public FragmentHelper(FragmentManager fragmentManager, int viewId, Fragment[] fragments) {
        this.fragmentManager = fragmentManager;
        this.viewId = viewId;
        this.fragments = fragments;
    }

    public void showFragment(int index) {
        if (curPos == index) {
            return;
        }
        if (curPos >= 0) {
            switchContent(fragments[curPos], fragments[index]);
        } else {
            switchContent(null, fragments[index]);
        }
        curPos = index;
    }

    public void switchContent(Fragment from, Fragment to) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (from == null) {
            transaction.add(viewId, to).commitAllowingStateLoss();
        } else if (from != to) {
            if (!to.isAdded()) {
                transaction.hide(from).add(viewId, to).commitAllowingStateLoss();
            } else {
                transaction.hide(from).show(to).commitAllowingStateLoss();
            }
        }
    }
}
