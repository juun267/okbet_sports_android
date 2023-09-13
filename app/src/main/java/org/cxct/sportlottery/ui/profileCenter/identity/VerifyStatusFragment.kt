package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_verify_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setServiceClick

class VerifyStatusFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.userInfo.value?.verified == ProfileActivity.VerifiedType.REVERIFYING.value) {
            (activity as VerifyIdentityActivity).setToolBarTitleForReverify()
        } else {
            (activity as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
        }
        initObserve()
        initView()
        initData()
    }

    private fun initObserve() {
        viewModel.userVerifiedType.observe(viewLifecycleOwner) {
            hideLoading()
            it.getContentIfNotHandled()?.let { verified ->
                when (verified) {
                    ProfileActivity.VerifiedType.PASSED.value -> {
                        img_status.setImageResource(R.drawable.ic_done)
                        txv_status.text = getString(R.string.kyc_verify_successful)
                        tvContactUs.text = getString(R.string.kyc_contact_service_hilight)
                        tvCustomer.text = getString(R.string.kyc_contact_service)
                        layout_verify_progress.visibility = View.GONE
                        layout_verify_success.visibility = View.VISIBLE
                    }
                    else -> {
                        layout_verify_success.visibility = View.GONE
                        layout_verify_progress.visibility = View.VISIBLE
                        tvVerifyProgress.text = "\t\t" + getString(R.string.p090)
                    }
                }

            }
        }
    }

    private fun initData() {
        viewModel.getUserVerified()
    }

    private fun initView() {

        btn_kyc_verify.setServiceClick(childFragmentManager)
        tvCustomer.setServiceClick(childFragmentManager)
//        btn_kyc_verify.setOnClickListener {
//            JumpUtil.toInternalWeb(
//                btn_kyc_verify.context,
//                Constants.getKYVUrl(btn_kyc_verify.context),
//                LocalUtils.getString(R.string.identity)
//            )
//        }
//
//        tvCustomer.setOnClickListener {
//            openService()
//        }
    }

    private fun openService() {
        val serviceUrl = sConfigData?.customerServicveVideoUrl
        if (!serviceUrl.isNullOrBlank()) {
            activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
        }
    }

}