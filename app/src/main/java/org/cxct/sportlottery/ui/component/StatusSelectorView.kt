package org.cxct.sportlottery.ui.component

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import org.cxct.sportlottery.R

class StatusSelectorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    companion object {
        const val STYLE_NULL = -99
    }

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_sheetLayout, R.layout.dialog_bottom_sheet_custom) }
    val bottomSheetView: View by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

    var selectedText: String? = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
        get() = tv_selected.text.toString()
        set(value) {
            field = value
            tv_selected.text = value
        }

    var selectedTag: String? = ""
        get() = tv_selected.tag.toString()
        set(value) {
            field = value
            tv_selected.tag = value
        }

    var selectedTextColor: Int? = null
        get() = tv_selected.currentTextColor
        set(value) {
            field = value
            if (value != null) tv_selected.setTextColor(ContextCompat.getColor(context, value))
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
            setButtonSheet(typedArray)

            view?.apply {
                setOnClickListener {
                    bottomSheet.show()
                }
                val chainStyle = typedArray.getInt(R.styleable.StatusBottomSheetStyle_horizontalChainStyle, STYLE_NULL)
                if (chainStyle != STYLE_NULL) {
                    val constrainSet = ConstraintSet()
                    val chain = IntArray(cl_root.childCount)
                    constrainSet.apply {
                        clone(cl_root)
                        cl_root.children.forEachIndexed { index, view ->
                            chain[index] = view.id
                        }
                        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, chain, null, ConstraintSet.CHAIN_SPREAD_INSIDE)
                        applyTo(cl_root)
                    }
                }
                tv_selected.tag = ""
                tv_selected.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
            }

            this.setOnClickListener {
                bottomSheet.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

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

    fun setCloseBtnClickListener(onClicked: () -> Unit) {
        bottomSheetView.sheet_tv_close.setOnClickListener {
            onClicked.invoke()
        }
    }

    private fun setButtonSheet(typedArray: TypedArray) {

        bottomSheetView.apply {
            bottomSheetView.sheet_tv_title.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultBottomSheetTitleText)
            val isShowCloseButton = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_bottomSheetShowCloseButton, true)
            sheet_tv_close.visibility = if (isShowCloseButton) View.VISIBLE else View.GONE
            sheet_tv_close.setOnClickListener {
                bottomSheet.dismiss()
            }
        }

        bottomSheet.setContentView(bottomSheetView)

    }
}


