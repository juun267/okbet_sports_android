package org.cxct.sportlottery.ui.maintab.home.game.perya

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentMinigameListBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
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
        loadingHolder.withRetry { OKGamesViewModel.loadMiniGameList(lifecycleScope) }
    }

    override fun onBindViewStatus(view: View) {
        initObserver()

        if (adapter.itemCount < 2) {
            val cacheData = OKGamesViewModel.getActiveMiniGameData()
            if (cacheData == null) {
                loadingHolder.go()
            } else {
                adapter.setNewInstance(cacheData.toMutableList())
            }
        }
    }

    private fun initObserver() {
        OKGamesViewModel.miniGameList.observe(viewLifecycleOwner) {
            if (it.first == null) {
                loadingHolder.showLoadFailed()
            } else {
                adapter.setNewInstance(it.first?.toMutableList())
                loadingHolder.showLoadSuccess()
            }
        }
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleScope) {
            if (Constants.FIRM_TYPE_OKMINI == it.firmCode) {
                adapter.updateMaintainStatus(it.maintain)
            }
        }
    }

    private class GameListAdapter: BaseQuickAdapter<OKGameBean?, BaseViewHolder>(0) {

        private val imgId = View.generateViewId()
        private val textId = View.generateViewId()
        private val maintainceId = View.generateViewId()

        init {
            setDiffCallback(object : DiffUtil.ItemCallback<OKGameBean?>() {
                override fun areItemsTheSame(oldItem: OKGameBean, newItem: OKGameBean) = oldItem == newItem
                override fun areContentsTheSame(oldItem: OKGameBean, newItem: OKGameBean): Boolean {
                    return oldItem.id == newItem.id && oldItem.maintain == newItem.maintain
                }
            })
        }

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val dp8 = 8.dp
            val dp12 = 12.dp
            val root = CardView(context)
            root.cardElevation = 0f
            root.radius = dp8.toFloat()
            root.foreground = context.getDrawable(R.drawable.fg_ripple)
            val lp = FrameLayout.LayoutParams(-1, 180.dp)
            lp.setMargins(dp12, dp8, dp12, 0)
            root.layoutParams = lp

            val lpChild = FrameLayout.LayoutParams(-1, -1)
            val img = AppCompatImageView(context)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            img.id = imgId
            root.addView(img, lpChild)

            val linMaintaince = LayoutInflater.from(context).inflate(R.layout.view_game_maintenance, null)
            linMaintaince.id = maintainceId
            root.addView(linMaintaince, lpChild)

            val text = AppCompatTextView(context)
            text.id = textId
            text.typeface = AppFont.helvetica_bold
            text.gravity = Gravity.CENTER
            text.setTextColor(Color.WHITE)
            text.textSize = 18f
            root.addView(text, lpChild)

            return BaseViewHolder(root)
        }

        override fun convert(holder: BaseViewHolder, item: OKGameBean?) {

            if (item == null) {
                holder.setImageResource(imgId, R.drawable.img_minigame_unknow)
                holder.setVisible(maintainceId, false)
                val textView = holder.getView<TextView>(textId)
                textView.show()
                textView.setText(R.string.M013)
                holder.setVisible(textId, true)
                return
            }
            holder.setVisible(textId, false)
            holder.getView<ImageView>(imgId).roundOf(item.imgBigGame, 8.dp, R.drawable.img_placeholder_default)
            if (!item.isMaintain()) {
                holder.setVisible(maintainceId, false)
            } else {
                holder.setVisible(maintainceId, true)
            }
        }

        override fun setNewInstance(list: MutableList<OKGameBean?>?) {
            if (list != null && list.size == 1) {
                list?.add(null)
            }
            super.setNewInstance(list)
        }

        fun updateMaintainStatus(maintain: Int) {
            if (getDefItemCount() > 0) {
                data.forEach { it?.maintain = maintain }
                notifyDataSetChanged()
            }
        }

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = adapter.getItemOrNull(position) ?: return
        val okGameBean = item as OKGameBean
        if (!okGameBean.isMaintain() && StaticData.miniGameOpened()) {
            (requireActivity() as MainTabActivity).enterThirdGame(item, null)
        }
    }


}