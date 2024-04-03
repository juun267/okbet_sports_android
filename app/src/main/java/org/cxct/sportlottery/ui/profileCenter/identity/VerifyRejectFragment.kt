package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentVerifyRejectBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.JumpUtil

class VerifyRejectFragment :
    BaseFragment<ProfileCenterViewModel, FragmentVerifyRejectBinding>() {

    override fun onInitView(view: View) {
        binding.tvReasonLabel.text = "${getString(R.string.P332)}: "
        binding.tvReason.text = "${viewModel.userInfo.value?.iconUrl}: "
        binding.btnCS.setOnClickListener {
           openService()
        }
        binding.btnSubmit.setOnClickListener {
            (requireActivity() as VerifyIdentityActivity).rejectResubmit()
        }
    }

    private fun openService() {
        val serviceUrl = sConfigData?.customerServicveVideoUrl
        if (!serviceUrl.isNullOrBlank()) {
            activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
        }
    }

}