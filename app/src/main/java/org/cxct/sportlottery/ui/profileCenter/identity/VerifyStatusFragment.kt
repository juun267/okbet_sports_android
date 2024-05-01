package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.databinding.FragmentVerifyStatusBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setServiceClick

class VerifyStatusFragment :
    BaseFragment<ProfileCenterViewModel,FragmentVerifyStatusBinding>() {

    override fun onInitView(view: View) {
        if (viewModel.userInfo.value?.verified == VerifiedType.REVERIFYING.value) {
            (activity as VerifyIdentityActivity).setToolBarTitleForReverify()
        } else {
            (activity as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
        }
        initObserve()
        initView()
        initData()
    }

    private fun initObserve()=binding.run {
        viewModel.userVerifiedType.observe(viewLifecycleOwner) {
            hideLoading()
            it.getContentIfNotHandled()?.let { verified ->
                when (verified) {
                    VerifiedType.PASSED.value -> {
                        imgStatus.setImageResource(R.drawable.ic_done)
                        txvStatus.text = getString(R.string.kyc_verify_successful)
                        tvContactUs.text = getString(R.string.kyc_contact_service_hilight)
                        tvCustomer.text = getString(R.string.kyc_contact_service)
                        layoutVerifyProgress.visibility = View.GONE
                        layoutVerifySuccess.visibility = View.VISIBLE
                    }
                    else -> {
                        layoutVerifySuccess.visibility = View.GONE
                        layoutVerifyProgress.visibility = View.VISIBLE
                        tvVerifyProgress.text = "\t\t" + getString(R.string.p090)
                    }
                }

            }
        }
    }

    private fun initData() {
        viewModel.getUserVerified()
    }

    private fun initView()=binding.run {

        btnKycVerify.setServiceClick(childFragmentManager)
        tvCustomer.setServiceClick(childFragmentManager)
    }

}