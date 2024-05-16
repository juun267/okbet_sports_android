package org.cxct.sportlottery.ui.sport.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import org.cxct.sportlottery.databinding.PopOkplaySelectBinding

class OKPlaySelectPop(val context: Context, val onItemClickListener: (position:Int) -> Unit) : PopupWindow(context) {

    private val binding by lazy { PopOkplaySelectBinding.inflate(LayoutInflater.from(context)) }
    var tvName: TextView? = null
    var ivArrow: ImageView? = null
    init {
        contentView = binding.root
        initView()
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private fun initView() {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        elevation = 0f
        setBackgroundDrawable(null)
        setOnDismissListener {
//            tvName?.text = binding.rgMenu.findViewById<RadioButton>(binding.rgMenu.checkedRadioButtonId).text
            ivArrow?.rotation = 0f
        }
        binding.tvOKSport.setOnClickListener {
            onItemClickListener.invoke(0)
            dismiss()
        }
        binding.tvOKPlay.setOnClickListener {
            onItemClickListener.invoke(1)
            dismiss()
        }
    }
    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
        ivArrow?.rotation=180f
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        super.showAsDropDown(anchor, xoff, yoff)
        ivArrow?.rotation=180f
    }
}