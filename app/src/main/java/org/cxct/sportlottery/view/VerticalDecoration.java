package org.cxct.sportlottery.view;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.OrientationHelper;


public class VerticalDecoration extends DividerItemDecoration {

    public VerticalDecoration(Context context, int drawableId) {
        super(context, OrientationHelper.VERTICAL);
        setDrawable(ContextCompat.getDrawable(context, drawableId));
    }

    public VerticalDecoration(Context context, int orientation, int drawableId) {
        super(context, orientation);
        setDrawable(ContextCompat.getDrawable(context, drawableId));
    }


}
