package org.cxct.sportlottery.util.drawable.shape;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ShapeGradientType.LINEAR_GRADIENT, ShapeGradientType.RADIAL_GRADIENT, ShapeGradientType.SWEEP_GRADIENT})
@Retention(RetentionPolicy.SOURCE)
public @interface ShapeGradientTypeLimit {}