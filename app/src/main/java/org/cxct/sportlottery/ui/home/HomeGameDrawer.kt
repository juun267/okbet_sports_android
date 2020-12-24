package org.cxct.sportlottery.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.home_game_drawer.view.*
import org.cxct.sportlottery.R

class HomeGameDrawer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_drawer, this, false)
        addView(view)

        try {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.HomeGameDrawer, 0, 0)

            view.tv_title.text = typedArray.getText(R.styleable.HomeGameDrawer_hgDrawer_title)
            view.tv_count.text = typedArray.getText(R.styleable.HomeGameDrawer_hgDrawer_count)

            //展開/收合
            view.titleBar.setOnClickListener {
                view.expandableLayout.toggle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTitle(title: String?) {
        tv_title.text = title
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }

}