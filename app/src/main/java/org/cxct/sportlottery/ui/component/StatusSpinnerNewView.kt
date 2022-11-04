package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil

class StatusSpinnerNewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerNewAdapter? = null
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

    var selectedCode: String? = ""
        get() = selectItem.code

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_spinner_new, null)
        addView(view)
        view.apply {

            iv_arrow.setImageResource(arrowImg)
            tv_name.tag = ""
            tv_name.text =
                typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            tv_name.gravity = textGravity
            tv_name.setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
            setOnClickListener {
                this@StatusSpinnerNewView.callOnClick()
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
            setSelectCode(first.code)
        }
        spinnerAdapter = StatusSpinnerNewAdapter(dataList)
        spinnerAdapter!!.setItmeColor(typedArray.getColor(R.styleable.StatusBottomSheetStyle_listTextColor,resources.getColor(R.color.color_FFFFFF_414655)))
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
        spinnerAdapter
        cl_root.doOnLayout {
            var listWidth = typedArray.getDimension(R.styleable.StatusBottomSheetStyle_listWidth,
                0F
            )

            if (listWidth > 0) {
                mListPop.width = listWidth.toInt()
            } else {
                mListPop.width = cl_root.width + 8.dp
            }

        }
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            @SuppressLint("ResourceAsColor")
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
        } else {
            mListPop.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        dataList.firstOrNull()?.let { defaultData ->
            setSelectCode(defaultData.code)
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

    fun setSelectInfo(data: StatusSheetData) {
        tv_name.tag = data.code
        tv_name.text = data.showName
        tv_name.setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
    }


}