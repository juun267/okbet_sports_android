package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.util.DisplayUtil.dp

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentPartOkgamesBinding
    private inline fun okgamesFragment() = parentFragment as OKGamesFragment
    private val gameChildAdapter by lazy { GameChildAdapter(listOf()) }
    override fun createRootView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPartOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        binding.apply {
            rvGamesSelect.apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
                adapter = gameChildAdapter
                gameChildAdapter.setOnItemChildClickListener(OnItemChildClickListener { adapter, view, position ->

                })
            }
        }
    }

}