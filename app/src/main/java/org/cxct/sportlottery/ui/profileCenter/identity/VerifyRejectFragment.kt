package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentVerifyRejectBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setServiceClick

class VerifyRejectFragment :
    BaseFragment<ProfileCenterViewModel, FragmentVerifyRejectBinding>() {

    override fun onInitView(view: View) {
        binding.tvReasonLabel.text = "${getString(R.string.P332)}: "
        binding.tvReason.text = "${viewModel.userInfo.value?.rejectRemark}"
        binding.btnCS.setServiceClick(childFragmentManager)
        binding.btnSubmit.setOnClickListener {
            (requireActivity() as? VerifyIdentityActivity)?.rejectResubmit()
        }
    }

}