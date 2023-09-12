package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class VerifyIdentityActivity :
    BaseSocketActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val mNavController by lazy { findNavController(R.id.identity_container) }

    private val loadingHolder by lazy { Gloading.wrapView(findViewById(R.id.identity_container)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_identity)
        initToolbar()
        initObserver()

        val verified = viewModel.userInfo.value?.verified
        if (verified != ProfileActivity.VerifiedType.NOT_YET.value
            && verified != ProfileActivity.VerifiedType.PASSED.value) {
            loadingHolder.withRetry {
                loadingHolder.showLoading()
                viewModel.loadUserInfo()
            }
            loadingHolder.go()
        } else {
            post{ checkKYCStatus() }
        }

    }

    private fun initObserver() {
        viewModel.userInfoEvent.observe(this) {
            checkKYCStatus()
            loadingHolder.showLoadSuccess()
        }
    }

    private fun checkKYCStatus() {
        val opt1 = when (viewModel.userInfo.value?.verified) {
            ProfileActivity.VerifiedType.VERIFYING.value,
//            ProfileActivity.VerifiedType.VERIFIED_FAILED.value,
            ProfileActivity.VerifiedType.PASSED.value,
            ProfileActivity.VerifiedType.VERIFIED_WAIT.value,
            ProfileActivity.VerifiedType.REVERIFYING.value -> {
                R.id.verifyStatusFragment
            }
            ProfileActivity.VerifiedType.REVERIFIED_NEED.value -> {
                R.id.reverifyKYCFragment
            }
            else -> {
                R.id.verifyKYCFragment
            }
        }

        if (mNavController.currentDestination?.id != opt1) {
            mNavController.navigate(opt1)
        }
    }

    private fun initToolbar() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        tv_toolbar_title.setTitleLetterSpacing()
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }


    fun setToolBar(title: String) {
        tv_toolbar_title.text = title
    }

    fun setToolBarTitleForDetail() {
        tv_toolbar_title.text = getString(R.string.scan_tool_bar)
    }

    fun setToolBarTitleForReverify() {
        tv_toolbar_title.text = getString(R.string.P211)
    }

    override fun onBackPressed() {
        when (mNavController.currentDestination?.id) {
            R.id.credentialsFragment -> {
                finish()
            }
        }
        super.onBackPressed()
    }
}