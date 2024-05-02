package org.cxct.sportlottery.ui.profileCenter.identity

import androidx.navigation.fragment.NavHostFragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityVerifyIdentityBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing

class VerifyIdentityActivity :
    BaseActivity<ProfileCenterViewModel, ActivityVerifyIdentityBinding>() {

    private val mNavController by lazy { (supportFragmentManager.findFragmentById(R.id.identity_container) as NavHostFragment).navController }

    private val loadingHolder by lazy { Gloading.wrapView(findViewById(R.id.identity_container)) }

    override fun onInitView() {
        initToolbar()
        initObserver()
    }

    private var isFirst = true

    override fun onStart() {
        super.onStart()

        if (!isFirst) {
            return
        }
        isFirst = false
        val verified = viewModel.userInfo.value?.verified
        if (verified != VerifiedType.NOT_YET.value
            && verified != VerifiedType.PASSED.value) {
            loadingHolder.withRetry {
                loadingHolder.showLoading()
                viewModel.getUserInfo()
            }
            loadingHolder.go()
        } else {
            post{ checkKYCStatus() }
        }
    }

    private fun initObserver() {
        viewModel.userInfo.observe(this) {
            checkKYCStatus()
            loadingHolder.showLoadSuccess()
        }
        viewModel.reVerifyResult.observe(this) {
            if (it.succeeded()){
                viewModel.getUserInfo()
            }else{
                loadingHolder.showLoadFailed()
                ToastUtil.showToast(this,it.msg)
            }
        }
    }

    private fun checkKYCStatus() {
        val opt1 = when (viewModel.userInfo.value?.verified) {
            VerifiedType.VERIFYING.value,
//            VerifiedType.VERIFIED_FAILED.value,
            VerifiedType.PASSED.value,
            VerifiedType.VERIFIED_WAIT.value,
            VerifiedType.REVERIFYING.value -> {
                R.id.verifyStatusFragment
            }
            VerifiedType.REVERIFIED_NEED.value -> {
                R.id.reverifyKYCFragment
            }
            VerifiedType.REJECT.value -> {
                R.id.verifyRejectFragment
            }
            else -> {
                R.id.verifyKYCFragment
            }
        }

        if (mNavController.currentDestination?.id != opt1) {
            mNavController.navigate(opt1)
        }
    }

    private fun initToolbar()=binding.toolbar.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        tvToolbarTitle.setTitleLetterSpacing()
        btnToolbarBack.setOnClickListener {
            onBackPressed()
        }
    }


    fun setToolBar(title: String) {
        binding.toolbar.tvToolbarTitle.text = title
    }

    fun setToolBarTitleForDetail() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.scan_tool_bar)
    }

    fun setToolBarTitleForReverify() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.P211_1)
    }

    override fun onBackPressed() {
        when (mNavController.currentDestination?.id) {
            R.id.credentialsFragment -> {
                finish()
            }
        }
        super.onBackPressed()
    }
    fun rejectResubmit(){
        loadingHolder.showLoading()
        viewModel.reVerify()
    }
}