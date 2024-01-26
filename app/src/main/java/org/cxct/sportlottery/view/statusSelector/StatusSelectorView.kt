package org.cxct.sportlottery.view.statusSelector

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetCustomBinding
import org.cxct.sportlottery.databinding.ViewStatusSelectorBinding
import org.cxct.sportlottery.ui.common.adapter.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.MetricsUtil.convertDpToPixel
import splitties.systemservices.layoutInflater

class StatusSelectorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    companion object {
        const val STYLE_NULL = -99
    }

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.StatusBottomSheetStyle, 0, 0) }
    val binding by lazy { DialogBottomSheetCustomBinding.inflate(layoutInflater)}
    val viewBinding by lazy { ViewStatusSelectorBinding.inflate(layoutInflater)}
    private val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }
    private val arrowImg by lazy { typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_arrowSrc, R.drawable.ic_arrow_gray)}
    private val textGravity by lazy { typedArray.getInt(R.styleable.StatusBottomSheetStyle_textGravity, 0x11)}

    var selectedText: String? = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
        get() = viewBinding.tvSelected.text.toString()
        set(value) {
            field = value
            viewBinding.tvSelected.text = value
        }

    var bottomSheetTitleText: String? = null
        get() = if (binding.sheetTvTitle.text.toString().isEmpty()) typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultBottomSheetTitleText) else binding.sheetTvTitle.text.toString()
        set(value) {
            field = value
            binding.sheetTvTitle.text = value
        }

    var selectedTag: String? = ""
        get() = viewBinding.tvSelected.tag?.toString()
        set(value) {
            field = value
            viewBinding.tvSelected.tag = value
            sheetAdapter?.defaultCheckedCode = value
        }

    var selectedTextColor: Int? = null
        get() = viewBinding.tvSelected.currentTextColor
        set(value) {
            field = value
            if (value != null) viewBinding.tvSelected.setTextColor(ContextCompat.getColor(context, value))
        }

    private var sheetAdapter: StatusSheetAdapter?= null

    var dataList = sheetAdapter?.dataList
        get() = sheetAdapter?.dataList
        set(value) {
            field = value
            sheetAdapter?.dataList = value ?: listOf()

            viewBinding.tvSelected.tag = value?.firstOrNull()?.code
            sheetAdapter?.defaultCheckedCode = value?.firstOrNull()?.code

            sheetAdapter?.notifyDataSetChanged()
        }

    var isShowAllCheckBoxView: Boolean? = false
        get() = binding.checkboxSelectAll.isVisible
        set(value) {
            field = value
            if (value == true) binding.checkboxSelectAll.visibility = View.VISIBLE
            else binding.checkboxSelectAll.visibility = View.GONE
        }

    init {
        addView(viewBinding.root)

        try {
            setBottomSheet(typedArray)

            viewBinding.run {
                root.setOnClickListener {
                    bottomSheet.show()
                }
                imgArrow.setImageResource(arrowImg)

                val chainStyle = typedArray.getInt(R.styleable.StatusBottomSheetStyle_horizontalChainStyle, STYLE_NULL)
                val arrowAtEnd = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_arrowAtEnd, false)
                val backgroundFrame = typedArray.getResourceId(R.styleable.StatusBottomSheetStyle_backgroundFrame, STYLE_NULL)

                val constrainSet = ConstraintSet()

                when {
                    arrowAtEnd -> {
                        constrainSet.apply {
                            clone(clRoot)
                            setHorizontalBias(R.id.img_arrow, 1f)
                            applyTo(clRoot)
                        }
                    }
                    else -> {
                        if (chainStyle != STYLE_NULL) {
                            constrainSet.apply {
                                val chain = IntArray(clRoot.childCount)
                                clone(clRoot)
                                connect(tvSelected.id, ConstraintSet.END, imgArrow.id, ConstraintSet.START, 0)
                                clRoot.children.forEachIndexed { index, view ->
                                    chain[index] = view.id
                                }
                                createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, chain, null, ConstraintSet.CHAIN_SPREAD_INSIDE)
                                applyTo(clRoot)
                            }
                        }
                    }
                }
                if (backgroundFrame != STYLE_NULL)
                    clRoot.background = ContextCompat.getDrawable(context, R.drawable.frame_color_silver_light_stroke)

                tvSelected.tag = ""
                tvSelected.text = typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultStatusText)
                tvSelected.gravity = textGravity
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
        binding.sheetRvMore.adapter = adapter
    }

    fun dismiss() {
        bottomSheet.dismiss()
    }

    fun setCloseBtnText(closeStr: String?) {
        closeStr?.let {
            binding.sheetTvClose.text = it
        }
    }

    fun getNowSelectedItemCode(): String? {
        return sheetAdapter?.checkedItemCode
    }

    fun setCloseBtnClickListener(onClicked: () -> Unit) {
        binding.sheetTvClose.setOnClickListener {
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

    private fun setBottomSheet(typedArray: TypedArray)=binding.run {

            sheetTvTitle.text = bottomSheetTitleText?:typedArray.getString(R.styleable.StatusBottomSheetStyle_defaultBottomSheetTitleText)
            val isShowCloseButton = typedArray.getBoolean(R.styleable.StatusBottomSheetStyle_bottomSheetShowCloseButton, true)
            sheetTvClose.visibility = if (isShowCloseButton) View.VISIBLE else View.GONE
            sheetTvClose.setOnClickListener {
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

            sheetRvMore.adapter = sheetAdapter

            sheetAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    val count = if (dataList?.size ?: 1 < 3) dataList?.size else 3
                    val params: ViewGroup.LayoutParams = sheetRvMore.layoutParams
                    params.height = convertDpToPixel(48f * (count ?: 1), context).toInt()
                    sheetRvMore.layoutParams = params
                }
            })

        bottomSheet.setContentView(binding.root)
    }
}