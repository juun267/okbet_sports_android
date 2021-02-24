package org.cxct.sportlottery.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.main_label.view.*
import org.cxct.sportlottery.R

class MainLabel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.main_label, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, -1))
            view.btn_more.visibility = if (typedArray.getBoolean(R.styleable.CustomView_cvEnableMoreBtn, false)) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setOnMoreClickListener(listener: (View) -> Unit) {
        btn_more.setOnClickListener { listener(it) }
    }

}