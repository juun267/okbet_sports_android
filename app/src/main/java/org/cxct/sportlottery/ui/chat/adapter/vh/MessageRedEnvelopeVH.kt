package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemChatMessageRedEnvelopeBinding as ICMREB
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.bean.ChatRedEnvelopeMsg
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil

class MessageRedEnvelopeVH(parent: ViewGroup,
                           private val binding: ICMREB = ICMREB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bindMsg1001(adpter: ChatMessageListAdapter3, data: ChatRedEnvelopeMsg.ChatRedEnvelopeMsg1001)
    = data.content?.run  {
        when (packetType) {
            ChatMessageListAdapter3.RedEnvelopeType.RANDOM.type -> {
                binding.tvName.text = nickName
                binding.tvMessage.mixFontText =
                    binding.root.context.getString(R.string.chat_room_member) + "\n" +
                            "[\u0020$nickName\u0020]" + "\n" +
                            binding.root.context.getString(R.string.chat_send_red_packets)
                binding.llMessage.apply {
                    setBackgroundResource(if (adpter.isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                    updatePadding(
                        paddingStart,
                        if (adpter.isAdmin) 13.dp else 16.dp,
                        paddingEnd,
                        paddingBottom
                    )
                }
            }
            else -> {
                binding.tvName.text =
                    binding.root.context.getString(R.string.system_red_packet)
                binding.tvMessage.mixFontText =
                    binding.root.context.getString(R.string.chat_opportunity)
                binding.llMessage.apply {
                    setBackgroundResource(if (adpter.isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                    updatePadding(
                        paddingStart,
                        if (adpter.isAdmin) 15.dp else 13.dp,
                        paddingEnd,
                        paddingBottom
                    )
                }
            }

        }
        binding.tvTime.text = if (data.time != null) TimeUtil.timeFormat(
            data.time,
            TimeUtil.HM_FORMAT
        ) else ""

        binding.tvRedEnvelope.apply { if (adpter.isAdmin) hide() else show() }
        binding.tvRedEnvelope.setOnClickListener {
            adpter.onRedEnvelopeClick(id.toString(), packetType)
        }
    }

    fun bindMsg2005(adpter: ChatMessageListAdapter3, data: ChatRedEnvelopeMsg.ChatRedEnvelopeMsg2005)
    = data.content?.run  {

        binding.tvName.text =
            binding.root.context.getString(R.string.system_red_packet)

        binding.llMessage.apply {
            setBackgroundResource(if (adpter.isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
            updatePadding(
                paddingStart,
                if (adpter.isAdmin) 15.dp else 13.dp,
                paddingEnd,
                paddingBottom
            )
        }
        binding.tvRedEnvelope.apply { if (adpter.isAdmin) hide() else show() }
        binding.tvRedEnvelope.setOnClickListener {
            adpter.onRedEnvelopeClick(
                this.id.toString(),
                this.packetType ?: -1
            )
        }
    }


    fun bindMsg2008(adpter: ChatMessageListAdapter3, data: ChatRedEnvelopeMsg.ChatRedEnvelopeMsg2008)
    = data.content?.chatRedEnvelopeMessageResult?.run {
        when (packetType) {
            ChatMessageListAdapter3.RedEnvelopeType.RANDOM.type -> {
                binding.tvName.text = nickName
                binding.tvMessage.mixFontText =
                    binding.root.context.getString(R.string.chat_room_member) + "\n" +
                            "[\u0020$nickName\u0020]" + "\n" +
                            binding.root.context.getString(R.string.chat_send_red_packets)
                binding.llMessage.apply {
                    setBackgroundResource(if (adpter.isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                    updatePadding(
                        paddingStart,
                        if (adpter.isAdmin) 13.dp else 16.dp,
                        paddingEnd,
                        paddingBottom
                    )
                }
            }
            else -> {
                binding.tvName.text =
                    binding.root.context.getString(R.string.system_red_packet)
                binding.tvMessage.mixFontText =
                    binding.root.context.getString(R.string.chat_opportunity)
                binding.llMessage.apply {
                    setBackgroundResource(if (adpter.isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                    updatePadding(
                        paddingStart,
                        if (adpter.isAdmin) 15.dp else 13.dp,
                        paddingEnd,
                        paddingBottom
                    )
                }
            }
        }
        binding.tvTime.text = if (data.time != null) TimeUtil.timeFormat(
            data.time,
            TimeUtil.HM_FORMAT
        ) else ""

        binding.tvRedEnvelope.apply { if (adpter.isAdmin) hide() else show() }
        binding.tvRedEnvelope.setOnClickListener {
            adpter.onRedEnvelopeClick(id.toString(), packetType ?: -1)
        }
    }

}