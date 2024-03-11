package org.cxct.sportlottery.ui.feedback.record

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentFeedbackDetailBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 反馈记录-反馈详情
 */
class FeedbackDetailFragment : BaseFragment<FeedbackViewModel, FragmentFeedbackDetailBinding>() {

    private val feedbackListDetailAdapter by lazy {
        viewModel.userInfo?.value?.userId?.toInt()?.let { FeedbackListDetailAdapter(it) }
    }
    override fun onInitView(view: View) {
        viewModel.showToolbar(false)
        viewModel.setToolbarName(getString(R.string.feedback_detail))
        viewModel.fbQueryDetail()
        initRecyclerView()
        initDataLive()
        initButton()
    }


    private fun initButton()=binding.run {
         btnSubmitNew.setOnClickListener {
                if (etContent.text.trim().isNotEmpty()) { viewModel.fbReply(etContent.text.toString()) }
        }
        btnSubmitNew.setTitleLetterSpacing()
    }

    private fun initRecyclerView()=binding.run {
        rvContent.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvContent.adapter = feedbackListDetailAdapter
    }

    private fun initDataLive()=binding.run {
        viewModel.feedbackDetail.observe(viewLifecycleOwner) {
            etContent.text.clear()
            feedbackListDetailAdapter?.setList(it)
            rvContent.scrollToPosition((feedbackListDetailAdapter?.itemCount ?: 0) - 1)
        }
    }

}