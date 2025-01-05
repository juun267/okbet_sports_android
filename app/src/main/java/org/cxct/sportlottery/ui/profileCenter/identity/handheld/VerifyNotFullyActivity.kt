package org.cxct.sportlottery.ui.profileCenter.identity.handheld

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityVerifyNotFullyBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.liveness.LivenessStartActivity

class VerifyNotFullyActivity: BaseActivity<ProfileCenterViewModel, ActivityVerifyNotFullyBinding>() {

    override fun pageName() = "准备开始上传手持照片"

    companion object{
        const val TYPE = "type"
    }

    /**
     * 0是kyc入口，1为提款入口
     */
    private val type by lazy { intent.getIntExtra(TYPE,0) }

    override fun onInitView() {
        setStatusbar(darkFont = true)
        binding.toolBar.titleText = when(type){
            1-> getString(R.string.withdraw)
            else-> getString(R.string.identity)
        }
        binding.toolBar.setOnBackPressListener {
            finish()
        }
        binding.btnStart.setOnClickListener {
            startActivity<LivenessStartActivity>()
        }
    }

}