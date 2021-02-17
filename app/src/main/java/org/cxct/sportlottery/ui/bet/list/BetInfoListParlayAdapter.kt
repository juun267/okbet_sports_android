package org.cxct.sportlottery.ui.bet.list

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_action.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoParlayItemBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import java.math.RoundingMode

class BetInfoListParlayAdapter(private val onTotalQuotaListener: OnTotalQuotaListener) :
        RecyclerView.Adapter<BetInfoListParlayAdapter.ViewHolder>() {


    var parlayOddList: MutableList<ParlayOdd> = mutableListOf()
    val winQuotaList: MutableList<Double> = mutableListOf()
    val betQuotaList: MutableList<Double> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ContentBetInfoParlayItemBinding = ContentBetInfoParlayItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return parlayOddList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(parlayOddList[position], position)
    }


    inner class ViewHolder(private val binding: ContentBetInfoParlayItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun check(it: String, parlayOdd: ParlayOdd, position: Int): Boolean {
            val sendOutStatus: Boolean
            if (TextUtils.isEmpty(it)) {
                binding.tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_bigger_than_zero)
                binding.tvParlayWinQuota.text = "--"
                winQuotaList[position] = 0.0
                betQuotaList[position] = 0.0
                sendOutStatus = true
            } else {

                val quota = it.toLong()

                when {
                    quota > parlayOdd.max -> {
                        binding.tvErrorMessage.text =
                                String.format(
                                        binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit),
                                        parlayOdd.max.toString()
                                )
                        sendOutStatus = false
                    }

                    quota < parlayOdd.min -> {
                        binding.tvErrorMessage.text =
                                String.format(
                                        binding.root.context.getString(R.string.bet_info_list_less_than_minimum_limit),
                                        parlayOdd.min.toString()
                                )
                        sendOutStatus = false
                    }

                    else -> {
                        sendOutStatus = true
                    }
                }

                winQuotaList[position] = it.toDouble() * parlayOdd.odds
                betQuotaList[position] = it.toDouble() * parlayOdd.num

                binding.tvParlayWinQuota.text =
                        ArithUtil.round(
                                it.toDouble() * parlayOdd.odds,
                                3,
                                RoundingMode.HALF_UP)

            }
            onTotalQuotaListener.count(winQuotaList.sum(), betQuotaList.sum())

            (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (sendOutStatus) 10.dp else 0.dp

            binding.tvErrorMessage.visibility = if (sendOutStatus) View.GONE else View.VISIBLE

            binding.rlInput.background =
                    if (sendOutStatus) ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_focus)
                    else ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_error)

            binding.etBet.setTextColor(
                    if (sendOutStatus) ContextCompat.getColor(binding.root.context, R.color.main_dark)
                    else ContextCompat.getColor(binding.root.context, R.color.orangeRed)
            )
            return sendOutStatus
        }

        @SuppressLint("SetTextI18n")
        fun bind(parlayOdd: ParlayOdd, position: Int) {

            winQuotaList.add(0.0)
            betQuotaList.add(0.0)

            binding.parlayOdd = parlayOdd

            binding.tvParlayType.text = parlayOdd.parlayType.replace("C", "串")

            binding.etBet.hint = String.format(binding.root.context.getString(R.string.bet_info_list_hint), parlayOdd.max.toString())
            binding.tvParlayOdds.text =
                    String.format(binding.root.context.getString(R.string.bet_info_list_odd), TextUtil.formatForOdd(parlayOdd.odds))

            if (!TextUtils.isEmpty(binding.etBet.text.toString())) {
                parlayOddList[position].sendOutStatus = check(binding.etBet.text.toString(), parlayOdd, position)
                onTotalQuotaListener.sendOutStatus(parlayOddList)
            }

            binding.etBet.afterTextChanged {
                parlayOddList[position].sendOutStatus = check(it, parlayOdd, position)
                onTotalQuotaListener.sendOutStatus(parlayOddList)
            }

            binding.ivClearText.setOnClickListener { binding.etBet.text.clear() }

            binding.tvNum.text = "x${parlayOdd.num}"

            binding.executePendingBindings()

        }
    }


    fun modify(list: List<ParlayOdd>) {
        winQuotaList.clear()
        betQuotaList.clear()
        parlayOddList.clear()
        parlayOddList.addAll(list.filterNot {
            it.parlayType == "1C1"
        })
        notifyDataSetChanged()
    }


    interface OnTotalQuotaListener {
        fun count(totalWin: Double, totalBet: Double)
        fun sendOutStatus(parlayOddList: MutableList<ParlayOdd>)
    }


}