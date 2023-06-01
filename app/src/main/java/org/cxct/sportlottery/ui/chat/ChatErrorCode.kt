package org.cxct.sportlottery.ui.chat

/**
 * @author Louis
 * @create 2023/3/22
 * @description chat api system error
 */
enum class ChatErrorCode(val code: Int) {
    CHAT_LONG_TIME_NO_OPERATION(2401),//因您长时间未操作，系统自动退出，请重新登录
}
