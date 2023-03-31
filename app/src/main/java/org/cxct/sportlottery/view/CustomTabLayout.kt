package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.custom_tab_layout.view.*
import org.cxct.sportlottery.R

/**
 * 客製化 TabLayout
 */
class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    init {

        LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, this, true)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout,0,0)
        tab_layout_custom.getTabAt(0)?.text = typedArray.getString(R.styleable.CustomTabLayout_firstTabText) ?:""
        tab_layout_custom.getTabAt(1)?.text = typedArray.getString(R.styleable.CustomTabLayout_secondTabText) ?:""
        typedArray.recycle()

        tab_layout_custom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabSelectedListener?.invoke(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    var tabSelectedListener: ((position: Int?) -> Unit)? = null

    var firstTabText: String? = null
        set(value) {
            field = value
            tab_layout_custom.getTabAt(0)?.text = firstTabText
        }

    var secondTabText: String? = null
        set(value) {
            field = value
            tab_layout_custom.getTabAt(1)?.text = secondTabText
        }

    var firstTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            tab_layout_custom.getTabAt(0)?.view?.visibility = value
        }

    var secondTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            tab_layout_custom.getTabAt(1)?.view?.visibility = value
        }

    val selectedTabPosition by lazy { tab_layout_custom.selectedTabPosition }

    fun selectTab(position: Int?) {
        tab_layout_custom.getTabAt(position ?: 0)?.select()
    }

    fun setCustomTabSelectedListener(listener: (position: Int?) -> Unit) {
        tabSelectedListener = listener
    }

}