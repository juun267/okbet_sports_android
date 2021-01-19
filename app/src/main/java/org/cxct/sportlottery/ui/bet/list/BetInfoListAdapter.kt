package org.cxct.sportlottery.ui.bet.list

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_action.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoItemSingleBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil


class BetInfoListAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BetInfoListAdapter.ViewHolder>() {

    var betInfoList: MutableList<BetInfoListData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ContentBetInfoItemSingleBinding = ContentBetInfoItemSingleBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return betInfoList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(betInfoList[position].matchOdd, betInfoList[position].parlayOdds, position)
    }


    inner class ViewHolder(private val binding: ContentBetInfoItemSingleBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun check(it: String, matchOdd: MatchOdd, parlayOdd: ParlayOdd): Boolean {
            val error: Boolean
            if (TextUtils.isEmpty(it)) {
                binding.tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_bigger_than_zero)
                binding.betInfoAction.tv_bet_quota.text = "0"
                binding.betInfoAction.tv_win_quota.text = "0"
                error = true
            } else {

                val quota = it.toLong()

                when {
                    quota > parlayOdd.max -> {
                        binding.tvErrorMessage.text =
                            String.format(
                                binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit),
                                parlayOdd.max.toString()
                            )
                        error = true
                    }

                    quota < parlayOdd.min -> {
                        binding.tvErrorMessage.text =
                            String.format(
                                binding.root.context.getString(R.string.bet_info_list_less_than_minimum_limit),
                                parlayOdd.min.toString()
                            )
                        error = true
                    }

                    else -> {
                        error = false
                    }
                }

                binding.betInfoAction.tv_bet_quota.text = TextUtil.format(quota)
                binding.betInfoAction.tv_win_quota.text = TextUtil.format(it.toDouble() * matchOdd.odds)
            }

            (binding.rlInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (error) 0.dp else 10.dp

            binding.tvErrorMessage.visibility = if (error) View.VISIBLE else View.GONE

            binding.rlInput.background =
                if (error) ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_error)
                else ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_focus)

            binding.etBet.setTextColor(
                if (error) ContextCompat.getColor(binding.root.context, R.color.orangeyRed)
                else ContextCompat.getColor(binding.root.context, R.color.main_dark)
            )

            binding.betInfoAction.tv_bet.apply {
                isClickable = if (error) {
                    background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_button_unselected)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.bright_gray))
                    false
                } else {
                    background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_button_pumkinorange)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    true
                }
            }
            return error
        }

        fun bind(matchOdd: MatchOdd, parlayOdd: ParlayOdd, position: Int) {
            binding.matchOdd = matchOdd
            binding.parlayOdd = parlayOdd
            binding.etBet.hint = String.format(binding.root.context.getString(R.string.bet_info_list_hint), parlayOdd.max.toString())
            binding.betInfoDetail.tvOdds.text = String.format(binding.root.context.getString(R.string.bet_info_list_odd), matchOdd.odds.toString())

            binding.etBet.afterTextChanged {
                check(it, matchOdd, parlayOdd)
            }

            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            binding.betInfoAction.tv_bet.setOnClickListener {
                if (!check(binding.etBet.text.toString(), matchOdd, parlayOdd)) {
                    onItemClickListener.onBetClick()
                }
            }
            binding.betInfoAction.tv_add_more.setOnClickListener { onItemClickListener.onAddMoreClick() }
            binding.ivClearText.setOnClickListener { binding.etBet.text.clear() }
            binding.executePendingBindings()
        }
    }


    fun modify(list: MutableList<BetInfoListData>, position: Int) {
        if (betInfoList.size == 0) {
            betInfoList.addAll(list)
            notifyDataSetChanged()
        } else if (list.size < betInfoList.size) {
            with(betInfoList) {
                notifyItemRemoved(position)
                notifyItemRangeChanged(0, betInfoList.size)
                clear()
                addAll(list)
            }
        }
    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
        fun onBetClick()
        fun onAddMoreClick()
    }

}