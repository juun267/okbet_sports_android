package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.DisplayUtil.dp

class StatusSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    open var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerAdapter? = null
    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StatusBottomSheetStyle,
            0,
            0
        )
    }
    private val arrowImg by lazy {
        typedArray.getResourceId(
            R.styleable.StatusBottomSheetStyle_arrowSrc,
            R.drawable.icon_arrow_down
        )
    }
    private val arrowImgUp by lazy {
        typedArray.getResourceId(
            R.styleable.StatusBottomSheetStyle_arrowSrc,
            R.drawable.icon_arrow_up
        )
    }
    private val textGravity by lazy {
        typedArray.getInt(
            R.styleable.StatusBottomSheetStyle_textGravity,
            0x11
        )
    }
    private var selectItem: StatusSheetData? = null
    private lateinit var mListPop: ListPopupWindow

    var selectedListener: OnClickListener ? = null

    var selectedTag: String? = ""
        get() = tv_name.tag?.toString()

    var selectedCode: String? = ""
        get() = selectItem?.code

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_spinner, null)
        addView(view)
        view.apply {

            iv_arrow.setImageResource(arrowImg)
            tv_name.tag = ""
            tv_name.text =
                typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            tv_name.setTextColor(ContextCompat.getColor(context, R.color.color_C9CFD7))
            tv_name.gravity = textGravity
            setOnClickListener {
                this@StatusSpinnerView.callOnClick()
                if (mListPop.isShowing) {
                    mListPop.dismiss()
                    iv_arrow.setImageResource(arrowImg)
                } else {
                    mListPop.horizontalOffset = (cl_root.width - mListPop.width) / 2
                    mListPop.show()
                    iv_arrow.setImageResource(arrowImgUp)
                }
            }
        }
        if (dataList.size > 0) {
            val first = dataList[0]
            first.isChecked = true
            selectItem = first
            setSelectCode(first.code)
        }
        spinnerAdapter = StatusSpinnerAdapter(dataList)
        spinnerAdapter!!.setItmeColor(resources.getColor(R.color.color_FFFFFF))
        mListPop = ListPopupWindow(context)
        cl_root.doOnLayout {
            var listWidth = typedArray.getDimension(R.styleable.StatusBottomSheetStyle_listWidth,
                0F
            )
            if (listWidth > 0) {
                mListPop.width = listWidth.toInt()
            } else {
                mListPop.width = cl_root.width - 10.dp
            }
        }
        mListPop.height = LayoutParams.WRAP_CONTENT

        var listBackResource =
            typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_listBackground, 0)
        if (listBackResource != 0) {
            mListPop.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    listBackResource
                )
            )
        }else{
            mListPop.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_pop_up_arrow
                )
            )
        }
        mListPop.setOnDismissListener {
            iv_arrow.setImageResource(arrowImg)
        }
        mListPop.setAdapter(spinnerAdapter)
        mListPop.anchorView = cl_root  //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.isModal = true //设置是否是模式
        mListPop.verticalOffset = 5
        mListPop.horizontalOffset = 5.dp
        mListPop.setDropDownGravity(Gravity.CENTER)
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                mListPop.dismiss()
                dataList[position]?.let {
                    selectItem = it
                    setSelectCode(it.code)
                    itemSelectedListener?.invoke(it)
                    setSelectInfo(it)
                }
            }
        })
    }

    fun setBetStationStyle(){
        ConstraintSet().apply {
            clone(cl_root)
            clear(R.id.tv_name, ConstraintSet.END)
            clear(R.id.iv_arrow, ConstraintSet.START)
            connect(R.id.iv_arrow, ConstraintSet.END, R.id.cl_root, ConstraintSet.END, 6.dp)
            applyTo(cl_root)
        }
    }

    fun setItemData(itemData: MutableList<StatusSheetData>, isSelectedDefault: Boolean = true) {
        dataList.clear()
        dataList.addAll(itemData)
        spinnerAdapter?.notifyDataSetChanged()
        if (dataList.size > 8) {
            mListPop.height = ScreenUtils.getScreenHeight(context) / 2
        } else {
            mListPop.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        if(isSelectedDefault){
            dataList.firstOrNull()?.let { defaultData ->
                setSelectCode(defaultData.code)
            }
        }
    }

    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }

    fun setSelectCode(code: String?) {
        for (index in dataList) {
            if (index.code == code) {
                selectItem = index
                index.isChecked = true
                setSelectInfo(index)
            } else {
                index.isChecked = false
            }
        }
    }

    fun setSelectInfo(data: StatusSheetData?) {
        tv_name.tag = data?.code
        tv_name.text = data?.showName
        selectedListener?.onClick(null)
        tv_name.setTextColor(ContextCompat.getColor(context, R.color.color_414655))
    }
}