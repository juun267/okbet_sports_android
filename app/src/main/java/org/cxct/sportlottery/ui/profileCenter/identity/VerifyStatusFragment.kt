package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_verify_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LocalUtils

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
        (activity as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
        initObserve()
        initView()
        initData()
    }

    private fun initObserve() {
        viewModel.userVerifiedType.observe(viewLifecycleOwner) {
            hideLoading()
            it.getContentIfNotHandled()?.let { verified ->
                img_status.isVisible = true
                txv_status.isVisible = true

                when (verified) {
                    ProfileActivity.VerifiedType.PASSED.value -> {
                        img_status.setImageResource(R.drawable.ic_done)
                        txv_status.text = LocalUtils.getString(R.string.kyc_verify_successful)
                    }
                    else -> {
                        img_status.setImageResource(R.drawable.ic_waiting_time)
                        txv_status.text = "Thank you for verifying with us, please kindly wait up to 24 hours for us to process your request."
                    }
                }
            }
        }
    }

    private fun initData() {
        viewModel.getUserVerified()
    }

    private fun initView() {
    }

    fun openService() {
        val serviceUrl = sConfigData?.customerServicveVideoUrl
        if (!serviceUrl.isNullOrBlank()) {
            activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
        }
    }

}