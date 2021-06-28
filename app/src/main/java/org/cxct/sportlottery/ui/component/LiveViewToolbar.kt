package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R

class LiveViewToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.CalendarBottomSheetStyle_calendarLayout, R.layout.dialog_bottom_sheet_webview) }
    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val calendarBottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar_live, this, false)
        addView(view)

        try {
            setupBottomSheet()
            initOnclick()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }
    private fun initOnclick() {

        iv_arrow.setOnClickListener {
            iv_arrow.animate().rotation(180f).setDuration(100).start()

            if (expand_layout.isExpanded) expand_layout.collapse()
            else expand_layout.expand()
        }

        tab_layout.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!calendarBottomSheet.isShowing) calendarBottomSheet.show()
                else calendarBottomSheet.dismiss()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }


    private fun setupBottomSheet() {
        calendarBottomSheet.setContentView(bottomSheetView)
    }

    fun loadUrl(url: String) {
        bottomSheetView.web_view.loadUrl(url)
    }


}