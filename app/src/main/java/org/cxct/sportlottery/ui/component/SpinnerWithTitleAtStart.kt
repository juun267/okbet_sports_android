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

/*

    private val spinnerAdapter by lazy {
        SpinnerRvAdapter(SpinnerRvAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                data.isChecked = true
                tv_selected.text = data.showName
                bottomSheet.dismiss()
            }
        })
    }
*/

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
    /*
    fun setSpinnerData(dataList: List<SpinnerItem>) {
        spinnerAdapter.submitList(dataList)
    }
*/

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_spinner, this, false)
        addView(view)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SpinnerWithTitleAtStartStyle, 0, 0)
//        try {

            setButtonSheet(typedArray)

            view?.apply {
                tv_title.text = typedArray.getString(R.styleable.SpinnerWithTitleAtStartStyle_titleText)

                layout.setOnClickListener {
                    bottomSheet.show()
                }
            }
/*

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
*/

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
//            bottomSheetView.spinner_rv_more.adapter = spinnerAdapter
        }

    }
/*
    class SpinnerRvAdapter(private val clickListener: ItemCheckedListener) : ListAdapter<SpinnerItem, RecyclerView.ViewHolder>(DiffCallback()) {

//        var previousCheckedPosition: Int? = null

        private var mNowCheckedPos:Int? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (holder) {
                is ItemViewHolder -> {
                    val data = getItem(position)

                    setSingleChecked(holder.binding.checkbox, position, data)

                    holder.bind(data)
                }
            }
        }

        private fun setSingleChecked(checkbox: CheckBox, position: Int, data: SpinnerItem) {
            checkbox.setOnClickListener {
                val previousPosition = mNowCheckedPos

                if (previousPosition != null) {
//                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                    getItem(previousPosition).isChecked = false
                    notifyItemChanged(previousPosition)
                }

                mNowCheckedPos = position
//                checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
                checkbox.isChecked = true
                notifyItemChanged(position)
                clickListener.checkedListener(checkbox.isChecked, data)
            }
        }

        class ItemViewHolder private constructor(val binding: ContentBottomSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(data: SpinnerItem) {
                itemView.apply {
                    if (data.isChecked) {
                        checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
                    } else {
                        checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                    }
                }
//                binding.item = data
*//*

                itemView.apply {
                    checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            previousCheckedBtn = buttonView
                            data.isChecked = true
                        }
                    }
                }
*//*

                binding.executePendingBindings()
            }

            companion object {
                fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = ContentBottomSheetItemBinding.inflate(layoutInflater, parent, false)
                    return ItemViewHolder(binding)
                }
            }

        }

        class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: SpinnerItem) -> Unit) {
            fun onChecked(isChecked: Boolean, data: SpinnerItem) = checkedListener(isChecked, data)
        }

    }


    class DiffCallback : DiffUtil.ItemCallback<SpinnerItem>() {
        override fun areItemsTheSame(oldItem: SpinnerItem, newItem: SpinnerItem): Boolean {
            return oldItem.showName == newItem.showName
        }

        override fun areContentsTheSame(oldItem: SpinnerItem, newItem: SpinnerItem): Boolean {
            return oldItem == newItem
        }

    }
    */
}