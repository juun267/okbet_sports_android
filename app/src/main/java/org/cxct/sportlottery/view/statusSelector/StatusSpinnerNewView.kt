package org.cxct.sportlottery.view.statusSelector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.luck.picture.lib.tools.ScreenUtils

import kotlinx.android.synthetic.main.view_status_spinner_new.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemPlaySpinnerNewBinding
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData


@Deprecated("宽度自适应有问题(虽然已处理)，但建议使用")
class StatusSpinnerNewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerNewAdapter? = null

    private lateinit var selectItem: StatusSheetData
    private lateinit var mListPop: ListPopupWindow
    var selectedTag: String? = ""
        get() = tv_name.tag?.toString()

    var selectedCode: String? = ""
        get() = selectItem.code

    var itmeColor: Int = Color.BLACK
    var maxWidth: Int = 0

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_spinner_new, this, true)
        view.apply {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle,0,0)
            val arrowImg = typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)
            val textGravity = typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity,0x11)
            itmeColor = typedArray.getColor(R.styleable.StatusBottomSheetStyle_listTextColor, resources.getColor(R.color.color_FFFFFF_414655))

            tv_name.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            typedArray.recycle()

            iv_arrow.setImageResource(arrowImg)
            tv_name.tag = ""
            tv_name.gravity = textGravity
            tv_name.setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
            setOnClickListener {
                if (mListPop.isShowing) {
                    mListPop.dismiss()
                } else {
                    cl_root_new.doOnLayout {
                        mListPop.setAnchorView(parent!! as View) //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
                        mListPop.show()
                    }
                }
            }
        }
        if (dataList.size > 0) {
            val first = dataList[0]
            first.isChecked = true
            selectItem = first
            setSelectCode(first.code)
        }

        mListPop = ListPopupWindow(context)
        spinnerAdapter = StatusSpinnerNewAdapter(dataList)

        spinnerAdapter!!.setItmeColor(itmeColor)
        mListPop.height = LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_play_category_pop))
        mListPop.setAdapter(spinnerAdapter)
        mListPop.setModal(true) //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {

                mListPop.dismiss()
                selectItem = dataList.get(position)
                setSelectCode(selectItem.code)
                itemSelectedListener?.invoke(selectItem)
                setSelectInfo(selectItem)
            }
        })
    }

    private fun resetWidth() {
        calculateWidth()
        val width = Math.max(maxWidth, measuredWidth)
        mListPop.width = width
        (parent as View?)?.let {
            it.layoutParams.width = width
            it.layoutParams = it.layoutParams
        }
    }

    private fun calculateWidth() {
        var text = ""
        var length = 0f
        val paint = Paint()
        paint.textSize = 1f
        dataList.forEach {
            it.showName?.let {
                val textLength = paint.measureText(it)
                if (textLength > length) {
                    text = it
                    length = textLength
                }
            }
        }

        val binding = ItemPlaySpinnerNewBinding.inflate(LayoutInflater.from(context), this, false)
        binding.tvPlay.text = text
        binding.root.measure(0, 0)
        maxWidth = binding.root.measuredWidth
    }

    fun setItemData(itemData: MutableList<StatusSheetData>) {
        dataList.clear()
        dataList.addAll(itemData)
        resetWidth()
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