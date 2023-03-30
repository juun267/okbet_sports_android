package org.cxct.sportlottery.view.boundsEditText

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.cxct.sportlottery.R

internal class ClipToBoundsView : RelativeLayout {
    var cornerRadius: Float? = null
    var rect = Rect()
    var rectF = RectF()
    var clipPath = Path()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    protected fun init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.getClipBounds(rect)
        rectF.set(rect)
        clipPath.reset()
        clipPath.addRoundRect(rectF, cornerRadius!!, cornerRadius!!, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }
}