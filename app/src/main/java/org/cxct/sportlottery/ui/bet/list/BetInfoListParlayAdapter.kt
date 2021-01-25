package org.cxct.sportlottery.ui.bet.list

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoParlayItemBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil

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
            val error: Boolean
            if (TextUtils.isEmpty(it)) {
                binding.tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_bigger_than_zero)
                binding.tvParlayWinQuota.text = "--"
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
                winQuotaList[position] = it.toDouble() * parlayOdd.odds
                betQuotaList[position] = it.toDouble() * parlayOdd.num

                binding.tvParlayWinQuota.text = TextUtil.format(it.toDouble() * parlayOdd.odds)

                onTotalQuotaListener.count(winQuotaList.sum(), betQuotaList.sum())
            }

            (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (error) 0.dp else 10.dp

            binding.tvErrorMessage.visibility = if (error) View.VISIBLE else View.GONE

            binding.rlInput.background =
                if (error) ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_error)
                else ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_5_edittext_focus)

            binding.etBet.setTextColor(
                if (error) ContextCompat.getColor(binding.root.context, R.color.orangeRed)
                else ContextCompat.getColor(binding.root.context, R.color.main_dark)
            )
            return error
        }

        @SuppressLint("SetTextI18n")
        fun bind(parlayOdd: ParlayOdd, position: Int) {

            winQuotaList.add(0.0)
            betQuotaList.add(0.0)

            binding.parlayOdd = parlayOdd

            binding.etBet.hint = String.format(binding.root.context.getString(R.string.bet_info_list_hint), parlayOdd.max.toString())
            binding.tvParlayOdds.text = String.format(binding.root.context.getString(R.string.bet_info_list_odd), parlayOdd.odds.toString())

            binding.etBet.afterTextChanged {
                check(it, parlayOdd, position)
            }

            binding.ivClearText.setOnClickListener { binding.etBet.text.clear() }

            binding.tvNum.text = "x${parlayOdd.num}"

            binding.executePendingBindings()
        }
    }


    fun modify(list: List<ParlayOdd>, position: Int) {
        parlayOddList.clear()
        parlayOddList.addAll(list)
        notifyDataSetChanged()
    }


    interface OnTotalQuotaListener {
        fun count(totalWin: Double, totalBet: Double)
    }

}