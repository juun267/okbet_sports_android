package org.cxct.sportlottery.ui.maintab.home.game.perya

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentMinigameListBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class MiniGameListFragment: BaseFragment<OKGamesViewModel, FragmentMinigameListBinding>(), OnItemClickListener {

    private val loadingHolder by lazy { Gloading.wrapView(binding.root) }
    private val adapter = GameListAdapter()

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = loadingHolder.wrapper

    override fun onInitView(view: View) {
        binding.recyclerView.setLinearLayoutManager()
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
        loadingHolder.withRetry { viewModel.getMiniGameList() }
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
        if (adapter.itemCount < 2) {
            loadingHolder.go()
        }
    }

    private fun initObserver() {
        viewModel.miniGameList.observe(viewLifecycleOwner) {
            if (it == null) {
                loadingHolder.showLoadFailed()
            } else {
                adapter.setNewInstance(it?.toMutableList())
                loadingHolder.showLoadSuccess()
            }
        }
    }

    private class GameListAdapter: BaseQuickAdapter<OKGameBean?, BaseViewHolder>(0) {

        private val imgId = View.generateViewId()
        private val textId = View.generateViewId()

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val dp8 = 8.dp
            val dp12 = 12.dp
            val root = CardView(context)
            root.cardElevation = 0f
            root.radius = dp8.toFloat()
            val lp = FrameLayout.LayoutParams(-1, 140.dp)
            lp.setMargins(dp12, dp8, dp12, 0)
            root.layoutParams = lp

            val lpChild = FrameLayout.LayoutParams(-1, -1)
            val img = AppCompatImageView(context)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            img.id = imgId
            root.addView(img, lpChild)

            val text = AppCompatTextView(context)
            text.id = textId
            text.typeface = AppFont.helvetica_bold
            text.gravity = Gravity.CENTER
            text.setTextColor(Color.WHITE)
            text.textSize = 18f
            text.setText(R.string.M013)
            root.addView(text, lpChild)

            return BaseViewHolder(root)
        }

        override fun convert(holder: BaseViewHolder, item: OKGameBean?) {
            if (item != null) {
                holder.getView<ImageView>(imgId).roundOf(item.imgGame, 8.dp, R.drawable.img_placeholder_default)
                holder.setVisible(textId, false)
            } else {
                holder.setImageResource(imgId, R.drawable.img_minigame_unknow)
                holder.setVisible(textId, true)
            }
        }

        override fun setNewInstance(list: MutableList<OKGameBean?>?) {
            list?.add(null)
            super.setNewInstance(list)
        }

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = adapter.getItemOrNull(position) ?: return
        viewModel.requestEnterThirdGame(item as OKGameBean, requireActivity() as BaseActivity<*, *>)
    }


}