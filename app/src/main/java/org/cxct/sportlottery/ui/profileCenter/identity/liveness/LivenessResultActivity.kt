package org.cxct.sportlottery.ui.profileCenter.identity.liveness

import android.content.Intent
import android.graphics.Paint
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityLivenessResultBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyHandheldActivity
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyNotFullyActivity
import org.cxct.sportlottery.util.setServiceClick

class LivenessResultActivity: BaseActivity<ProfileCenterViewModel, ActivityLivenessResultBinding>() {

    override fun pageName()= "活体检测结果"
    private val tryAgain by lazy { intent.getBooleanExtra("tryAgain",true) }

    override fun onInitView() {
        setStatusbar(darkFont = true)
        binding.toolBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.clLiveChat.setServiceClick(supportFragmentManager)
        setReason()
    }

    fun setReason(){
        if (tryAgain){
            binding.btnStart.text = getString(R.string.B684)
            binding.tvhandheldId.visible()
            binding.tvhandheldId.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            binding.tvhandheldId.setOnClickListener {
                startActivity<VerifyHandheldActivity>()
            }
            binding.btnStart.setOnClickListener {
                finish()
            }
        }else{
            binding.tvhandheldId.gone()
            binding.btnStart.text = getString(R.string.P288_1)
            binding.btnStart.setOnClickListener {
                startActivity(VerifyIdentityActivity::class.java){
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
        }
    }

    override fun onBackPressed() {
        MainTabActivity.reStart(this)
    }


}