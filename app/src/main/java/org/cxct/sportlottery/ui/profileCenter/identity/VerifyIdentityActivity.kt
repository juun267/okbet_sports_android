package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class VerifyIdentityActivity :
    BaseSocketActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val mNavController by lazy { findNavController(R.id.identity_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_identity)

        initToolbar()

    }

    override fun onStart() {
        super.onStart()
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        checkKYCStatus()
    }

    private fun checkKYCStatus() {
        val opt1 = when (viewModel.userInfo.value?.verified) {
            ProfileActivity.VerifiedType.VERIFYING.value,
            ProfileActivity.VerifiedType.VERIFIED_FAILED.value,
            ProfileActivity.VerifiedType.PASSED.value,
            -> true
            else -> {
                false
            }
        }

        val opt2 = mNavController.currentDestination?.id == R.id.verifyKYCFragment

        if (opt1 and opt2) {
            mNavController.navigate(R.id.action_verifyKYCFragment_to_verifyStatusFragment)
        }
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.select_id_type)
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

    fun setToolBarTitle() {
        tv_toolbar_title.text = getString(R.string.select_id_type)
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