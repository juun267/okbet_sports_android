package org.cxct.sportlottery.ui.chat.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
class PacketListener(
        private val onClickListener: (packetId: Int, watchWord: String) -> Unit,
        private val onCancelListener: () -> Unit,
        private val onCompleteListener: (packetId: String) -> Unit,
        private val goRegisterPageListener: () -> Unit,
    ):Parcelable {
        fun onClick(packetId: Int, watchWord: String) = onClickListener(packetId, watchWord)
        fun onCancel() = onCancelListener()
        fun onComplete(packetId: String) = onCompleteListener(packetId)
        fun goRegisterPage() = goRegisterPageListener()
    }