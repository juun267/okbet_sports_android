package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.CustomTabLayoutBinding
import splitties.systemservices.layoutInflater

/**
 * 客製化 TabLayout
 */
class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

     val binding: CustomTabLayoutBinding = CustomTabLayoutBinding.inflate(layoutInflater,this,true)

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout,0,0)
        binding.tabLayoutCustom.getTabAt(0)?.text = typedArray.getString(R.styleable.CustomTabLayout_firstTabText) ?:""
        binding.tabLayoutCustom.getTabAt(1)?.text = typedArray.getString(R.styleable.CustomTabLayout_secondTabText) ?:""
        typedArray.recycle()

        binding.tabLayoutCustom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
            binding.tabLayoutCustom.getTabAt(0)?.text = firstTabText
        }

    var secondTabText: String? = null
        set(value) {
            field = value
            binding.tabLayoutCustom.getTabAt(1)?.text = secondTabText
        }

    var firstTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            binding.tabLayoutCustom.getTabAt(0)?.view?.visibility = value
        }

    var secondTabVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            binding.tabLayoutCustom.getTabAt(1)?.view?.visibility = value
        }

    val selectedTabPosition by lazy { binding.tabLayoutCustom.selectedTabPosition }

    fun selectTab(position: Int?) {
        binding.tabLayoutCustom.getTabAt(position ?: 0)?.select()
    }

    fun setCustomTabSelectedListener(listener: (position: Int?) -> Unit) {
        tabSelectedListener = listener
    }

}