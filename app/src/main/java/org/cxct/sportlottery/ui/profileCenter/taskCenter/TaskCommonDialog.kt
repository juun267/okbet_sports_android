package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.DialogTaskCommonBinding
import org.cxct.sportlottery.databinding.ItemCommonTextviewLineBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration

const val TITLE = "title"
const val CONTENT = "content"

class TaskCommonDialog : BaseDialog<BaseViewModel, DialogTaskCommonBinding>() {

    private var mTitle = ""
    private var mContent = ""

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        fun newInstance(title: String, content: String) = TaskCommonDialog().apply {
            arguments = Bundle().apply {
                putString(TITLE, title)
                putString(CONTENT, content)
            }
        }
    }

    override fun onInitView() = binding.run {
        mTitle = arguments?.getString(TITLE) ?: ""
        mContent = arguments?.getString(CONTENT) ?: ""

        root.setOnClickListener { dismissAllowingStateLoss() }
        tvConfirm.setOnClickListener { dismissAllowingStateLoss() }
        tvTitle.text = mTitle

        val linesText = mContent.split("\n\n")
        rvContent.setLinearLayoutManager()
        rvContent.addItemDecoration(SpaceItemDecoration(requireContext(),R.dimen.margin_8))
        rvContent.adapter = TextLineAdapter().apply {
            setList(linesText)
        }
    }
    inner class TextLineAdapter : BindingAdapter<String,ItemCommonTextviewLineBinding>(){
        override fun onBinding(
            position: Int,
            binding: ItemCommonTextviewLineBinding,
            item: String,
        ) {
            binding.root.text = item
        }

    }

}
