package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R

/**
 * 客製化 TabLayout
 */
class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
     val tabLayoutCustom by lazy { findViewById<TabLayout>(R.id.tab_layout_custom) }
    init {

        LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, this, true)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout,0,0)
        tabLayoutCustom.getTabAt(0)?.text = typedArray.getString(R.styleable.CustomTabLayout_firstTabText) ?:""
        tabLayoutCustom.getTabAt(1)?.text = typedArray.getString(R.styleable.CustomTabLayout_secondTabText) ?:""
        typedArray.recycle()

        tabLayoutCustom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
            tabLayoutCustom.getTabAt(0)?.text = firstTabText
        }

    var secondTabText: String? = null
        set(value) {
            field = value
            tabLayoutCustom.getTabAt(1)?.text = secondTabText
        }

    var firstTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            tabLayoutCustom.getTabAt(0)?.view?.visibility = value
        }

    var secondTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            tabLayoutCustom.getTabAt(1)?.view?.visibility = value
        }

    val selectedTabPosition by lazy { tabLayoutCustom.selectedTabPosition }

    fun selectTab(position: Int?) {
        tabLayoutCustom.getTabAt(position ?: 0)?.select()
    }

    fun setCustomTabSelectedListener(listener: (position: Int?) -> Unit) {
        tabSelectedListener = listener
    }

}