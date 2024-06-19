package org.cxct.sportlottery.ui.profileCenter.invite

import android.content.res.ColorStateList
import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.databinding.ActivityInviteBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel

class InviteActivity : BaseActivity<ProfileModel, ActivityInviteBinding>(){

    override fun onInitView() {
        setStatusbar(R.color.color_025BE8,false)
       binding.toolBar.apply {
            binding.appBarLayout.setBackgroundResource(R.color.color_025BE8)
            binding.tvToolbarTitle.setTextColor(getColor(R.color.color_FFFFFF))
            binding.btnToolbarBack.imageTintList = ColorStateList.valueOf(getColor(R.color.color_FFFFFF))
            setOnBackPressListener {
               finish()
            }
        }
        binding.okWebView.addJavascriptInterface(JsBridge(),"app")
        Constants.appendParams(Constants.getInviteUrl())?.let {  binding.okWebView.loadUrl(it) }
        viewModel.inviteUserDetail()
    }

    inner class JsBridge {
        @JavascriptInterface
        fun openShareNative() {
            val inviteCode = viewModel.inviteUserDetailEvent.value
            if (inviteCode.isNullOrEmpty()) return
            post {
                InviteDialog.newInstance(inviteCode).show(supportFragmentManager)
            }
        }
    }

}