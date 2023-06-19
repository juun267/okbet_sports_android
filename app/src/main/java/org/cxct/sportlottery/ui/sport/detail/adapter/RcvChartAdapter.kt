package org.cxct.sportlottery.ui.sport.detail.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

class RcvChartAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_text_view) {

        private var gameType: String? = null
        private var currentSpt: Int? = null
        fun setCurrentGameType(gameType: String?) {
            this.gameType = gameType
        }

        fun setCurrentSpt(currentSpt: Int?) {
            this.currentSpt = currentSpt
        }

        override fun convert(holder: BaseViewHolder, item: String) {

            //表格去掉header的剩余宽度(单位dp )
            val width = 258

            when (gameType) {
                GameType.FT.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        lp.width = (width / 5).dp
                        holder.itemView.layoutParams = lp
                    }
                }

                //Tennis 网球
                GameType.TN.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        val setLp: (Int) -> Unit = { it ->
                            lp.width = (width / it).dp
                        }
                        Timber.d("currentSpt:$currentSpt")
                        when (currentSpt) {
                            3, 5, 7 -> {
                                setLp((currentSpt ?: 3) + 2)
                            }

                            else -> {
                                setLp(5)
                            }
                        }
                        holder.itemView.layoutParams = lp
                    }
                }

                //VolleyBall VB排球
                //TableTennis TT乒乓球
                //Badminton BM羽毛球
                GameType.VB.name, GameType.TT.name, GameType.BM.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        val setLp: (Int) -> Unit = { it ->
                            lp.width = (width / it).dp
                        }
                        when (currentSpt) {
                            3, 5, 7 -> {
                                setLp((currentSpt ?: 4) + 1)
                            }

                            else -> {
                                lp.width = (width / 5).dp
                            }
                        }
                        holder.itemView.layoutParams = lp
                    }
                }

                GameType.BK.name, GameType.AFT.name, GameType.IH.name, GameType.CB.name -> {
                    holder.itemView.layoutParams.let { lp ->
                        lp.width = (width / 6).dp
                        holder.itemView.layoutParams = lp
                    }
                }

            }

            holder.setText(R.id.tvContent, item)
        }
    }