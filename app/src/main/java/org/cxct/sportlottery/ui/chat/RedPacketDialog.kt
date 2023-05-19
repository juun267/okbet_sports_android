package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.dialog_chat_red_enp.*
import kotlinx.android.synthetic.main.dialog_chat_red_enp.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.*

/**
 * @author Bill
 * @create 2023/3/19
 * @description
 * 聊天室紅包彈窗
 * */

class RedPacketDialog(
    context: Context,
    var packetClickListener: PacketListener,
    var packetId: String,
    var packetType: Int,
) : AlertDialog(context) {

    private var mView = layoutInflater.inflate(R.layout.dialog_chat_red_enp, null)

    enum class PacketType(val code: Int) {
        PW_PACKET(5) //口令紅包
    }

    class PacketListener(
        private val onClickListener: (packetId: Int, watchWord: String) -> Unit,
        private val onCancelListener: () -> Unit,
        private val onCompleteListener: (packetId: String) -> Unit,
        private val goRegisterPageListener: () -> Unit,
    ) {
        fun onClick(packetId: Int, watchWord: String) = onClickListener(packetId, watchWord)
        fun onCancel() = onCancelListener()
        fun onComplete(packetId: String) = onCompleteListener(packetId)
        fun goRegisterPage() = goRegisterPageListener()
    }

    //是否登入
    private val isLogin: Boolean
        get() = LoginRepository.isLogin.value == true

    init {
        this.window?.requestFeature(Window.FEATURE_NO_TITLE)
        this.window?.decorView?.setPadding(0, 0, 0, 0)
        this.window?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        this.window?.setDimAmount(0.5f) // 透明度 (0:完全透明 ~ 1:完全不透明)
        this.window?.setGravity(Gravity.CENTER)
        this.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)//處理 一開始先隱藏鍵盤
        this.setCanceledOnTouchOutside(false)
        this.window?.setWindowAnimations(R.style.AnimDialogEnter) //開啟動畫設置

        setView(mView)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initButtonAndAnimation()
    }

    private fun initButtonAndAnimation() {
        val flipIn = AnimationUtils.loadAnimation(context, R.anim.flip_in)
        flipIn.repeatCount = Animation.INFINITE
        flipIn.interpolator = LinearInterpolator()

        mView.btn_close.setOnClickListener {
            dismiss()
        }
    }

    private fun initView() {

        mView.btn_red_packet_open.setOnClickListener {
            if (!isLogin || isGuest()) {
                packetClickListener.goRegisterPage()
            } else {
                if (packetType == PacketType.PW_PACKET.code &&
                    watchWordInput.text.trim().toString().isEmpty()
                ) {
                    showPWErrorHint()
                    mView.watchWordInput.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_watchword_input)
                } else
                    packetClickListener.onClick(
                        packetId.toInt(),
                        watchWordInput.text.trim().toString()
                    )
            }
        }

        mView.card_view.setOnClickListener {
            dismiss()
        }

        //口令紅包，顯示輸入口令欄位
        if (packetType == PacketType.PW_PACKET.code) {
            mView.titleLayout.hide()
            mView.watchWordLayout.show()
        } else {
            mView.titleLayout.show()
            mView.watchWordLayout.hide()
        }
    }

    //紅包成功開啟 dialog
    @SuppressLint("SetTextI18n")
    fun showRedPacketOpenDialog(money: Double) {
        mView.red_enp_open.visibility = View.VISIBLE
        mView.red_enp_close.visibility = View.GONE
        mView.tv_money.text = ArithUtil.toMoneyFormat(money)
    }

    fun showPWErrorHint() {
//        if (watchWordInput.text.isNotEmpty()) {
        watchWordMessage.text = context.getString(R.string.chat_error_input)
//        } else
//            watchWordMessage.text = context.getString(R.string.chat_enter_password)
    }

}
