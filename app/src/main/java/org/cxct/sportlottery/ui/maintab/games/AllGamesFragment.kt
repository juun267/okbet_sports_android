package org.cxct.sportlottery.ui.maintab.games

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setServiceClick

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding
    private val gameAllAdapter by lazy {
        GameCategroyAdapter(
            clickCollect = {
                okGamesFragment().viewModel.collectGame(it.id, !it.markCollect)
            },
            clickGame = {
                okGamesFragment().viewModel.requestEnterThirdGame(it, this@AllGamesFragment)
            }
        )
    }
    private val providersAdapter by lazy { OkGameProvidersAdapter() }
    private val gameRecordAdapter by lazy { OkGameRecordAdapter() }

    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAllOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
    }

    private fun initObserve() {
        okGamesFragment().viewModel.gameHall.observe(this.viewLifecycleOwner) {
            val categoryList =
                it.categoryList?.filter { !it.gameList.isNullOrEmpty() }?.toMutableList()
                    ?: mutableListOf()
            gameAllAdapter.setList(categoryList)
        }
        okGamesFragment().viewModel.collectOkGamesResult.observe(this.viewLifecycleOwner) { result ->
            var needUpdate = false
            gameAllAdapter.data.forEach {
                it.gameList?.forEach { gameBean ->
                    if (gameBean.id == result.first) {
                        gameBean.markCollect = result.second
                        needUpdate = true
                    }
                }
            }
            if (needUpdate) {
                gameAllAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onBindGamesView() {
        binding.includeGamesAll.apply {
            rvGamesAll.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rvGamesAll.adapter = gameAllAdapter
            gameAllAdapter.setOnItemChildClickListener(OnItemChildClickListener { adapter, view, position ->
                gameAllAdapter.data[position].let {
                    okGamesFragment().showGameResult(it.categoryName, categoryId = it.id.toString())
                }
            })
        }
    }

    private fun onBindPart3View() {
        binding.include3.apply {
            rvOkgameProviders.adapter = providersAdapter
            rvOkgameRecord.adapter = gameRecordAdapter
            rvOkgameRecord.itemAnimator = DefaultItemAnimator()
            receiver.recordNew.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (gameRecordAdapter.data.size >= 10) {
                        gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
                    }
                    gameRecordAdapter.addData(0,it)
                }
            }

        }


    }

    private fun onBindPart5View() {
        val include3 = binding.include3
        val include5 = binding.include5
        val tvPrivacyPolicy = include5.tvPrivacyPolicy
        val tvTermConditions = include5.tvTermConditions
        val tvResponsibleGaming = include5.tvResponsibleGaming
        val tvLiveChat = include5.tvLiveChat
        val tvContactUs = include5.tvContactUs
        val tvFaqs = include5.tvFaqs
        setUnderline(
            tvPrivacyPolicy, tvTermConditions, tvResponsibleGaming, tvLiveChat, tvContactUs, tvFaqs
        )
        setOnClickListener(
            tvPrivacyPolicy, tvTermConditions, tvResponsibleGaming, tvLiveChat, tvContactUs, tvFaqs
        ) {
            when (it.id) {
                R.id.tvPrivacyPolicy -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getPrivacyRuleUrl(requireContext()),
                        getString(R.string.privacy_policy)
                    )
                }

                R.id.tvTermConditions -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getAgreementRuleUrl(requireContext()),
                        getString(R.string.terms_conditions)
                    )
                }

                R.id.tvResponsibleGaming -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getDutyRuleUrl(requireContext()),
                        getString(R.string.responsible)
                    )
                }

                R.id.tvLiveChat, R.id.tvContactUs -> {
                    it.setServiceClick(childFragmentManager)
                }

                R.id.tvFaqs -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getFAQsUrl(requireContext()),
                        getString(R.string.faqs)
                    )
                }
            }
        }

    }

    private fun setUnderline(vararg view: TextView) {
        view.forEach {
            it.paint.flags = Paint.UNDERLINE_TEXT_FLAG; //下划线
            it.paint.isAntiAlias = true;//抗锯齿
        }
    }
}