package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.component.StatusSpinnerAdapter
import java.util.ArrayList

class StatusSpinnerView1 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
    private lateinit var spStatus: Spinner
    var dataList = mutableListOf<StatusSheetData>()
    var spinnerAdapter: StatusSpinnerAdapter? = null
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    private val arrowImg by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)}
    private val textGravity by lazy { typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity, 0x11)}


    init {
        Log.d("hjq","init")
        val view  = LayoutInflater.from(context).inflate(R.layout.view_status_spinner, null)
        addView(view)
        view.apply {
            val chainStyle = typedArray.getInt(R.styleable.StatusBottomSheetStyle_horizontalChainStyle,
                StatusSelectorView.STYLE_NULL
            )
            val arrowAtEnd = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_arrowAtEnd, false)
            val backgroundFrame = typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_backgroundFrame,
                StatusSelectorView.STYLE_NULL
            )
            iv_arrow.setImageResource(arrowImg)
            cl_root.background = ContextCompat.getDrawable(context, R.drawable.frame_color_silver_light_stroke)
            if (backgroundFrame != StatusSelectorView.STYLE_NULL)
                cl_root.background = ContextCompat.getDrawable(context, R.drawable.frame_color_silver_light_stroke)
            tv_name.tag = ""
            tv_name.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            tv_name.gravity = textGravity
            setOnClickListener(OnClickListener { spStatus.performClick() })
            if (dataList.size > 0) {
                val first = dataList[0]
                first.isChecked = true
            }
            spinnerAdapter = StatusSpinnerAdapter(dataList)
            spStatus.setAdapter(spinnerAdapter)
            spStatus.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id -> })
        }

    }
    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }

}