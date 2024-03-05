package org.cxct.sportlottery.ui.results.vh

import org.cxct.sportlottery.databinding.ContentGameDetailResultBbRvBinding
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.ui.results.StatusType

//棒球第一筆
    class BbDetailFirstItemViewHolder(val binding: ContentGameDetailResultBbRvBinding){


        fun bind(detailData: Match?) {
            setupBbDetailFirstItem(detailData)
        }

        private fun setupBbDetailFirstItem(detailData: Match?) {
            val matchList = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchList?.find { it.status == StatusType.FIRST_HALF }
            val secondPlat = matchList?.find { it.status == StatusType.SECOND_HALF }
            val thirdPlat = matchList?.find { it.status == StatusType.END_GAME }

            binding.apply {
                matchInfo?.apply {
                    bbHomeName.text = homeName
                    bbAwayName.text = awayName
                }

                matchList.apply {
                    //上半場
                    firstPlat?.let {
                        bbHomeFirst.text = it.homeScore?.toString()
                        bbAwayFirst.text = it.awayScore?.toString()
                    }

                    //下半場
                    secondPlat?.let {
                        bbHomeSecond.text = it.homeScore?.toString()
                        bbAwaySecond.text = it.awayScore?.toString()
                    }

                    //總分
                    thirdPlat?.let {
                        bbHomeThird.text = it.homeScore?.toString()
                        bbAwayThird.text = it.awayScore?.toString()
                    }

                }
            }
        }

    }