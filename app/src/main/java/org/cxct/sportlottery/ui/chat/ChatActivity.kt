package org.cxct.sportlottery.ui.chat

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.databinding.ActivityChatBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTextTypeFace

class ChatActivity : BaseSocketActivity<ChatViewModel>(ChatViewModel::class) {

    private lateinit var binding: ActivityChatBinding


    private val marqueeAdapter by lazy {
        ChatMarqueeAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initMarquee()
        initObserve()
        viewModel.attchLifecycleOwner(this)
    }

    fun initToolbar() {
        binding.bvToolBar.setupWith(window?.decorView?.rootView as ViewGroup)
            .setFrameClearDrawable(window?.decorView?.background)
            .setBlurRadius(2f)
        binding.ivBack.setOnClickListener {onBackPressed() }
        binding.tvTitle.setTextTypeFace(Typeface.BOLD)
        ImmersionBar.with(this)
            .statusBarDarkFont(!MultiLanguagesApplication.isNightMode)
            .fitsSystemWindows(false)
            .init()
    }

    private fun initMarquee() {
        binding.rvMarquee.adapter = marqueeAdapter
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
