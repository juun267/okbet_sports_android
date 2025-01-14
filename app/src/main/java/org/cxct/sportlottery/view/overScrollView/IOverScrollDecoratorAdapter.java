package org.cxct.sportlottery.view.overScrollView;

import android.view.View;

/**
 * @see HorizontalOverScrollBounceEffectDecorator
 */
public interface IOverScrollDecoratorAdapter {

    View getView();

    /**
     * Is view in it's absolute start position - such that a negative over-scroll can potentially
     * be initiated. For example, in list-views, this is synonymous with the first item being
     * fully visible.
     *
     * @return Whether in absolute start position.
     */
    boolean isInAbsoluteStart();

    /**
     * Is view in it's absolute end position - such that an over-scroll can potentially
     * be initiated. For example, in list-views, this is synonymous with the last item being
     * fully visible.
     *
     * @return Whether in absolute end position.
     */
    boolean isInAbsoluteEnd();
}
