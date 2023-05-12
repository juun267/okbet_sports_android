package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemChatMessageRedEnvelopeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatPersonalRedEnvelopeResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRedEnvelopeResult
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter2
import org.cxct.sportlottery.ui.chat.ChatMsgReceiveType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil

class MessageRedEnvelopeVH(parent: ViewGroup,
                           private val isAdmin: () -> Boolean,
                           private val onRedEnvelopeClick: (String, Int) -> Unit,
                           private val binding: ItemChatMessageRedEnvelopeBinding = ItemChatMessageRedEnvelopeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {


    fun bind(data: ChatReceiveContent<*>) {
        when (data.type) {
            //1001
            ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE -> {
                data.getThisContent<ChatRedEnvelopeResult>()?.apply {
                    when (packetType) {
                        ChatMessageListAdapter2.RedEnvelopeType.RANDOM.type -> {
                            binding.tvName.text = nickName
                            binding.tvMessage.mixFontText =
                                binding.root.context.getString(R.string.chat_room_member) + "\n" +
                                        "[\u0020$nickName\u0020]" + "\n" +
                                        binding.root.context.getString(R.string.chat_send_red_packets)
                            binding.llMessage.apply {
                                setBackgroundResource(if (isAdmin()) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                                updatePadding(
                                    paddingStart,
                                    if (isAdmin()) 13.dp else 16.dp,
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
                                setBackgroundResource(if (isAdmin()) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                                updatePadding(
                                    paddingStart,
                                    if (isAdmin()) 15.dp else 13.dp,
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

                    binding.tvRedEnvelope.apply { if (isAdmin()) hide() else show() }
                    binding.tvRedEnvelope.setOnClickListener {
                        onRedEnvelopeClick(id.toString(), packetType)
                    }
                }
            }

            //2005
            ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE -> {
                data.getThisContent<ChatPersonalRedEnvelopeResult>()?.apply {
                    binding.tvName.text =
                        binding.root.context.getString(R.string.system_red_packet)

                    binding.llMessage.apply {
                        setBackgroundResource(if (isAdmin()) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                        updatePadding(
                            paddingStart,
                            if (isAdmin()) 15.dp else 13.dp,
                            paddingEnd,
                            paddingBottom
                        )
                    }
                    binding.tvRedEnvelope.apply { if (isAdmin()) hide() else show() }
                    binding.tvRedEnvelope.setOnClickListener {
                        onRedEnvelopeClick(
                            this.id.toString(),
                            this.packetType ?: -1
                        )
                    }
                }
            }

            //2008
            ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE -> {
                data.getThisContent<ChatMessageResult>()?.chatRedEnvelopeMessageResult?.apply {
                    when (packetType) {
                        ChatMessageListAdapter2.RedEnvelopeType.RANDOM.type -> {
                            binding.tvName.text = nickName
                            binding.tvMessage.mixFontText =
                                binding.root.context.getString(R.string.chat_room_member) + "\n" +
                                        "[\u0020$nickName\u0020]" + "\n" +
                                        binding.root.context.getString(R.string.chat_send_red_packets)
                            binding.llMessage.apply {
                                setBackgroundResource(if (isAdmin()) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                                updatePadding(
                                    paddingStart,
                                    if (isAdmin()) 13.dp else 16.dp,
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
                                setBackgroundResource(if (isAdmin()) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                                updatePadding(
                                    paddingStart,
                                    if (isAdmin()) 15.dp else 13.dp,
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

                    binding.tvRedEnvelope.apply { if (isAdmin()) hide() else show() }
                    binding.tvRedEnvelope.setOnClickListener {
                        onRedEnvelopeClick(
                            id.toString(),
                            packetType ?: -1
                        )
                    }
                }
            }
        }
    }
}