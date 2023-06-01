package org.cxct.sportlottery.ui.chat

import android.widget.Button

interface OnChatListener {
    fun onSelectName(name: String) //點擊姓名
    fun onBetFollow(chatMessage: ChatMessage, btnBetFollow: Button) //跟注
    fun onGetFollowPlanProjectMoney(
        chatMessage: ChatMessage,
        btnBetFollow: Button,
        projectId: String,
    )

    fun onLike(chatMessage: ChatMessage, btnGood: Button) //按讚
    fun onReward(chatMessage: ChatMessage, btnBetFollow: Button) //打賞
    fun onRedOpen(chatMessage: ChatMessage) //開紅包
    fun onSystemMsg()
    fun onRefreshUnPacketList(packetId: String)
}