package org.cxct.sportlottery.util.drawable.shape;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ShapeType.RECTANGLE, ShapeType.OVAL,
        ShapeType.LINE, ShapeType.RING})
@Retention(RetentionPolicy.SOURCE)
public @interface ShapeTypeLimit {}