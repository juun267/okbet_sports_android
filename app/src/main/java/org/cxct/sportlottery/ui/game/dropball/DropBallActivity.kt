package org.cxct.sportlottery.ui.game.dropball

import android.webkit.JavascriptInterface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.databinding.ActivityDropBallBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.util.LogUtil

class DropBallActivity : BaseActivity<ProfileModel, ActivityDropBallBinding>(){

    override fun pageName() = "DropBall活动页面"

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.toolBar.titleText = "DropBall"
        binding.toolBar.setOnBackPressListener {
           finish()
        }
        binding.okWebView.addJavascriptInterface(JsBridge(),"app")
        Constants.appendParams(Constants.getPromoDropBall())?.let {
            LogUtil.d("getPromoDropBall=$it")
            binding.okWebView.loadUrl(it)
        }
        viewModel.inviteUserDetail()
    }

    inner class JsBridge {
        @JavascriptInterface
        fun openShareNative() {
            val inviteCode = viewModel.inviteUserDetailEvent.value
            if (inviteCode.isNullOrEmpty()) return
            post {
                DropBallShareDialog.newInstance(inviteCode).show(supportFragmentManager)
            }
        }
    }

}