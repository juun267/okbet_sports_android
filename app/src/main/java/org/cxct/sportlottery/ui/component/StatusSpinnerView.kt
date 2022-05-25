package org.cxct.sportlottery.ui.component

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.itemview_play_category_v4.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_selector.view.img_arrow
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.PlayCate
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlaySpinnerAdapter
import org.cxct.sportlottery.util.BindingUtil
import org.cxct.sportlottery.util.MetricsUtil.convertDpToPixel

class StatusSpinnerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    private lateinit var contetView:View
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    private val arrowImg by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)}
    private val textGravity by lazy { typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity, 0x11)}

    var selectedText: String? = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
        get() = tv_selected.text.toString()
        set(value) {
            field = value
            tv_selected.text = value
        }
    private var sheetAdapter: StatusSheetAdapter ?= null
    var itemData = mutableListOf<StatusSheetData>()
    init {
        contetView=LayoutInflater.from(context).inflate(R.layout.view_status_spinner,null)
        addView(contetView)
//        setSpinner(contetView.sp_status)
        try {
            contetView.apply {
                setOnClickListener {
                    contetView.sp_status.performClick()
                }
                img_arrow.setImageResource(arrowImg)
                cl_root.background = ContextCompat.getDrawable(context, R.drawable.frame_color_silver_light_stroke)
                tv_selected.tag = ""
                tv_selected.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
                tv_selected.gravity = textGravity
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }
    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }

    fun setSpinner(spinner:AppCompatSpinner) {
        var initSpinnerAdapter = true
        var initialSetItem: Boolean
        val StatusSpinnerAdapter by lazy { StatusSpinnerAdapter(itemData) }

        spinner.adapter = StatusSpinnerAdapter
            var dropDownVerticalOffset = ScreenUtils.dip2px(context, 48F)
            var dropDownHorizontalOffset = ScreenUtils.dip2px(context, -20F)
            itemData?.indexOfFirst { it.isChecked }
                ?.let { selectedIndex ->
                    //indexOfFirst無符合條件回傳-1
                    if (selectedIndex == -1)
                        spinner.setSelection(itemData.size)
                    else
                        spinner.setSelection(selectedIndex)

                    contetView.tv_name.text = itemData[selectedIndex].showName
                }
        sheetAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                val count = if (itemData?.size ?: 1 < 3) itemData?.size else 3
                val params: ViewGroup.LayoutParams = sheet_rv_more.layoutParams
                params.height = convertDpToPixel(48f * (count ?: 1), context).toInt()
                sheet_rv_more.layoutParams = params
            }
        })

    }
}