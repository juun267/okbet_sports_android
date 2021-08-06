package org.cxct.sportlottery.ui.game.bottomNavigation

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import org.cxct.sportlottery.R
import java.lang.Exception

class BottomNavigationItem @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {
    var checked: Boolean = false
        set(value) {
            field = value
            setupStatus(field)
        }

    init {
        init(attributeSet)
    }

    private fun init(attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BottomNavigationItem)
        try {
            inflate(context, R.layout.bottom_navigation_item, this).apply {
                item_icon.setImageResource(
                    typedArray.getResourceId(
                        R.styleable.BottomNavigationItem_btiIcon,
                        android.R.color.white
                    )
                )
                item_title.text = typedArray.getString(R.styleable.BottomNavigationItem_btiTitle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            typedArray.recycle()
        }

    }

    private fun setupStatus(checked: Boolean) {
        item.isSelected = checked
        item_icon.isSelected = checked
        item_title.isSelected = checked
    }
}