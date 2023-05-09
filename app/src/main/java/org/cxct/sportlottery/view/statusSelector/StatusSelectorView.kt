package org.cxct.sportlottery.view.statusSelector

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui2.common.adapter.StatusSheetAdapter
import org.cxct.sportlottery.ui2.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.MetricsUtil.convertDpToPixel

class StatusSelectorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    companion object {
        const val STYLE_NULL = -99
    }

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_sheetLayout, R.layout.dialog_bottom_sheet_custom) }
    private val bottomSheetView: View by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }
    private val arrowImg by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)}
    private val textGravity by lazy { typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity, 0x11)}

    var selectedText: String? = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
        get() = tv_selected.text.toString()
        set(value) {
            field = value
            tv_selected.text = value
        }

    var bottomSheetTitleText: String? = null
        get() = if (bottomSheetView.sheet_tv_title.text.toString().isEmpty()) typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultBottomSheetTitleText) else bottomSheetView.sheet_tv_title.text.toString()
        set(value) {
            field = value
            bottomSheetView.sheet_tv_title.text = value
        }

    var selectedTag: String? = ""
        get() = tv_selected.tag?.toString()
        set(value) {
            field = value
            tv_selected.tag = value
            sheetAdapter?.defaultCheckedCode = value
        }

    var selectedTextColor: Int? = null
        get() = tv_selected.currentTextColor
        set(value) {
            field = value
            if (value != null) tv_selected.setTextColor(ContextCompat.getColor(context, value))
        }

    private var sheetAdapter: StatusSheetAdapter?= null

    var dataList = sheetAdapter?.dataList
        get() = sheetAdapter?.dataList
        set(value) {
            field = value
            sheetAdapter?.dataList = value ?: listOf()

            tv_selected.tag = value?.firstOrNull()?.code
            sheetAdapter?.defaultCheckedCode = value?.firstOrNull()?.code

            sheetAdapter?.notifyDataSetChanged()
        }

    var isShowAllCheckBoxView: Boolean? = false
        get() = bottomSheetView.checkbox_select_all.isVisible
        set(value) {
            field = value
            if (value == true) bottomSheetView.checkbox_select_all.visibility = View.VISIBLE
            else bottomSheetView.checkbox_select_all.visibility = View.GONE
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_selector, this, false)
        addView(view)

        try {
            setBottomSheet(typedArray)

            view?.apply {
                setOnClickListener {
                    bottomSheet.show()
                }
                img_arrow.setImageResource(arrowImg)

                val chainStyle = typedArray.getInt(R.styleable.StatusBottomSheetStyle_horizontalChainStyle, STYLE_NULL)
                val arrowAtEnd = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_arrowAtEnd, false)
                val backgroundFrame = typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_backgroundFrame, STYLE_NULL)

                val constrainSet = ConstraintSet()

                when {
                    arrowAtEnd -> {
                        constrainSet.apply {
                            clone(cl_root)
                            setHorizontalBias(R.id.img_arrow, 1f)
                            applyTo(cl_root)
                        }
                    }
                    else -> {
                        if (chainStyle != STYLE_NULL) {
                            constrainSet.apply {
                                val chain = IntArray(cl_root.childCount)
                                clone(cl_root)
                                connect(tv_selected.id, ConstraintSet.END, img_arrow.id, ConstraintSet.START, 0)
                                cl_root.children.forEachIndexed { index, view ->
                                    chain[index] = view.id
                                }
                                createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, chain, null, ConstraintSet.CHAIN_SPREAD_INSIDE)
                                applyTo(cl_root)
                            }
                        }
                    }
                }
                if (backgroundFrame != STYLE_NULL)
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

    fun invokeClick(){
        bottomSheet.show()
    }

    fun setAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        bottomSheetView.sheet_rv_more.adapter = adapter
    }

    fun dismiss() {
        bottomSheet.dismiss()
    }

    fun setCloseBtnText(closeStr: String?) {
        closeStr?.let {
            bottomSheetView.sheet_tv_close.text = it
        }
    }

    fun getNowSelectedItemCode(): String? {
        return sheetAdapter?.checkedItemCode
    }

    fun setCloseBtnClickListener(onClicked: () -> Unit) {
        bottomSheetView.sheet_tv_close.setOnClickListener {
            onClicked.invoke()
        }
    }

    var itemSelectedListener: ((data: StatusSheetData) -> Unit)? = null

    fun setOnItemSelectedListener(listener: (data: StatusSheetData) -> Unit) {
        itemSelectedListener = listener
    }

    fun excludeSelected(code: String) {
        if (sheetAdapter == null || selectedTag != code) {
            return
        }

        sheetAdapter!!.dataList.find{ it.code != code }?.let {
            selectedText = it.showName
            selectedTag = it.code
            itemSelectedListener?.invoke(it)
            dismiss()
        }
    }

    private fun setBottomSheet(typedArray: TypedArray) {

        bottomSheetView.apply {
            bottomSheetView.sheet_tv_title.text = bottomSheetTitleText?:typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultBottomSheetTitleText)
            val isShowCloseButton = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_bottomSheetShowCloseButton, true)
            sheet_tv_close.visibility = if (isShowCloseButton) View.VISIBLE else View.GONE
            sheet_tv_close.setOnClickListener {
                bottomSheet.dismiss()
            }

            sheetAdapter = StatusSheetAdapter(StatusSheetAdapter.ItemCheckedListener { isChecked, data ->
                if (isChecked) {
                    selectedText = data.showName
                    selectedTag = data.code
                    itemSelectedListener?.invoke(data)
                    dismiss()
                }
            })

            sheet_rv_more.adapter = sheetAdapter

            sheetAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    val count = if (dataList?.size ?: 1 < 3) dataList?.size else 3
                    val params: ViewGroup.LayoutParams = sheet_rv_more.layoutParams
                    params.height = convertDpToPixel(48f * (count ?: 1), context).toInt()
                    sheet_rv_more.layoutParams = params
                }
            })
        }

        bottomSheet.setContentView(bottomSheetView)
    }
}