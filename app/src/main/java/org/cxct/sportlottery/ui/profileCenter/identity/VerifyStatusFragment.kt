package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_verify_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.JumpUtil

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
        viewModel.userVerifiedType.observe(viewLifecycleOwner, {
            hideLoading()
            it.getContentIfNotHandled()?.let { verified ->
                img_status.isVisible = true
                txv_status.isVisible = true

                when (verified) {
                    ProfileActivity.VerifiedType.PASSED.value -> {
                        img_status.setImageResource(R.drawable.ic_done)
                        txv_status.text = resources.getText(R.string.kyc_verify_successful)
                        btn_kyc_verify.setOnClickListener {
                            openService()
                        }
                    }
                    else -> {
                        img_status.setImageResource(R.drawable.ic_waiting_time)
                        txv_status.text = resources.getText(R.string.kyc_you_wait)
                        btn_kyc_verify.setOnClickListener {
                            JumpUtil.toInternalWeb(
                                requireContext(),
                                Constants.getKYVUrl(requireContext()),
                                resources.getString(R.string.identity)
                            )
                        }
                    }
                }
            }
        })
    }

    private fun initData() {
        viewModel.getUserVerified()
    }

    private fun initView() {
        btn_kyc_verify.setOnClickListener {
            JumpUtil.toInternalWeb(
                btn_kyc_verify.context,
                Constants.getKYVUrl(btn_kyc_verify.context),
                resources.getString(R.string.identity)
            )
        }

        btn_service.setOnClickListener {
            openService()
        }
    }

    fun openService() {
        val serviceUrl = sConfigData?.customerServiceUrl
        val serviceUrl2 = sConfigData?.customerServiceUrl2
        when {
            !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                activity?.supportFragmentManager?.let { it1 ->
                    ServiceDialog().show(
                        it1,
                        null
                    )
                }
            }
            serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
            }
            !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
            }
        }
    }

}