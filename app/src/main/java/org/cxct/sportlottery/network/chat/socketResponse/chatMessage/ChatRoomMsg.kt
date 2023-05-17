package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.chat.adapter.IChatVH
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

@KeepMembers
abstract class ChatRoomMsg<T, VH: BaseViewHolder> (
    val content: T? = null,
    val seq: Int = 0,//信息编号,client发送讯息id,如果为server主动推送，则为0
    val time: Long = 0,
    val type: Int = -1,
): MultiItemEntity, IChatVH<VH> {

    // 时间标签
    val dateTips by lazy {
        val date = if (content is ChatMessageResult) content.curTime else time
        "${TimeUtil.timeStampToDateString(date, TimeUtil.D_NARROW_MONTH, Locale.US)}"
    }

    companion object {

        const val MESSAGE_ADMIN = 2
        const val MESSAGE_USER = 3
        const val MESSAGE_ME = 4
        const val TIME = 5
        const val SYSTEM = 6
        const val RED_ENVELOPE = 7
        const val WIN_RED_ENVELOPE = 8
        const val DATE_TIP = 9
        const val FLOATING_DATE_TIP = 10

    }

}