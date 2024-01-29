package org.cxct.sportlottery.ui.chat

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.databinding.ActivityChatBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.setTextTypeFace

class ChatActivity : BaseActivity<ChatViewModel, ActivityChatBinding>() {

    private val marqueeAdapter by lazy { ChatMarqueeAdapter() }

    override fun onInitView() {

        initToolbar()
        initMarquee()
        initObserve()
        viewModel.initChatClient(this)
    }

    fun initToolbar() {
        binding.bvToolBar.setupWith(window?.decorView?.rootView as ViewGroup)
            .setFrameClearDrawable(window?.decorView?.background)
            .setBlurRadius(2f)
        binding.bvToolBar.apply {
            layoutParams.height = ImmersionBar.getStatusBarHeight(this@ChatActivity)+resources.getDimensionPixelOffset(R.dimen.tool_bar_height)
            this.layoutParams = layoutParams
        }
        binding.ivBack.setOnClickListener {onBackPressed() }
        binding.tvTitle.setTextTypeFace(Typeface.BOLD)
        binding.rlToolbar.apply {
            (layoutParams as FrameLayout.LayoutParams).topMargin = ImmersionBar.getStatusBarHeight(this@ChatActivity)
            this.layoutParams = layoutParams
        }
        ImmersionBar.with(this)
            .statusBarDarkFont(!MultiLanguagesApplication.isNightMode)
            .fitsSystemWindows(false)
            .init()
    }

    private fun initMarquee() {
        binding.rvMarquee.adapter = marqueeAdapter
        binding.rvMarquee.bindLifecycler(this)
    }

    private fun initObserve() = viewModel.run {

        chatEvent.collectWith(lifecycleScope) { chatEvent ->

            if (chatEvent is ChatEvent.UpdateMarquee) {
                marqueeAdapter.setData(chatEvent.marqueeList)
                binding.rvMarquee.startAuto(false)
                return@collectWith
            }

            if (chatEvent is ChatEvent.ShowPhoto) {
                val chatPhotoFragment = ChatPhotoFragment.newInstance(chatEvent.photoUrl)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_chat_photo, chatPhotoFragment)
                    .addToBackStack(null)
                    .commit()
            }

        }

        connStatus.collectWith(lifecycleScope) {
            if (!it) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.N655),
                    buttonText = null,
                    { finish() },
                    isError = true,
                    hasCancle = false
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.rvMarquee.startAuto()
    }

    override fun onStop() {
        super.onStop()
        binding.rvMarquee.stopAuto()
    }


}
