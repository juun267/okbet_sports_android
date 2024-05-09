package org.cxct.sportlottery.ui.profileCenter.identity

import android.view.View
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.CenterImageSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.KYCEvent
import org.cxct.sportlottery.databinding.FragmentVerifyIdentityKyc2Binding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.greenrobot.eventbus.ThreadMode

class VerifyKYCFragment2: BaseFragment<ProfileCenterViewModel, FragmentVerifyIdentityKyc2Binding>() {

    override fun onInitView(view: View) {
        val bg = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_F8F9FD)
        val bg1 = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_dbdeeb)
        binding.layout1.background = bg
        binding.layout2.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_F8F9FD)
        binding.layout3.background = bg
        binding.ivImg1.background = bg1
        binding.ivImg2.background = bg1
        binding.ivImg3.background = bg1

        val dp4 = 4.dp.toFloat()
        binding.text2.text = "* ${getString(R.string.real_name)}  \n* ${getString(R.string.P253)}  \n* ${getString(R.string.birthofdate)}".replaceSpan("*") {
            CenterImageSpan(DrawableCreator.Builder()
                .setSizeHeight(dp4)
                .setSizeWidth(dp4)
                .setSolidColor(view.context.getColor(R.color.color_025BE8))
                .setCornersRadius(90f).build())
        }

        binding.btnStart.setOnClickListener { IDTypeSelectorDialog(requireActivity(), lifecycle).show() }
    }

    override fun onBindViewStatus(view: View) {
        EventBusUtil.targetLifecycle(this)
        val parentActivity = activity
        if (parentActivity is VerifyIdentityActivity) {
            parentActivity.setToolBar(getString(R.string.identity))
        }
    }

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    fun onKYCEvent(kycEvent: KYCEvent) {
        activity?.finish()
    }

}