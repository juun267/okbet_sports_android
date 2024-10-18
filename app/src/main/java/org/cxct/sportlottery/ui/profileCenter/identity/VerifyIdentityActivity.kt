package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Intent
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityVerifyIdentityBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyNotFullyFragment
import org.cxct.sportlottery.util.*

class VerifyIdentityActivity :
    BaseActivity<ProfileCenterViewModel, ActivityVerifyIdentityBinding>() {

    override fun pageName() = "KYC状态页面"

    private val loadingHolder by lazy { Gloading.wrapView(binding.root) }
    private val fragmentHelper2 by lazy { FragmentHelper2(supportFragmentManager, binding.fragmentContainer.id) }
    private var backToMainPage = false //点击返回按钮时，是否需要回到主页
    private var title: String? = null //外部页面带标题进来

    override fun onInitView() {
        getIntentParams(intent)
        initToolbar()
        initObserver()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            getIntentParams(it)
        }
    }

    private fun getIntentParams(intent: Intent){
        backToMainPage = intent.getBooleanExtra("backToMainPage", false)
        title = intent.getStringExtra("title")
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
        val userInfo = viewModel.userInfo.value!!
        if (userInfo.fullVerified==1){
            fragmentHelper2.show(VerifyStatusFragment::class.java)
        }else{
            when (userInfo.verified) {
                VerifiedType.VERIFYING.value,
                VerifiedType.VERIFIED_WAIT.value,
                VerifiedType.REVERIFYING.value -> {
                    fragmentHelper2.show(VerifyStatusFragment::class.java)
                }
                VerifiedType.REVERIFIED_NEED.value -> {
                    fragmentHelper2.show(ReverifyKYCFragment::class.java)
                }
                VerifiedType.REJECT.value -> {
                    fragmentHelper2.show(VerifyRejectFragment::class.java)
                }
                VerifiedType.PASSED.value->{
                   fragmentHelper2.show(VerifyNotFullyFragment::class.java)
                }
                else -> {
                    fragmentHelper2.show(VerifyKYCFragment2::class.java)
                }
            }
        }
        if (!title.isNullOrEmpty()){
            setToolBar(title!!)
        }
    }
    fun showFragment(clazz: Class<out Fragment>){
        fragmentHelper2.show(clazz)
    }

    private fun initToolbar()=binding.toolbar.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        tvToolbarTitle.setTitleLetterSpacing()
        btnToolbarBack.setOnClickListener {
            if (backToMainPage){
                MainTabActivity.reStart(this@VerifyIdentityActivity)
            }else{
                finish()
            }
        }
    }


    fun setToolBar(title: String) {
        binding.toolbar.tvToolbarTitle.text = title
    }

    fun setToolBarTitleForReverify() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.P211_1)
    }

    fun rejectResubmit(){
        loadingHolder.showLoading()
        viewModel.reVerify()
    }

}