package org.cxct.sportlottery.ui.component

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.itemview_play_category_v4.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.component.StatusSpinnerAdapter
import org.cxct.sportlottery.util.MetricsUtil
import java.util.ArrayList

class StatusSpinnerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
    private var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerAdapter? = null
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    private val arrowImg by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)}
    private val textGravity by lazy { typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity, 0x11)}
    private lateinit var selectItem:StatusSheetData
    var selectedTag: String? = ""
        get() = tv_name.tag?.toString()

    init {
        val view  = LayoutInflater.from(context).inflate(R.layout.view_status_spinner, null)
        addView(view)
        view.apply {

            iv_arrow.setImageResource(arrowImg)
            cl_root.background = ContextCompat.getDrawable(context, R.drawable.frame_color_silver_light_stroke)
            tv_name.tag = ""
            tv_name.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            tv_name.gravity = textGravity
            setOnClickListener(OnClickListener { sp_status.performClick() })
            if (dataList.size > 0) {
                val first = dataList[0]
                first.isChecked = true
                selectItem=first
            }
            //region 調整Spinner選單位置
            sp_status.dropDownVerticalOffset = ScreenUtils.dip2px(context, 48F)
            sp_status.dropDownWidth=ScreenUtils.getScreenWidth(context)/2
            spinnerAdapter = StatusSpinnerAdapter(dataList)
            sp_status.setAdapter(spinnerAdapter)
            sp_status.onItemSelectedListener=object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    selectItem=dataList.get(position)
                    setSelectCode(selectItem.code)
                    itemSelectedListener?.invoke(selectItem)
                    setSelectInfo(selectItem)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }
        }
    }
   fun setItemData(itemData:MutableList<StatusSheetData>){
       dataList.clear()
       dataList.addAll(itemData)
       spinnerAdapter?.notifyDataSetChanged()
   }
    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }
    fun setSelectCode(code:String?){
        code?.let {
            for (index in dataList){
                if(index.code == code){
                    selectItem=index
                    index.isChecked=true
                    setSelectInfo(index)
                }else{
                    index.isChecked=false
                }
            }
        }
    }
    fun setSelectInfo(data:StatusSheetData){
        tv_name.tag=data.code
        tv_name.text=data.showName
    }

}