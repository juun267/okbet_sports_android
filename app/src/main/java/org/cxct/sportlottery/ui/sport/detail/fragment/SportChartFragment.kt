package org.cxct.sportlottery.ui.sport.detail.fragment

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentChartBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.util.fromJson

class SportChartFragment : BindingFragment<SportViewModel, FragmentChartBinding>() {

    val matchInfo by lazy {
        arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
    }


    override fun onInitView(view: View) {
        initRecyclerView()
    }

    private fun initRecyclerView() {

        binding.clRoot.background = ResourcesCompat.getDrawable(
            resources, GameType.getGameTypeDetailBg(
                GameType.getGameType(matchInfo?.gameType) ?: GameType.FT

            ), null
        )

        val list = mutableListOf(
            "112",
            "23",
            "2323",
            "2323",
            "2323",
            "23",
            "112",
            "23",
            "2323",
            "2323",
            "2323",
            "23",
            "112",
            "23",
            "2323",
            "2323",
            "2323",
            "23"
        )

        binding.rcvChartView.apply {
            val rcvAdapter =
                object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_text_view) {
                    override fun convert(holder: BaseViewHolder, item: String) {
                        holder.setText(R.id.tvContent, item)
                    }
                }
            adapter = rcvAdapter
            rcvAdapter.setNewInstance(list)
            layoutManager = GridLayoutManager(requireContext(), 5)
        }

    }

    override fun onInitData() {
        super.onInitData()
    }


}