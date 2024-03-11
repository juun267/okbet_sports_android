package org.cxct.sportlottery.ui.results.vh

import android.view.View
import org.cxct.sportlottery.databinding.ContentGameDetailResultVbRvBinding
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.ui.results.StatusType

//排球第一筆
    class VbDetailFirstItemViewHolder(val binding: ContentGameDetailResultVbRvBinding){

        fun bind(detailData: Match?) {
            setupBmDetailFirstItem(detailData)
        }

        private fun setupBmDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME }
            var homeRound = 0
            var awayRound = 0

            binding.apply {
                llGameDetailFirstItem.visibility = View.VISIBLE

                matchInfo?.let {
                    tvHomeName.text = it.homeName
                    tvAwayName.text = it.awayName
                }

                //第一
                firstPlat?.let {
                    tvHomeFirst.text = it.homeScore?.apply { homeRound += this }.toString()
                    tvAwayFirst.text = it.awayScore?.apply { awayRound += this }.toString()
                }

                //第二
                secondPlat?.let {
                    tvHomeSecond.text = it.homeScore?.apply { homeRound += this }.toString()
                    tvAwaySecond.text = it.awayScore?.apply { awayRound += this }.toString()
                }

                //第三
                thirdPlat?.let {
                    tvHomeThird.text = it.homeScore?.apply { homeRound += this }.toString()
                    tvAwayThird.text = it.awayScore?.apply { awayRound += this }.toString()
                }

                //第四
                fourthPlat?.let {
                    tvHomeFourth.text = it.homeScore?.apply { homeRound += this }.toString()
                    tvAwayFourth.text = it.awayScore?.apply { awayRound += this }.toString()
                }

                //第五
                fifthPlat?.let {
                    tvHomeFifth.text = it.homeScore?.apply { homeRound += this }.toString()
                    tvAwayFifth.text = it.awayScore?.apply { awayRound += this }.toString()
                }

                //終局
                tvHomeScore.text = homeRound.toString()
                tvAwaySecond.text = awayRound.toString()

                //終盤 完賽(賽果)
                finalPlat?.let {
                    tvHomeRound.text = it.homeScore?.toString()
                    tvAwayRound.text = it.awayScore?.toString()
                }
            }
        }
    }