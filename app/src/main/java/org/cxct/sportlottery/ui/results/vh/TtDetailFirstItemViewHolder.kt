package org.cxct.sportlottery.ui.results.vh

import org.cxct.sportlottery.databinding.ContentGameDetailResultTtRvBinding
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.ui.results.StatusType

//桌球第一筆
    class TtDetailFirstItemViewHolder(val binding: ContentGameDetailResultTtRvBinding){

        fun bind(detailData: Match?) {
            setupTtDetailFirstItem(detailData)
        }

        private fun setupTtDetailFirstItem(detailData: Match?) {
            val matchInfo = detailData?.matchInfo

            val matchList = detailData?.matchStatusList
            val gameStatus = matchList?.find { it.status == StatusType.END_GAME }

            binding.apply {
                matchInfo?.apply {
                    ttHomeName.text = homeName
                    ttAwayName.text = awayName
                }

                //完場
                gameStatus?.let {
                    ttScoreFirst.text = it.homeTotalScore?.toString()
                    ttPlatFirst.text = it.homeScore?.toString()
                    ttScoreSecond.text = it.awayTotalScore?.toString()
                    ttPlatSecond.text = it.awayScore?.toString()
                }
            }
        }
    }