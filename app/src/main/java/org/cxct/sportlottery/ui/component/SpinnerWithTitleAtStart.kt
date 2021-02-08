package org.cxct.sportlottery.ui.component

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.content_bottom_sheet_item.view.*
import kotlinx.android.synthetic.main.custom_spinner.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetItemBinding
import org.cxct.sportlottery.network.custom.SpinnerItem

class SpinnerWithTitleAtStart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.SpinnerWithTitleAtStartStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.SpinnerWithTitleAtStartStyle_spinnerLayout, R.layout.dialog_bottom_sheet_custom) }
    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

    var isShowCloseButton
        get() = bottomSheetView.spinner_tv_close.visibility == View.VISIBLE
        set(value) {
            bottomSheetView.spinner_tv_close.visibility = if (value) View.VISIBLE else View.GONE
        }

    fun <T> setAdapter (adapter: ListAdapter<T, RecyclerView.ViewHolder>) {
        bottomSheetView.spinner_rv_more.adapter = adapter
    }

    fun setText(testStr: String) {
        tv_selected.text = testStr
    }

    fun dismiss() {
        bottomSheet.dismiss()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_spinner, this, false)
        addView(view)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SpinnerWithTitleAtStartStyle, 0, 0)
        try {
            setButtonSheet(typedArray)

            view?.apply {
                tv_title.text = typedArray.getString(R.styleable.SpinnerWithTitleAtStartStyle_titleText)

                layout.setOnClickListener {
                    bottomSheet.show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }

    private fun setButtonSheet(typedArray: TypedArray) {
        bottomSheet.setContentView(bottomSheetView)
        //避免bottomSheet與listView的滑動發生衝突
        bottomSheet.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    this@SpinnerWithTitleAtStart.bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })

        bottomSheetView.apply {
            bottomSheetView.spinner_tv_title.text = typedArray.getText(R.styleable.SpinnerWithTitleAtStartStyle_spinnerTitle)
            val isShowCloseButton = typedArray.getBoolean(R.styleable.SpinnerWithTitleAtStartStyle_spinnerIsShowCloseButton, true)
            spinner_tv_close.visibility = if (isShowCloseButton) View.VISIBLE else View.GONE
        }

    }
}