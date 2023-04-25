package org.cxct.sportlottery.ui.chat

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ActivityChatBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTextTypeFace
import java.util.*

class ChatActivity : BaseSocketActivity<ChatViewModel>(ChatViewModel::class) {

    private lateinit var binding: ActivityChatBinding

    private val navController by lazy { findNavController(R.id.container) }

    private var chatTimer: Timer? = null

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
    }

    fun initToolbar() {
        binding.bvToolBar.setupWith(window?.decorView?.rootView as ViewGroup)
            .setFrameClearDrawable(window?.decorView?.background)
            .setBlurRadius(2f)
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvTitle.setTextTypeFace(Typeface.BOLD)

        ImmersionBar.with(this)
            .statusBarDarkFont(!MultiLanguagesApplication.isNightMode)
            .fitsSystemWindows(false)
            .init()
    }

    private fun initMarquee() {
        binding.rvMarquee.adapter = marqueeAdapter
    }

    private fun initObserve() {
        lifecycleScope.launch {
            viewModel.chatEvent.collect { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.UpdateMarquee -> {
                        marqueeAdapter.setData(chatEvent.marqueeList)
                        binding.rvMarquee.startAuto(false)
                    }
                    is ChatEvent.ShowPhoto -> {
                        val chatPhotoFragment = ChatPhotoFragment.newInstance(chatEvent.photoUrl)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_chat_photo, chatPhotoFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startCheckToken()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus() //背景返回必須重走checkLoginStatus
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    override fun onDestroy() {
        viewModel.leaveRoom()
        super.onDestroy()
    }

    private fun startCheckToken() {
        try {
            if (viewModel.loginRepository.isLogin.value == true) {
                if (chatTimer == null) {
                    chatTimer = Timer()
                    chatTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            viewModel.checkChatTokenIsAlive()
                        }
                    }, 30000, 30000) //延時(delay)在30s後重複執行task, 週期(period)是30s
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopTimer() {
        chatTimer?.cancel()
        chatTimer = null
    }
}
