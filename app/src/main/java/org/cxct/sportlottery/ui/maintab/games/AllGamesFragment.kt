package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding

    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {


        onBindPart5View()
    }



    private fun onBindPart5View() {
        val include5 = binding.include5
        setOnClickListener(
            include5.tvPrivacyPolicy,
            include5.tvTermConditions,
            include5.tvResposibleGaming,
            include5.tvLiveChat,
            include5.tvContactUs,
            include5.tvFaqs
        ) {
            when (it.id) {
                R.id.tvPrivacyPolicy -> {

                }

                R.id.tvTermConditions -> {

                }

                R.id.tvResposibleGaming -> {

                }

                R.id.tvLiveChat -> {

                }

                R.id.tvContactUs -> {

                }

                R.id.tvFaqs -> {

                }
            }
        }

    }
}