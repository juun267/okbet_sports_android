package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp


class EmptyView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var imageView: ImageView
    var textView: TextView

    init {
        val linearLayout = LinearLayout(context)
        linearLayout.gravity = Gravity.CENTER
        linearLayout.orientation = LinearLayout.VERTICAL
        imageView = AppCompatImageView(context)
        val wh = LinearLayout.LayoutParams.WRAP_CONTENT
        linearLayout.addView(imageView, LinearLayout.LayoutParams(256.dp, wh))
        textView = AppCompatTextView(context)
        textView.setTextColor(resources.getColor(R.color.color_6C7BA8_A7B2C4))
        textView.setTextSize(14f)
        linearLayout.addView(textView, LinearLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        addView(linearLayout, LayoutParams(-2, -2).apply { gravity = Gravity.CENTER })
    }

    fun setEmptyImg(imgResId: Int) {
        imageView.setImageResource(imgResId)
    }

    fun setEmptyText(text: String) {
        textView.setText(text)
    }
}