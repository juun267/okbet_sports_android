package org.cxct.sportlottery.ui.bet.list

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoParlayItemBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import java.math.RoundingMode

class BetInfoListParlayAdapter(private val onTotalQuotaListener: OnTotalQuotaListener) :
    RecyclerView.Adapter<BetInfoListParlayAdapter.ViewHolder>() {


    var focusPosition = -1


    var parlayOddList: MutableList<ParlayOdd> = mutableListOf()
    val winQuotaList: MutableList<Double> = mutableListOf()
    val betQuotaList: MutableList<Double> = mutableListOf()//用於計算
    val sendBetQuotaList: MutableList<Double> = mutableListOf()//用於送出


    var oddsType: String = OddsType.EU.value
        set(value) {
            field = value
            notifyDataSetChanged()
        }


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
                binding.tvParlayWinQuota.text = "--".plus(" ").plus(binding.root.context.getString(R.string.bet_info_list_rmb))
                winQuotaList[position] = 0.0
                betQuotaList[position] = 0.0
                sendBetQuotaList[position] = 0.0
                sendOutStatus = true
            } else {

                val quota = it.toLong()

                when {
                    quota > parlayOdd.max -> {
                        binding.tvErrorMessage.text =
                            String.format(
                                binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit),
                                TextUtil.formatMoneyNoDecimal(parlayOdd.max)
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

                winQuotaList[position] = it.toDouble() * getOdds(parlayOdd, oddsType)
                betQuotaList[position] = it.toDouble() * parlayOdd.num
                sendBetQuotaList[position] = it.toDouble()

                binding.tvParlayWinQuota.text = TextUtil.format(it.toDouble() * getOdds(parlayOdd, oddsType))
                    .plus(" ")
                    .plus(binding.root.context.getString(R.string.bet_info_list_rmb))

            }
            onTotalQuotaListener.count(winQuotaList.sum(), betQuotaList.sum())

            (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (sendOutStatus) 9.dp else 0.dp

            binding.tvErrorMessage.visibility = if (sendOutStatus) View.GONE else View.VISIBLE

            binding.etBet.setBackgroundResource(if (sendOutStatus) R.drawable.effect_select_bet_edit_text else R.drawable.bg_radius_4_edittext_error)

            binding.etBet.setTextColor(
                if (sendOutStatus) ContextCompat.getColor(binding.root.context, R.color.main_dark)
                else ContextCompat.getColor(binding.root.context, R.color.colorRedDark)
            )
            return sendOutStatus
        }

        @SuppressLint("SetTextI18n")
        fun bind(parlayOdd: ParlayOdd, position: Int) {

            /* fix focus */
            if (binding.etBet.tag is TextWatcher) {
                binding.etBet.removeTextChangedListener(binding.etBet.tag as TextWatcher)
            }
            binding.etBet.onFocusChangeListener = null

            winQuotaList.add(0.0)
            betQuotaList.add(0.0)
            sendBetQuotaList.add(0.0)

            binding.parlayOdd = parlayOdd

            binding.tvParlayType.text = parlayOdd.parlayType.replace("C", "串")

            binding.etBet.hint = String.format(binding.root.context.getString(R.string.bet_info_list_hint), TextUtil.formatForBetHint(parlayOdd.max))

            binding.tvParlayOdds.text = TextUtil.formatForOdd(getOdds(parlayOdd, oddsType))

            binding.tvParlayOdds.visibility = if (position == 0) View.VISIBLE else View.GONE

            binding.ivClearText.setOnClickListener { binding.etBet.text.clear() }

            binding.tvNum.text = "x${parlayOdd.num}"

            if (!TextUtils.isEmpty(binding.etBet.text.toString())) {
                parlayOddList[position].sendOutStatus = check(binding.etBet.text.toString(), parlayOdd, position)
                onTotalQuotaListener.sendOutStatus(parlayOddList)
            }

            /* check input focus */
            if (position == focusPosition) {
                binding.etBet.requestFocus()
                binding.etBet.setSelection(binding.etBet.text.length)
            }

            /* set listener */
            val tw = object : TextWatcher {
                override fun afterTextChanged(it: Editable?) {
                    parlayOddList[position].sendOutStatus = check(it.toString(), parlayOdd, position)
                    onTotalQuotaListener.sendOutStatus(parlayOddList)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            val fc = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusPosition = position
                    binding.etBet.requestFocus()
                } else {
                    binding.etBet.clearFocus()
                }
            }

            binding.etBet.onFocusChangeListener = fc
            binding.etBet.addTextChangedListener(tw)
            binding.etBet.tag = tw


            binding.executePendingBindings()

        }
    }


    fun modify(list: List<ParlayOdd>) {
        winQuotaList.clear()
        betQuotaList.clear()
        sendBetQuotaList.clear()
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