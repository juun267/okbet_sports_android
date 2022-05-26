package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData

class StatusSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var dataList = mutableListOf<StatusSheetData>()
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
            R.drawable.ic_arrow_gray
        )
    }
    private val textGravity by lazy {
        typedArray.getInt(
            R.styleable.StatusBottomSheetStyle_textGravity,
            0x11
        )
    }
    private lateinit var selectItem: StatusSheetData
    private lateinit var mListPop: ListPopupWindow
    var selectedTag: String? = ""
        get() = tv_name.tag?.toString()


    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_spinner, null)
        addView(view)
        view.apply {

            iv_arrow.setImageResource(arrowImg)
            tv_name.tag = ""
            tv_name.text =
                typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            tv_name.gravity = textGravity
            setOnClickListener {
                if (mListPop.isShowing) {
                    mListPop.dismiss()
                } else {
                    mListPop.show()
                }
            }
        }
        if (dataList.size > 0) {
            val first = dataList[0]
            first.isChecked = true
            selectItem = first
        }
        spinnerAdapter = StatusSpinnerAdapter(dataList)
        mListPop = ListPopupWindow(context)
        mListPop.width = ScreenUtils.getScreenWidth(context) / 2
        mListPop.height = LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.bg_play_category_pop
            )
        )
        mListPop.setAdapter(spinnerAdapter)
        mListPop.setAnchorView(cl_root) //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.setModal(true) //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mListPop.dismiss()
                selectItem = dataList.get(position)
                setSelectCode(selectItem.code)
                itemSelectedListener?.invoke(selectItem)
                setSelectInfo(selectItem)
            }
        })
    }

    fun setItemData(itemData: MutableList<StatusSheetData>) {
        dataList.clear()
        dataList.addAll(itemData)
        spinnerAdapter?.notifyDataSetChanged()
        if (dataList.size > 8) {
            mListPop.height = ScreenUtils.getScreenHeight(context) / 2
        }
    }

    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }

    fun setSelectCode(code: String?) {
        code?.let {
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
    }

    fun setSelectInfo(data: StatusSheetData) {
        tv_name.tag = data.code
        tv_name.text = data.showName
    }
}